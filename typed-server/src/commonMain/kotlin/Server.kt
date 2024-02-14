@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.typed.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import opensavvy.spine.typed.Endpoint
import opensavvy.spine.typed.ParameterStorage
import opensavvy.spine.typed.Parameters
import opensavvy.spine.typed.fullSlug

@KtorDsl
inline fun <reified In : Any, reified Out : Any, reified Params : Parameters> Route.route(
	endpoint: Endpoint<In, Out, Params>,
	crossinline block: suspend TypedResponseScope<In, Params>.() -> Pair<HttpStatusCode, Out>,
) {
	route(endpoint.fullSlug, endpoint.method) {
		handle {
			val paramData: ParameterStorage = HashMap()
			for ((name, values) in call.parameters.entries())
				paramData[name] = values.first()

			val params = endpoint.buildParameters(paramData)

			val body: In = when {
				// If the expected type is Unit, don't even try to read the body.
				// Ktor doesn't like to read the body on requests that cannot have a body (GET, DELETE, OPTIONS).
				In::class == Unit::class -> Unit as In
				// For any other type, delegate to the ContentNegotiation plugin
				else -> call.receive()
			}

			val scope = TypedResponseScopeImpl(call, body, params)
			val (resultCode, result) = scope.block()

			call.respond(resultCode, result)
		}
	}
}
