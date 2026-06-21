@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CancellationException
import opensavvy.spine.api.DynamicResource
import opensavvy.spine.api.Endpoint
import opensavvy.spine.api.FailureSpec
import opensavvy.spine.api.FailureSpec.Or
import opensavvy.spine.api.Parameters
import kotlin.jvm.JvmName

/**
 * The various methods available within the handler of a Ktor endpoint.
 *
 * Full Ktor information is available via [call], as is standard in Ktor endpoints.
 */
@KtorDsl
interface TypedResponseScope<out In : Any, out Out : Any, out Failure : FailureSpec, out Params : Parameters> {

	/**
	 * The standard Ktor [ApplicationCall] instance, which is used to access cookies
	 * or any other information directly from Ktor.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * routing {
	 *     route(Api.Users.logIn) {
	 *         // …perform log in…
	 *
	 *         call.response.cookies.append("my-auth", "123")
	 *         respond(Unit)
	 *     }
	 * }
	 * ```
	 */
	val call: ApplicationCall

	/**
	 * The declared Spine [endpoint][opensavvy.spine.api.AnyEndpoint] which was called by the user.
	 */
	val endpoint: Endpoint<out In, out Out, out Failure, out Params>

	/**
	 * The request body sent by the client.
	 *
	 * Spine automatically deserializes this value based on the [request][opensavvy.spine.api.AnyEndpoint.Builder.request] type declared in the endpoint.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * routing {
	 *     route(Api.Users.logIn) {
	 *         println("User ${body.username} wants to log in…")
	 *         // …
	 *     }
	 * }
	 * ```
	 */
	val body: In

	/**
	 * The parameters sent by the client.
	 *
	 * Spine automatically deserializes this value based on the [parameters][opensavvy.spine.api.AnyEndpoint.Builder.parameters] type declared in the endpoint.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * routing {
	 *     route(Api.Users.list) {
	 *         println("Include archived users? ${parameters.includeArchived}")
	 *         // …
	 *     }
	 * }
	 * ```
	 */
	val parameters: Params

	/**
	 * Extracts the identifier for a [DynamicResource] as it was provided by the client.
	 *
	 * For example, if we declare the following API:
	 * ```kotlin
	 * object Api : RootResource("api") {
	 *     object Users : StaticResource<Api>("users", Api) {
	 *         object User : DynamicResource<Users>("user", Users) {
	 *             val get by get()
	 *         }
	 *     }
	 * }
	 * ```
	 * and the client calls the endpoint `GET /api/users/123`, then we can declare our route as:
	 * ```kotlin
	 * route(Api.Users.User) {
	 *     val id = idOf(Api.Users.User) // "123", because this is what the client passed for this path parameter
	 * }
	 * ```
	 */
	fun idOf(resource: DynamicResource<*>): String =
		call.parameters[resource.slug.removePrefix("{").removeSuffix("}")]
			?: error("Could not find the required path parameter ${resource.slug} for resource $resource. This shouldn't be possible: Ktor shouldn't invoke this route if the path parameter is not provided by the client.")
}

/**
 * Responds with the given [body].
 *
 * This method is identical to [ApplicationCall.respond] but verifies that the [body] type matches the one declared in the endpoint.
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         val user = authService.verifyLogIn(body.username, body.password)
 *         respond(user)
 *     }
 * }
 * ```
 *
 * ### Parameters
 *
 * - `body`: a value of the response type declared in the endpoint.
 * If the endpoint declared a response type of [Unit], or declared no response at all, this parameter is optional.
 * - `code`: the HTTP status code to respond with.
 * Defaults to [HttpStatusCode.NoContent] if the response type is [Unit] or if no response type is declared.
 * Defaults to [HttpStatusCode.OK] for any other value.
 */
@KtorDsl
suspend inline fun <reified Out : Any> TypedResponseScope<*, Out, *, *>.respond(body: Out, code: HttpStatusCode = if (body == Unit) HttpStatusCode.NoContent else HttpStatusCode.OK) {
	call.respond(status = code, message = body)
}

