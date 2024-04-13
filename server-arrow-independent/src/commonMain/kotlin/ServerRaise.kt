package opensavvy.spine.server.arrow.independent

import arrow.core.raise.Raise
import arrow.core.raise.recover
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

/**
 * Allows to use the [Raise] DSL to implement a route.
 *
 * ### Example
 *
 * ```kotlin
 * routing {
 *     get("/ping") {
 *         raise {
 *             val value = call.parameters["value"]
 *             ensureNotNull(value) { HttpFailure("Missing value", HttpStatusCode.UnprocessableEntity) }
 *
 *             call.respond("Pong: $value")
 *         }
 *     }
 * }
 * ```
 */
@KtorDsl
suspend inline fun PipelineContext<Unit, ApplicationCall>.raise(block: Raise<HttpFailure>.() -> Unit) {
	recover(block) {
		call.respond(status = it.code, message = it.body, messageType = it.type)
	}
}
