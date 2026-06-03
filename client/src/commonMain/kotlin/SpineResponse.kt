package opensavvy.spine.client

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import opensavvy.spine.api.FailureSpec
import opensavvy.spine.api.FailureSpec.Never
import opensavvy.spine.api.FailureSpec.Or

/**
 * The type returned by [request], encapsulating an HTTP response.
 *
 * Most functionality on this type is provided by extensions.
 */
class SpineResponse<@Suppress("unused") Out : Any, @Suppress("unused") out Failure : FailureSpec>(
	/**
	 * Ktor's underlying [HttpResponse].
	 */
	val httpResponse: HttpResponse,

	/**
	 * The [FailureSpec] that was defined in the endpoint that was called.
	 */
	val failureSpec: Failure,
) {

	/**
	 * `true` if the HTTP status code is a success. `false` otherwise.
	 */
	val isSuccessful get() = httpResponse.status.isSuccess()
}

/**
 * Deserializes the response body, returning `null` if the response is not successful.
 *
 * The Ktor `ContentNegotation` plugin may be necessary for this function to work.
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.list).bodyOrNull()
 * ```
 *
 * @see bodyOrThrow Throw an exception on failure.
 * @see handle Handle failures exhaustively.
 */
suspend inline fun <reified Out : Any> SpineResponse<Out, *>.bodyOrNull(): Out? =
	if (isSuccessful) httpResponse.call.body()
	else null

/**
 * Deserializes the response body, throwing a [SpineReceptionException] if the response is not successful.
 *
 * The Ktor `ContentNegotation` plugin may be necessary for this function to work.
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.list).bodyOrThrow()
 * ```
 *
 * @see bodyOrNull Return `null` on failure.
 * @see handle Handle failures exhaustively.
 */
suspend inline fun <reified Out : Any> SpineResponse<Out, *>.bodyOrThrow(): Out =
	if (isSuccessful) httpResponse.call.body()
	else throw SpineReceptionException(httpResponse, httpResponse.bodyAsText())

/**
 * The exception thrown by [bodyOrThrow].
 */
@Suppress("CanBeParameter") // 'response' is unused in our code, but it's useful in user code
class SpineReceptionException(
	val response: HttpResponse,
	body: String,
) : RuntimeException("Received the HTTP code ${response.status} with body '$body'")

@PublishedApi
internal suspend inline fun <reified Out : Any, O> SpineResponse<Out, *>.handleFinal(
	transform: (Out) -> O,
) : O = transform(bodyOrThrow())

@PublishedApi
internal suspend inline fun <reified F, O> handleFailureUnchecked(
	response: SpineResponse<*, *>,
	failure: FailureSpec,
	handle: (F) -> O,
) : O? {
	require(failure is FailureSpec.ByCode<*>) { "Handling a failure can only be done by code for now. Attempted to handle the failure $failure of ${failure::class}" }

	if (response.httpResponse.status != failure.statusCode)
		// This failure does not correspond to the response, ignore it.
		return null

	return handle(response.httpResponse.body<F>())
}

/**
 * Exhaustively handle failures.
 *
 * If the request is succesful, [transform] is called.
 * If the request fails with the error [F1], [handle1] is called.
 *
 * To learn more about failures, see [the documentation](https://spine.opensavvy.dev/failures.html).
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.create).handle(
 *     handle1 = { println("Failed with $it") },
 * ) { println("Created: " + it) }
 * ```
 *
 * @see bodyOrNull Treat all failures as `null`.
 * @see bodyOrThrow Treat all failures as exceptions.
 */
suspend inline fun <reified Out : Any, O, reified F1> SpineResponse<Out, Or<Never, FailureSpec.ByCode<F1>>>.handle(
	handle1: (F1) -> O,
	transform: (Out) -> O,
) : O = handleFailureUnchecked(this, failureSpec.b, handle1)
	?: handleFinal(transform)

/**
 * Exhaustively handle failures.
 *
 * If the request is succesful, [transform] is called.
 * If the request fails with an error, the corresponding handler is called.
 *
 * To learn more about failures, see [the documentation](https://spine.opensavvy.dev/failures.html).
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.create).handle(
 *     handle1 = { println("Failed with $it") },
 * ) { println("Created: " + it) }
 * ```
 *
 * @see bodyOrNull Treat all failures as `null`.
 * @see bodyOrThrow Treat all failures as exceptions.
 */
suspend inline fun <reified Out : Any, O, reified F1, reified F2> SpineResponse<Out, Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>>.handle(
	handle1: (F1) -> O,
	handle2: (F2) -> O,
	transform: (Out) -> O,
) : O = handleFailureUnchecked(this, failureSpec.a.b, handle1)
	?: handleFailureUnchecked(this, failureSpec.b, handle2)
	?: handleFinal(transform)

/**
 * Exhaustively handle failures.
 *
 * If the request is succesful, [transform] is called.
 * If the request fails with an error, the corresponding handler is called.
 *
 * To learn more about failures, see [the documentation](https://spine.opensavvy.dev/failures.html).
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.create).handle(
 *     handle1 = { println("Failed with $it") },
 * ) { println("Created: " + it) }
 * ```
 *
 * @see bodyOrNull Treat all failures as `null`.
 * @see bodyOrThrow Treat all failures as exceptions.
 */
