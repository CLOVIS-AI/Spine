package opensavvy.spine.client.arrow

import arrow.core.raise.Raise
import io.ktor.client.statement.*
import opensavvy.spine.client.SpineResponse
import opensavvy.spine.client.bodyOrNull

// TODO in Kotlin 2.2: replace the 'raise' parameter by a context parameter
suspend inline fun <reified Out : Any> SpineResponse<Out>.body(raiseIn: Raise<HttpResponse>): Out =
	bodyOrNull() ?: raiseIn.raise(httpResponse)
