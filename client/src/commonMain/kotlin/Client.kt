@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import opensavvy.spine.api.Endpoint
import opensavvy.spine.api.FailureSpec
import opensavvy.spine.api.Parameters
import opensavvy.spine.api.ResolvedEndpoint

suspend inline fun <reified In : Any, reified Out : Any, reified Failure : FailureSpec, reified Params : Parameters> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<In, Out, Failure, Params>>,
	input: In,
	crossinline parameters: Params.() -> Unit,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out, Failure> {
	val result = request {
		method = endpoint.data.method
		url(endpoint.path.toString())

		for ((name, value) in endpoint.data.buildParameters(HashMap()).apply(parameters).data)
			parameter(name, value)

		contentType(contentType)
		setBody(input)

		configure()
	}

	return SpineResponse(result, endpoint.data.failureSpec)
}

// The above was the real implementation.
// However, most usage will be with the default body, etc.

suspend inline fun <reified Out : Any, reified Failure : FailureSpec, reified Params : Parameters> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<Unit, Out, Failure, Params>>,
	crossinline parameters: Params.() -> Unit,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out, Failure> = request(endpoint, Unit, parameters, contentType, configure)

suspend inline fun <reified In : Any, reified Out : Any, reified Failure : FailureSpec> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<In, Out, Failure, Parameters.Empty>>,
	input: In,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out, Failure> = request(endpoint, input, {}, contentType, configure)

suspend inline fun <reified Out : Any, reified Failure : FailureSpec> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<Unit, Out, Failure, Parameters.Empty>>,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out, Failure> = request(endpoint, Unit, {}, contentType, configure)
