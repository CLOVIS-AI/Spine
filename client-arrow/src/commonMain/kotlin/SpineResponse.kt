package opensavvy.spine.client.arrow

import arrow.core.raise.Raise
import opensavvy.spine.api.AnyEndpoint
import opensavvy.spine.api.FailureSpec
import opensavvy.spine.api.FailureSpec.Never
import opensavvy.spine.api.FailureSpec.Or
import opensavvy.spine.client.SpineResponse
import opensavvy.spine.client.bodyOrThrow
import opensavvy.spine.client.handle

/**
 * Returns the response body.
 *
 * This overload is inferred when the [endpoint][AnyEndpoint] was declared without any possible failure types.
 *
 * To declare a failure type, see [failure][AnyEndpoint.Builder.failure] or the [failures tutorial](https://spine.opensavvy.dev/failures.html).
 */
suspend inline fun <reified Out : Any> SpineResponse<Out, Never>.body(): Out =
	bodyOrThrow()

/**
 * Returns the response body of an endpoint that declares one or more [failures][AnyEndpoint.Builder.failure].
 *
 * If any of the declared failures happen, they are raised into the corresponding [Raise] context parameter.
 *
 * ### Example
 *
 * For the following API:
 *
 * ```kotlin
 * object Users : RootResource("users") {
 *
 *     object User : DynamicResource<Users>("user", Users) {
 *
 *         val get by get()
 *             .response<UserDto>()
 *             .failure<NotFound>(HttpStatusCode.NotFound)
 *             .failure<NotAllowedToQueryUsers>(HttpStatusCode.Forbidden)
 *     }
 *
 *     val me by get()
 *         .response<UserDto>()
 *         .failure<NotLoggedIn>(HttpStatusCode.Unauthorized)
 * }
 * ```
 *
 * We can request the different endpoints as so:
 *
 * ```kotlin
 * val client = HttpClient()
 * // …configure your HttpClient…
 *
 * context(_: Raise<NotLoggedIn>)
 * suspend fun HttpClient.me(): UserDto =
 *     request(Users / Users.me).body()
 *
 * context(_: Raise<NotFound>, _: Raise<NotAllowedToQueryUsers>)
 * suspend fun HttpClient.getUser(id: String): UserDto =
 *     request(Users / User(id) / User.get).body()
 * ```
 */
context(raise1: Raise<F1>)
suspend inline fun <reified Out : Any, reified F1> SpineResponse<Out, Or<Never, FailureSpec.ByCode<F1>>>.body(): Out = handle(
	handle1 = { raise1.raise(it) },
	transform = { bodyOrThrow() },
)

/**
 * Returns the response body of an endpoint that declares one or more [failures][AnyEndpoint.Builder.failure].
 *
 * If any of the declared failures happen, they are raised into the corresponding [Raise] context parameter.
 *
 * ### Example
 *
 * For the following API:
 *
 * ```kotlin
 * object Users : RootResource("users") {
 *
 *     object User : DynamicResource<Users>("user", Users) {
 *
 *         val get by get()
 *             .response<UserDto>()
 *             .failure<NotFound>(HttpStatusCode.NotFound)
 *             .failure<NotAllowedToQueryUsers>(HttpStatusCode.Forbidden)
 *     }
 *
 *     val me by get()
 *         .response<UserDto>()
 *         .failure<NotLoggedIn>(HttpStatusCode.Unauthorized)
 * }
 * ```
 *
 * We can request the different endpoints as so:
 *
 * ```kotlin
 * val client = HttpClient()
 * // …configure your HttpClient…
 *
 * context(_: Raise<NotLoggedIn>)
 * suspend fun HttpClient.me(): UserDto =
 *     request(Users / Users.me).body()
 *
 * context(_: Raise<NotFound>, _: Raise<NotAllowedToQueryUsers>)
 * suspend fun HttpClient.getUser(id: String): UserDto =
 *     request(Users / User(id) / User.get).body()
 * ```
 */
