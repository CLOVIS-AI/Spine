@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.server

import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import opensavvy.spine.api.*

@KtorDsl
inline fun <reified In : Any, reified Out : Any, reified Failure : FailureSpec, reified Params : Parameters> Route.route(
	endpoint: Endpoint<In, Out, Failure, Params>,
	crossinline block: suspend TypedResponseScope<In, Out, Failure, Params>.() -> Unit,
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

			val scope = TypedResponseScopeImpl(call, endpoint, body, params)
			try {
				scope.block()
			} catch (_: SpineShortCircuitException) {
				// This is normal, nothing to do
			}
		}
	}
}
