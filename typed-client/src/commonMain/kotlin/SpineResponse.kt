package opensavvy.spine.typed.client

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.jvm.JvmInline

@JvmInline
value class SpineResponse<@Suppress("unused") Out : Any>(
	val httpResponse: HttpResponse,
) {

	val isSuccessful get() = httpResponse.status.isSuccess()
}

suspend inline fun <reified Out : Any> SpineResponse<Out>.bodyOrNull(): Out? =
	if (isSuccessful) httpResponse.call.body()
	else null

suspend inline fun <reified Out : Any> SpineResponse<Out>.bodyOrThrow(): Out =
	if (isSuccessful) httpResponse.call.body()
	else throw SpineReceptionException(httpResponse, httpResponse.bodyAsText())

class SpineReceptionException(
	val response: HttpResponse,
	body: String,
) : RuntimeException("Received the HTTP code ${response.status} with body '$body'")