context(raise1: Raise<F1>, raise2: Raise<F2>)
suspend inline fun <reified Out : Any, reified F1, reified F2> SpineResponse<Out, Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>>.body(): Out = handle(
	handle1 = { raise1.raise(it) },
	handle2 = { raise2.raise(it) },
	transform = { bodyOrThrow() },
)

/**
 * Returns the response body of an endpoint that declares one or more [failures][AnyEndpoint.Builder.failure].
 *
 * If any of the declared failures happen, they are raised into the corresponding [Raise] context parameter.
 *
 * ### Example
 *
 * For the following API:
 *
 * ```kotlin
 * object Users : RootResource("users") {
 *
 *     object User : DynamicResource<Users>("user", Users) {
 *
 *         val get by get()
 *             .response<UserDto>()
 *             .failure<NotFound>(HttpStatusCode.NotFound)
 *             .failure<NotAllowedToQueryUsers>(HttpStatusCode.Forbidden)
 *     }
 *
 *     val me by get()
 *         .response<UserDto>()
 *         .failure<NotLoggedIn>(HttpStatusCode.Unauthorized)
 * }
 * ```
 *
 * We can request the different endpoints as so:
 *
 * ```kotlin
 * val client = HttpClient()
 * // …configure your HttpClient…
 *
 * context(_: Raise<NotLoggedIn>)
 * suspend fun HttpClient.me(): UserDto =
 *     request(Users / Users.me).body()
 *
 * context(_: Raise<NotFound>, _: Raise<NotAllowedToQueryUsers>)
 * suspend fun HttpClient.getUser(id: String): UserDto =
 *     request(Users / User(id) / User.get).body()
 * ```
 */
context(raise1: Raise<F1>, raise2: Raise<F2>, raise3: Raise<F3>)
suspend inline fun <reified Out : Any, reified F1, reified F2, reified F3> SpineResponse<Out, Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>>.body(): Out = handle(
	handle1 = { raise1.raise(it) },
	handle2 = { raise2.raise(it) },
	handle3 = { raise3.raise(it) },
	transform = { bodyOrThrow() },
)

/**
 * Returns the response body of an endpoint that declares one or more [failures][AnyEndpoint.Builder.failure].
 *
 * If any of the declared failures happen, they are raised into the corresponding [Raise] context parameter.
 *
 * ### Example
 *
 * For the following API:
 *
 * ```kotlin
 * object Users : RootResource("users") {
 *
 *     object User : DynamicResource<Users>("user", Users) {
 *
 *         val get by get()
 *             .response<UserDto>()
 *             .failure<NotFound>(HttpStatusCode.NotFound)
 *             .failure<NotAllowedToQueryUsers>(HttpStatusCode.Forbidden)
 *     }
 *
 *     val me by get()
 *         .response<UserDto>()
 *         .failure<NotLoggedIn>(HttpStatusCode.Unauthorized)
 * }
 * ```
 *
 * We can request the different endpoints as so:
 *
 * ```kotlin
 * val client = HttpClient()
 * // …configure your HttpClient…
 *
 * context(_: Raise<NotLoggedIn>)
 * suspend fun HttpClient.me(): UserDto =
 *     request(Users / Users.me).body()
 *
 * context(_: Raise<NotFound>, _: Raise<NotAllowedToQueryUsers>)
 * suspend fun HttpClient.getUser(id: String): UserDto =
 *     request(Users / User(id) / User.get).body()
 * ```
 */
context(raise1: Raise<F1>, raise2: Raise<F2>, raise3: Raise<F3>, raise4: Raise<F4>)
suspend inline fun <reified Out : Any, reified F1, reified F2, reified F3, reified F4> SpineResponse<Out, Or<Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>, FailureSpec.ByCode<F4>>>.body(): Out = handle(
	handle1 = { raise1.raise(it) },
	handle2 = { raise2.raise(it) },
	handle3 = { raise3.raise(it) },
	handle4 = { raise4.raise(it) },
	transform = { bodyOrThrow() },
)

