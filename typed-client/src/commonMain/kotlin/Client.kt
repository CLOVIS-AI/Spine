@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.typed.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import opensavvy.spine.typed.Endpoint
import opensavvy.spine.typed.Parameters
import opensavvy.spine.typed.Parameters.Empty.data
import opensavvy.spine.typed.ResolvedEndpoint

suspend inline fun <reified In : Any, reified Out : Any, reified Params : Parameters> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<In, Out, Params>>,
	input: In,
	crossinline parameters: Params.() -> Unit,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> {
	val result = request {
		method = endpoint.data.method
		url(endpoint.path.toString())

		for ((name, value) in endpoint.data.buildParameters(HashMap()).apply(parameters).data)
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
	endpoint: ResolvedEndpoint<Endpoint<Unit, Out, Params>>,
	crossinline parameters: Params.() -> Unit,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> = request(endpoint, Unit, parameters, contentType, configure)

suspend inline fun <reified In : Any, reified Out : Any> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<In, Out, Parameters.Empty>>,
	input: In,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> = request(endpoint, input, {}, contentType, configure)

suspend inline fun <reified Out : Any> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<Unit, Out, Parameters.Empty>>,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out> = request(endpoint, Unit, {}, contentType, configure)
