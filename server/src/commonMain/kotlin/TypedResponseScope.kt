package opensavvy.spine.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import opensavvy.spine.api.DynamicResource
import opensavvy.spine.api.Parameters

interface TypedResponseScope<In : Any, Out : Any, Params : Parameters> {
	val call: ApplicationCall

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

suspend inline fun <reified Out : Any> TypedResponseScope<*, Out, *>.respond(body: Out, code: HttpStatusCode = if (body == Unit) HttpStatusCode.NoContent else HttpStatusCode.OK) {
	call.respond(status = code, message = body)
}

suspend fun TypedResponseScope<*, Unit, *>.respond(code: HttpStatusCode = HttpStatusCode.NoContent) {
	respond(Unit, code)
}

@PublishedApi
internal class TypedResponseScopeImpl<In : Any, Out : Any, Params : Parameters>(
	override val call: ApplicationCall,
	override val body: In,
	override val parameters: Params,
) : TypedResponseScope<In, Out, Params>
