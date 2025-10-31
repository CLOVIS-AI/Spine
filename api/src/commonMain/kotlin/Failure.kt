package opensavvy.spine.api

import io.ktor.http.*

/**
 * Declares the different ways an [endpoint][AnyEndpoint] can fail.
 *
 * An endpoint can declare zero or more ways in which it can fail:
 * ```kotlin
 * val create by patch()
 *     .request<UserModificationDto>()
 *     .failure<UserNotFound>(HttpStatusCode.NotFound)
 *     .failure<NotAllowed>(HttpStatusCode.Forbidden)
 * ```
 *
 * Each of these failures is user-defined and can carry arbitrary data.
 * Failures are declared in an endpoint with the [AnyEndpoint.Builder.failure] function.
 *
 * ### Declaring a failure by HTTP code
 *
 * Each endpoint declares its failures and which HTTP status code they each correspond to.
 * When the server throws that failure, it will be returned using that HTTP code.
 * When the client receives that HTTP code, it will attempt to deserialize that failure from the body.
 *
 * Any Kotlin object that Ktor can serialize can be used as a failure type.
 * ```kotlin
 * @Serializable
 * data class NotAllowed(val reason: String)
 *
 * val listUsers by get()
 *     .response<List<UserDto>>()
 *     .failure<NotAllowed>(HttpStatusCode.Forbidden)
 * ```
 *
 * However, since the client uses the status code to decide what to deserialize, it is not possible
 * to declare two different failures under the same status code. Instead, we can declare a single
 * failure represented by a `sealed class` or `sealed interface` and let KotlinX.Serialization decide
 * which is which:
 * ```kotlin
 * @Serializable
 * sealed class CannotProcessUserModification {
 *
 *     @Serializable
 *     @SerialName("InvalidUsername")
 *     data class InvalidUsername(val userId: String, val explain: String) : CannotProcessUserModification()
 *
 *     @Serializable
 *     @SerialName("InvalidAge")
 *     data class InvalidAge(val userId: String, val explain: String) : CannotProcessUserModification()
 * }
 *
 * val edit by patch()
 *     .request<UserModification>()
 *     .failure<CannotProcessUserModification>(HttpStatusCode.UnprocessableEntity)
 * ```
 *
 * With this approach, each endpoint that declares a particular failure must redeclare
 * which HTTP status code it corresponds to. At scale, this may cause inconsistencies where
 * the same failure is used for different status codes in different endpoints.
 * To avoid this, see [FailureCompanion].
 */
sealed interface FailureSpec {

	/**
	 * The no-operation [FailureSpec]: this failure type can never happen.
	 *
	 * This type exists to make representation with [Or] easier.
	 * It is not expected that end-users need to use this type, though you may see it appear in inlay hints.
	 *
	 * @see FailureSpec Learn more about failures.
	 */
	object Never : FailureSpec

	/**
	 * Represents a failure that is represented by a specific [statusCode].
	 *
	 * When the server throws that failure, it will be returned using that HTTP code.
	 * When the client receives that HTTP code, it will attempt to deserialize that failure from the body.
	 *
	 * Because of this, no two failures in the same endpoint can have the same [statusCode].
	 * To learn how to work around this, read [FailureSpec].
	 *
	 * @see FailureSpec Learn more about failures.
	 * @see FailureCompanion Easily implement [ByCode] for your companion objects.
	 */
	interface ByCode<out F> : FailureSpec {
		val statusCode: HttpStatusCode
	}

	/**
	 * Combines multiple failure specifications into a single one.
	 *
	 * This type is used to type-safely represent an endpoint that can failure in multiple different ways.
	 * It is not expected that end-users need to use this type, though you may see it appear in inlay hints.
	 */
	class Or<out A : FailureSpec, out B : FailureSpec>(
		val a: A,
		val b: B,
	) : FailureSpec
}

private fun FailureSpec.all(): Sequence<FailureSpec> = sequence {
	val self = this@all

	if (self is FailureSpec.Or<*, *>) {
		yieldAll(self.a.all())
		yieldAll(self.b.all())
	}

	yield(self)
}

/**
 * Helper to bind a [statusCode] to a given error class.
 *
 * For an introduction to failures, see [FailureSpec].
 *
 * ### Example
 *
 * ```kotlin
 * @Serializable
 * data class UserNotFound(
 *     val id: String,
 * ) {
 *
 *    companion object : FailureCompanion<UserNotFound>(HttpStatusCode.NotFound)
 * }
 *
 * // …An endpoint in a resources…
 * val getMe by get("me")
 *     .result<UserDto>()
 *     .failure(UserNotFound)
 * ```
 *
 * [FailureCompanion] is a helper to implement [FailureSpec.ByCode].
 * Just like [FailureSpec.ByCode], two failures in the same endpoint cannot share the same HTTP status code.
 * Read [FailureSpec] to learn why.
 *
 * To declare multiple failures with the same code on the same endpoint, use a `sealed class` or a `sealed interface`.
 * For example:
 * ```kotlin
 * @Serializable
 * sealed class CannotProcessUserModification {
 *
 *     @Serializable
 *     @SerialName("InvalidUsername")
 *     data class InvalidUsername(val userId: String, val explain: String) : CannotProcessUserModification()
 *
 *     @Serializable
 *     @SerialName("InvalidAge")
 *     data class InvalidAge(val userId: String, val explain: String) : CannotProcessUserModification()
 *
 *     companion object : FailureCompanion<CannotProcessUserModification>(HttpStatusCode.UnprocessableEntity)
 * }
 *
 * val edit by patch()
 *     .request<UserModification>()
 *     .failure(CannotProcessUserModification)
 * ```
 */
abstract class FailureCompanion<F>(
	override val statusCode: HttpStatusCode,
) : FailureSpec.ByCode<F>

internal data class FailureByCodeImpl<F>(
	override val statusCode: HttpStatusCode,
) : FailureSpec.ByCode<F> {

	override fun toString() = "Failure.ByCode($statusCode)"
}

internal operator fun <A : FailureSpec, B : FailureSpec> A.plus(b: B): FailureSpec.Or<A, B> {
	val newStatusCodes = b.all()
		.filterIsInstance<FailureSpec.ByCode<*>>()
		.mapTo(HashSet()) { it.statusCode }

	require(this.all().none { it is FailureSpec.ByCode<*> && it.statusCode in newStatusCodes }) {
		"""
			Two different failures cannot define the same HTTP status code because the client wouldn't know which one to deserialize.
			To declare different failure types for the same HTTP status code, used a sealed class or sealed interface. To learn more, view the documentation of FailureSpec.
			HTTP status codes that have been redefined: ${newStatusCodes intersect this.all().filterIsInstance<FailureSpec.ByCode<*>>().mapTo(HashSet()) { it.statusCode }}
		""".trimIndent()
	}

	return FailureSpec.Or(this, b)
}