/**
 * Returns the response body of an endpoint that declares one or more [failures][AnyEndpoint.Builder.failure].
 *
 * If any of the declared failures happen, they are raised into the corresponding [Raise] context parameter.
 *
 * ### Example
 *
 * For the following API:
 *
 * ```kotlin
 * object Users : RootResource("users") {
 *
 *     object User : DynamicResource<Users>("user", Users) {
 *
 *         val get by get()
 *             .response<UserDto>()
 *             .failure<NotFound>(HttpStatusCode.NotFound)
 *             .failure<NotAllowedToQueryUsers>(HttpStatusCode.Forbidden)
 *     }
 *
 *     val me by get()
 *         .response<UserDto>()
 *         .failure<NotLoggedIn>(HttpStatusCode.Unauthorized)
 * }
 * ```
 *
 * We can request the different endpoints as so:
 *
 * ```kotlin
 * val client = HttpClient()
 * // …configure your HttpClient…
 *
 * context(_: Raise<NotLoggedIn>)
 * suspend fun HttpClient.me(): UserDto =
 *     request(Users / Users.me).body()
 *
 * context(_: Raise<NotFound>, _: Raise<NotAllowedToQueryUsers>)
 * suspend fun HttpClient.getUser(id: String): UserDto =
 *     request(Users / User(id) / User.get).body()
 * ```
 */
context(raise1: Raise<F1>, raise2: Raise<F2>, raise3: Raise<F3>, raise4: Raise<F4>, raise5: Raise<F5>)
suspend inline fun <reified Out : Any, reified F1, reified F2, reified F3, reified F4, reified F5> SpineResponse<Out, Or<Or<Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>, FailureSpec.ByCode<F4>>, FailureSpec.ByCode<F5>>>.body(): Out = handle(
	handle1 = { raise1.raise(it) },
	handle2 = { raise2.raise(it) },
	handle3 = { raise3.raise(it) },
	handle4 = { raise4.raise(it) },
	handle5 = { raise5.raise(it) },
	transform = { bodyOrThrow() },
)

/**
 * Returns the response body of an endpoint that declares one or more [failures][AnyEndpoint.Builder.failure].
 *
 * If any of the declared failures happen, they are raised into the corresponding [Raise] context parameter.
 *
 * ### Example
 *
 * For the following API:
 *
 * ```kotlin
 * object Users : RootResource("users") {
 *
 *     object User : DynamicResource<Users>("user", Users) {
 *
 *         val get by get()
 *             .response<UserDto>()
 *             .failure<NotFound>(HttpStatusCode.NotFound)
 *             .failure<NotAllowedToQueryUsers>(HttpStatusCode.Forbidden)
 *     }
 *
 *     val me by get()
 *         .response<UserDto>()
 *         .failure<NotLoggedIn>(HttpStatusCode.Unauthorized)
 * }
 * ```
 *
 * We can request the different endpoints as so:
 *
 * ```kotlin
 * val client = HttpClient()
 * // …configure your HttpClient…
 *
 * context(_: Raise<NotLoggedIn>)
 * suspend fun HttpClient.me(): UserDto =
 *     request(Users / Users.me).body()
 *
 * context(_: Raise<NotFound>, _: Raise<NotAllowedToQueryUsers>)
 * suspend fun HttpClient.getUser(id: String): UserDto =
 *     request(Users / User(id) / User.get).body()
 * ```
 */
context(raise1: Raise<F1>, raise2: Raise<F2>, raise3: Raise<F3>, raise4: Raise<F4>, raise5: Raise<F5>, raise6: Raise<F6>)
suspend inline fun <reified Out : Any, reified F1, reified F2, reified F3, reified F4, reified F5, reified F6> SpineResponse<Out, Or<Or<Or<Or<Or<Or<Never, FailureSpec.ByCode<F1>>, FailureSpec.ByCode<F2>>, FailureSpec.ByCode<F3>>, FailureSpec.ByCode<F4>>, FailureSpec.ByCode<F5>>, FailureSpec.ByCode<F6>>>.body(): Out = handle(
	handle1 = { raise1.raise(it) },
	handle2 = { raise2.raise(it) },
	handle3 = { raise3.raise(it) },
	handle4 = { raise4.raise(it) },
	handle5 = { raise5.raise(it) },
	handle6 = { raise6.raise(it) },
	transform = { bodyOrThrow() },
)
