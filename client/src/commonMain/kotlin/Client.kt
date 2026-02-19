@file:Suppress("DEPRECATION_ERROR")

package opensavvy.spine.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import opensavvy.spine.api.Endpoint
import opensavvy.spine.api.FailureSpec
import opensavvy.spine.api.Parameters
import opensavvy.spine.api.ResolvedEndpoint

/**
 * Invokes a [Ktor typesafe endpoint][ResolvedEndpoint].
 *
 * If the following API has been declared:
 * ```kotlin
 * object Api : RootResource("v1") {
 *     object Users : StaticResource<Api>("users", Api) {
 *         val list by get()
 *             .response<List<User>>()
 *
 *         val create by post()
 *             .request<UserCreation>()
 *             .response<User>()
 *
 *         object User : DynamicResource<Users>("user", Users) {
 *             val get by get()
 *                  .response<User>()
 *         }
 *     }
 * }
 * ```
 *
 * An [HttpClient] can be used to call it:
 *
 * ```kotlin
 * // List users:
 * client.request(Api / Users / Users.list).bodyOrThrow()
 *
 * // Create a user:
 * client.request(Api / Users / Users.create, UserCreation("John", 15)).bodyOrThrow()
 *
 * // Access a specific user:
 * client.request(Api / Users / User("123456") / User.get).bodyOrThrow()
 * ```
 *
 * For this example to work, you will need to configure the [HttpClient]'s `DefaultRequest` and `ContentNegotiation` plugin.
 * To do so, please follow [our tutorial](https://spine.opensavvy.dev/setup.html#client-side-implementation).
 *
 * @see bodyOrNull Access the body, returning `null` on failure.
 * @see bodyOrThrow Access the body, throwing an exception on failure.
 * @see handle Exhaustively handle declared failures.
 */
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

		for ((name, values) in endpoint.data.buildParameters(HashMap()).apply(parameters).data)
			for (value in values)
				parameter(name, value)

		contentType(contentType)
		setBody(input)

		configure()
	}

	return SpineResponse(result, endpoint.data.failureSpec)
}

// The above was the real implementation.
// However, most usage will be with the default body, etc.

/**
 * Invokes a [Ktor typesafe endpoint][ResolvedEndpoint].
 *
 * If the following API has been declared:
 * ```kotlin
 * object Api : RootResource("v1") {
 *     object Users : StaticResource<Api>("users", Api) {
 *         val list by get()
 *             .response<List<User>>()
 *
 *         val create by post()
 *             .request<UserCreation>()
 *             .response<User>()
 *
 *         object User : DynamicResource<Users>("user", Users) {
 *             val get by get()
 *                  .response<User>()
 *         }
 *     }
 * }
 * ```
 *
 * An [HttpClient] can be used to call it:
 *
 * ```kotlin
 * // List users:
 * client.request(Api / Users / Users.list).bodyOrThrow()
 *
 * // Create a user:
 * client.request(Api / Users / Users.create, UserCreation("John", 15)).bodyOrThrow()
 *
 * // Access a specific user:
 * client.request(Api / Users / User("123456") / User.get).bodyOrThrow()
 * ```
 *
 * For this example to work, you will need to configure the [HttpClient]'s `DefaultRequest` and `ContentNegotiation` plugin.
 * To do so, please follow [our tutorial](https://spine.opensavvy.dev/setup.html#client-side-implementation).
 *
 * @see bodyOrNull Access the body, returning `null` on failure.
 * @see bodyOrThrow Access the body, throwing an exception on failure.
 * @see handle Exhaustively handle declared failures.
 */
suspend inline fun <reified Out : Any, reified Failure : FailureSpec, reified Params : Parameters> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<Unit, Out, Failure, Params>>,
	crossinline parameters: Params.() -> Unit,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out, Failure> = request(endpoint, Unit, parameters, contentType, configure)

/**
 * Invokes a [Ktor typesafe endpoint][ResolvedEndpoint].
 *
 * If the following API has been declared:
 * ```kotlin
 * object Api : RootResource("v1") {
 *     object Users : StaticResource<Api>("users", Api) {
 *         val list by get()
 *             .response<List<User>>()
 *
 *         val create by post()
 *             .request<UserCreation>()
 *             .response<User>()
 *
 *         object User : DynamicResource<Users>("user", Users) {
 *             val get by get()
 *                  .response<User>()
 *         }
 *     }
 * }
 * ```
 *
 * An [HttpClient] can be used to call it:
 *
 * ```kotlin
 * // List users:
 * client.request(Api / Users / Users.list).bodyOrThrow()
 *
 * // Create a user:
 * client.request(Api / Users / Users.create, UserCreation("John", 15)).bodyOrThrow()
 *
 * // Access a specific user:
 * client.request(Api / Users / User("123456") / User.get).bodyOrThrow()
 * ```
 *
 * For this example to work, you will need to configure the [HttpClient]'s `DefaultRequest` and `ContentNegotiation` plugin.
 * To do so, please follow [our tutorial](https://spine.opensavvy.dev/setup.html#client-side-implementation).
 *
 * @see bodyOrNull Access the body, returning `null` on failure.
 * @see bodyOrThrow Access the body, throwing an exception on failure.
 * @see handle Exhaustively handle declared failures.
 */
suspend inline fun <reified In : Any, reified Out : Any, reified Failure : FailureSpec> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<In, Out, Failure, Parameters.Empty>>,
	input: In,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out, Failure> = request(endpoint, input, {}, contentType, configure)

/**
 * Invokes a [Ktor typesafe endpoint][ResolvedEndpoint].
 *
 * If the following API has been declared:
 * ```kotlin
 * object Api : RootResource("v1") {
 *     object Users : StaticResource<Api>("users", Api) {
 *         val list by get()
 *             .response<List<User>>()
 *
 *         val create by post()
 *             .request<UserCreation>()
 *             .response<User>()
 *
 *         object User : DynamicResource<Users>("user", Users) {
 *             val get by get()
 *                  .response<User>()
 *         }
 *     }
 * }
 * ```
 *
 * An [HttpClient] can be used to call it:
 *
 * ```kotlin
 * // List users:
 * client.request(Api / Users / Users.list).bodyOrThrow()
 *
 * // Create a user:
 * client.request(Api / Users / Users.create, UserCreation("John", 15)).bodyOrThrow()
 *
 * // Access a specific user:
 * client.request(Api / Users / User("123456") / User.get).bodyOrThrow()
 * ```
 *
 * For this example to work, you will need to configure the [HttpClient]'s `DefaultRequest` and `ContentNegotiation` plugin.
 * To do so, please follow [our tutorial](https://spine.opensavvy.dev/setup.html#client-side-implementation).
 *
 * @see bodyOrNull Access the body, returning `null` on failure.
 * @see bodyOrThrow Access the body, throwing an exception on failure.
 * @see handle Exhaustively handle declared failures.
 */
suspend inline fun <reified Out : Any, reified Failure : FailureSpec> HttpClient.request(
	endpoint: ResolvedEndpoint<Endpoint<Unit, Out, Failure, Parameters.Empty>>,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configure: HttpRequestBuilder.() -> Unit = {},
): SpineResponse<Out, Failure> = request(endpoint, Unit, {}, contentType, configure)