/**
 * Responds with the given [body].
 *
 * This method is identical to [ApplicationCall.respond] but verifies that the [body] type matches the one declared in the endpoint.
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         val user = authService.verifyLogIn(body.username, body.password)
 *         respond(user)
 *     }
 * }
 * ```
 *
 * ### Parameters
 *
 * - `body`: a value of the response type declared in the endpoint.
 * If the endpoint declared a response type of [Unit], or declared no response at all, this parameter is optional.
 * - `code`: the HTTP status code to respond with.
 * Defaults to [HttpStatusCode.NoContent] if the response type is [Unit] or if no response type is declared.
 * Defaults to [HttpStatusCode.OK] for any other value.
 */
@KtorDsl
suspend fun TypedResponseScope<*, Unit, *, *>.respond(code: HttpStatusCode = HttpStatusCode.NoContent) {
	respond(Unit, code)
}

/**
 * Fails the endpoint call with one of the declared [failures][opensavvy.spine.api.AnyEndpoint.Builder.failure].
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         if (body.password.isBlank()) {
 *             fail(InvalidPassword)
 *         }
 *     }
 * }
 * ```
 */
@KtorDsl
@JvmName("fail1")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<*, FailureSpec.ByCode<F>>, *>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

/**
 * Fails the endpoint call with one of the declared [failures][opensavvy.spine.api.AnyEndpoint.Builder.failure].
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         if (body.password.isBlank()) {
 *             fail(InvalidPassword)
 *         }
 *     }
 * }
 * ```
 */
@KtorDsl
@JvmName("fail2")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, *>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

/**
 * Fails the endpoint call with one of the declared [failures][opensavvy.spine.api.AnyEndpoint.Builder.failure].
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         if (body.password.isBlank()) {
 *             fail(InvalidPassword)
 *         }
 *     }
 * }
 * ```
 */
@KtorDsl
@JvmName("fail3")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, Nothing>, *>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

/**
 * Fails the endpoint call with one of the declared [failures][opensavvy.spine.api.AnyEndpoint.Builder.failure].
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         if (body.password.isBlank()) {
 *             fail(InvalidPassword)
 *         }
 *     }
 * }
 * ```
 */
@KtorDsl
@JvmName("fail4")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, Nothing>, Nothing>, Nothing>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.a.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

/**
 * Fails the endpoint call with one of the declared [failures][opensavvy.spine.api.AnyEndpoint.Builder.failure].
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         if (body.password.isBlank()) {
 *             fail(InvalidPassword)
 *         }
 *     }
 * }
 * ```
 */
@KtorDsl
@JvmName("fail5")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<Or<Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, Nothing>, Nothing>, Nothing>, Nothing>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.a.a.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

/**
 * Fails the endpoint call with one of the declared [failures][opensavvy.spine.api.AnyEndpoint.Builder.failure].
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     route(Api.Users.logIn) {
 *         if (body.password.isBlank()) {
 *             fail(InvalidPassword)
 *         }
 *     }
 * }
 * ```
 */
@KtorDsl
@JvmName("fail6")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<Or<Or<Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, Nothing>, Nothing>, Nothing>, Nothing>, Nothing>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.a.a.a.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

@PublishedApi
internal class TypedResponseScopeImpl<In : Any, Out : Any, Failure : FailureSpec, Params : Parameters>(
	override val call: ApplicationCall,
	override val endpoint: Endpoint<In, Out, Failure, Params>,
	override val body: In,
	override val parameters: Params,
) : TypedResponseScope<In, Out, Failure, Params>

@PublishedApi
internal class SpineShortCircuitException : CancellationException(
	"""
		An endpoint declared with Spine called the method 'fail'. 
		This exception is used to short-circuit the rest of the handler. It is expected behavior that shouldn't appear in logs.
		If you see this exception in your logs, it means one of:
		 - The exception somehow escaped its handler and bubbled up to Ktor. This should never happen. Please report a bug to Spine with a reproducer at https://gitlab.com/opensavvy/groundwork/spine/-/issues
		 - You have a try…catch that catches too many things. This exception inherits from CancellationException, which you should never catch! See https://betterprogramming.pub/the-silent-killer-thats-crashing-your-coroutines-9171d1e8f79b?gi=b4555a1271e9
	""".trimIndent()
)