suspend inline fun <reified Out : Any, O, reified F1, reified F2, reified F3> SpineResponse<Out, Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>>.handle(
	handle1: (F1) -> O,
	handle2: (F2) -> O,
	handle3: (F3) -> O,
	transform: (Out) -> O,
) : O = handleFailureUnchecked(this, failureSpec.a.a.b, handle1)
	?: handleFailureUnchecked(this, failureSpec.a.b, handle2)
	?: handleFailureUnchecked(this, failureSpec.b, handle3)
	?: handleFinal(transform)

/**
 * Exhaustively handle failures.
 *
 * If the request is succesful, [transform] is called.
 * If the request fails with an error, the corresponding handler is called.
 *
 * To learn more about failures, see [the documentation](https://spine.opensavvy.dev/failures.html).
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.create).handle(
 *     handle1 = { println("Failed with $it") },
 * ) { println("Created: " + it) }
 * ```
 *
 * @see bodyOrNull Treat all failures as `null`.
 * @see bodyOrThrow Treat all failures as exceptions.
 */
suspend inline fun <reified Out : Any, O, reified F1, reified F2, reified F3, reified F4> SpineResponse<Out, Or<Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>, FailureSpec.ByCode<F4>>>.handle(
	handle1: (F1) -> O,
	handle2: (F2) -> O,
	handle3: (F3) -> O,
	handle4: (F4) -> O,
	transform: (Out) -> O,
) : O = handleFailureUnchecked(this, failureSpec.a.a.a.b, handle1)
	?: handleFailureUnchecked(this, failureSpec.a.a.b, handle2)
	?: handleFailureUnchecked(this, failureSpec.a.b, handle3)
	?: handleFailureUnchecked(this, failureSpec.b, handle4)
	?: handleFinal(transform)

/**
 * Exhaustively handle failures.
 *
 * If the request is succesful, [transform] is called.
 * If the request fails with an error, the corresponding handler is called.
 *
 * To learn more about failures, see [the documentation](https://spine.opensavvy.dev/failures.html).
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.create).handle(
 *     handle1 = { println("Failed with $it") },
 * ) { println("Created: " + it) }
 * ```
 *
 * @see bodyOrNull Treat all failures as `null`.
 * @see bodyOrThrow Treat all failures as exceptions.
 */
suspend inline fun <reified Out : Any, O, reified F1, reified F2, reified F3, reified F4, reified F5> SpineResponse<Out, Or<Or<Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>, FailureSpec.ByCode<F4>>, FailureSpec.ByCode<F5>>>.handle(
	handle1: (F1) -> O,
	handle2: (F2) -> O,
	handle3: (F3) -> O,
	handle4: (F4) -> O,
	handle5: (F5) -> O,
	transform: (Out) -> O,
) : O = handleFailureUnchecked(this, failureSpec.a.a.a.a.b, handle1)
	?: handleFailureUnchecked(this, failureSpec.a.a.a.b, handle2)
	?: handleFailureUnchecked(this, failureSpec.a.a.b, handle3)
	?: handleFailureUnchecked(this, failureSpec.a.b, handle4)
	?: handleFailureUnchecked(this, failureSpec.b, handle5)
	?: handleFinal(transform)

/**
 * Exhaustively handle failures.
 *
 * If the request is succesful, [transform] is called.
 * If the request fails with an error, the corresponding handler is called.
 *
 * To learn more about failures, see [the documentation](https://spine.opensavvy.dev/failures.html).
 *
 * ### Example
 *
 * ```kotlin
 * client.request(Api / Users / Users.create).handle(
 *     handle1 = { println("Failed with $it") },
 * ) { println("Created: " + it) }
 * ```
 *
 * @see bodyOrNull Treat all failures as `null`.
 * @see bodyOrThrow Treat all failures as exceptions.
 */
suspend inline fun <reified Out : Any, O, reified F1, reified F2, reified F3, reified F4, reified F5, reified F6> SpineResponse<Out, Or<Or<Or<Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>, FailureSpec.ByCode<F4>>, FailureSpec.ByCode<F5>>, FailureSpec.ByCode<F6>>>.handle(
	handle1: (F1) -> O,
	handle2: (F2) -> O,
	handle3: (F3) -> O,
	handle4: (F4) -> O,
	handle5: (F5) -> O,
	handle6: (F6) -> O,
	transform: (Out) -> O,
) : O = handleFailureUnchecked(this, failureSpec.a.a.a.a.a.b, handle1)
	?: handleFailureUnchecked(this, failureSpec.a.a.a.a.b, handle2)
	?: handleFailureUnchecked(this, failureSpec.a.a.a.b, handle3)
	?: handleFailureUnchecked(this, failureSpec.a.a.b, handle4)
	?: handleFailureUnchecked(this, failureSpec.a.b, handle5)
	?: handleFailureUnchecked(this, failureSpec.b, handle6)
	?: handleFinal(transform)
