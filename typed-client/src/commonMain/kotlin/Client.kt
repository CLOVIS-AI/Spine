@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.typed.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import opensavvy.spine.typed.Endpoint
import opensavvy.spine.typed.Parameters

suspend inline fun <reified In : Any, reified Out : Any, reified Params : Parameters> HttpClient.request(
	endpoint: Endpoint<In, Out, Params>,
	input: In,
	parameters: Params,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> {
	val result = request {
		method = endpoint.method
		url(endpoint.path)

		for ((name, value) in parameters.data)
			parameter(name, value)

		contentType(contentType)
		setBody(input)

		configure()
	}

	return SpineResponse(result)
}

// The above was the real implementation.
// However, most usage will be with the default body, etc.

suspend inline fun <reified Out : Any, reified Params : Parameters> HttpClient.request(
	endpoint: Endpoint<Unit, Out, Params>,
	parameters: Params,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> = request(endpoint, Unit, parameters, contentType, configure)

suspend inline fun <reified In : Any, reified Out : Any> HttpClient.request(
	endpoint: Endpoint<In, Out, Parameters.Empty>,
	input: In,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> = request(endpoint, input, Parameters.Empty, contentType, configure)

suspend inline fun <reified Out : Any> HttpClient.request(
	endpoint: Endpoint<Unit, Out, Parameters.Empty>,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> = request(endpoint, Unit, Parameters.Empty, contentType, configure)
