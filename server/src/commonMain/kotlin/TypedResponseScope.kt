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

@KtorDsl
interface TypedResponseScope<out In : Any, out Out : Any, out Failure : FailureSpec, out Params : Parameters> {
	val call: ApplicationCall

	val endpoint: Endpoint<out In, out Out, out Failure, out Params>

	val body: In

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

@KtorDsl
suspend inline fun <reified Out : Any> TypedResponseScope<*, Out, *, *>.respond(body: Out, code: HttpStatusCode = if (body == Unit) HttpStatusCode.NoContent else HttpStatusCode.OK) {
	call.respond(status = code, message = body)
}

@KtorDsl
suspend fun TypedResponseScope<*, Unit, *, *>.respond(code: HttpStatusCode = HttpStatusCode.NoContent) {
	respond(Unit, code)
}

@KtorDsl
@JvmName("fail1")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<*, FailureSpec.ByCode<F>>, *>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

@KtorDsl
@JvmName("fail2")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, *>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

@KtorDsl
@JvmName("fail3")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, Nothing>, *>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

@KtorDsl
@JvmName("fail4")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, Nothing>, Nothing>, Nothing>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.a.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

@KtorDsl
@JvmName("fail5")
suspend inline fun <reified F : Any> TypedResponseScope<*, *, Or<Or<Or<Or<Or<*, FailureSpec.ByCode<F>>, Nothing>, Nothing>, Nothing>, Nothing>, Nothing>.fail(failure: F): Nothing {
	val spec = endpoint.failureSpec.a.a.a.a.b
	call.respond(status = spec.statusCode, message = failure)
	throw SpineShortCircuitException()
}

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
