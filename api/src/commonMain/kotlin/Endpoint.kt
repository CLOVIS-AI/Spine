package opensavvy.spine.api

import io.ktor.http.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * A specific HTTP [method] in a [resource].
 *
 * All instances of this interface must be immutable.
 *
 * ## Example
 *
 * Endpoints are declared in the body of a [resource][Resource], by calling one of the HTTP method names.
 *
 * ```kotlin
 * object User : DynamicResource<Users>("user", parent = Users) {
 *
 *     // GET …/{user}
 *     val get by get()
 *         .response<UserDto>()
 *
 *     // POST …/{user}
 *     val create by post()
 *         .request<UserCreationDto>()
 *         .response<UserDto>()
 *
 *     // PATCH …/{user}
 *     val edit by patch()
 *         .request<UserEditionDto>()
 *         .response<UserDto>()
 *
 *     // DELETE …/{user}
 *     val delete by delete()
 *
 * }
 * ```
 *
 * Configuration options on an endpoint during its creation are listed in [AnyEndpoint.Builder].
 *
 * ## The trick
 *
 * To represent endpoints in a type-safe manner, they must declare their information as type parameters.
 * For example, the [requestType] and [responseType] must appear in type parameters.
 *
 * However, adding a type parameter to a class in Kotlin is a source-incompatible change: all usages of a type must
 * explicitly say which type parameters are used.
 *
 * Because endpoints are the core of this library, and we expect to add more information to endpoints in the future,
 * we know that we will want to add new type parameters, which would break source compatibility for all users!
 *
 * To avoid this, the only part of the public API is `AnyEndpoint`, which does not declare the type parameters at all.
 * `AnyEndpoint` is therefore safe to use in user code. However, it lacks typing information (though the exact types
 * are still available as reflection entities in [requestType] and [responseType]). `AnyEndpoint` is a sealed interface
 * with a single implementation that is hidden in the library, `Endpoint`.
 *
 * When we declare an endpoint, we do not specify the type explicitly:
 * ```kotlin
 * val list by get()
 *     .response<List<UserDto>>()
 * ```
 *
 * Because we did not declare a type, Kotlin infers it to the _real type returned by the function, even though it is hidden_.
 * In fact, it infers the type to be:
 * ```kotlin
 * val list: Endpoint<Unit, UserDto, Parameters.Empty> by get()
 *     .response<List<UserDto>>()
 * ```
 * As you can see, all type parameters are indeed declared. You can see this by enabling [inlay hints in IntelliJ](https://www.jetbrains.com/help/idea/inlay-hints.html).
 *
 * However, if you try to write the type yourself, you will see that it will not compile, because `Endpoint` cannot be accessed.
 * This is an intended protection: you can create a value of type `Endpoint`, but you cannot write the type
 * `Endpoint` in your code, because the type `Endpoint` will change in source-incompatible ways in the future.
 * Writing a value of type `Endpoint` without the type appearance in your code is safe, so it is allowed.
 *
 * The type you are allowed to use is `AnyEndpoint`, this interface:
 * ```kotlin
 * val list: AnyEndpoint by get()
 *     .response<List<UserDto>>()
 * ```
 * However, this interface doesn't have type parameters, so this removes all type safety. As a consequence, if you do this,
 * none of the other functions of this library will compile for this endpoint, as it cannot be used safely.
 *
 * This interface is still useful because you may want to create operations that act on any endpoint without caring about
 * the type of a specific one. For example, you may create a function that accepts an endpoint and prints information
 * about it:
 * ```kotlin
 * fun AnyEndpoint.print() {
 *     println("$method $fullSlug")
 *     println("  - Input:  $requestType")
 *     println("  - Output: $responseType")
 * }
 * ```
 *
 * As a rule of thumb:
 * - Endpoints declaration should not have an explicit type declaration, and should instead rely on the inferred type.
 * - If you want to process endpoints, use `AnyEndpoint`.
 * - If you really absolutely must use type parameters, you can force access to `Endpoint` via a suppression.
 * Note, however, that this guarantees that your code will stop compiling in future versions of this library.
 */
sealed interface AnyEndpoint {
	val resource: Resource

	/**
	 * The [HttpMethod] used when invoking this [Endpoint][AnyEndpoint].
	 */
	val method: HttpMethod

	/**
	 * Optional path extension from the [Resource] this endpoint is a part of.
	 *
	 * To access the real path of this endpoint, see [ResolvedEndpoint.path].
	 */
	val path: Path.Segment?

	/**
	 * The type of the body went the client sends data to the server.
	 */
	val requestType: KClass<*>

	/**
	 * The type of the body when the server responds to a client.
	 */
	val responseType: KClass<*>

	/**
	 * Constructor for query parameters.
	 *
	 * If no query parameters are used by this endpoint, this function returns [Parameters.Empty].
	 */
	val buildParameters: ParameterConstructor<Parameters>

	/**
	 * The super-type for the endpoint declaration syntax.
	 *
	 * See [AnyEndpoint] to learn more about the syntax.
	 *
	 * This interface uses the same trick as [AnyEndpoint] to avoid source-incompatible breaking changes.
	 * See its documentation for details.
	 */
	sealed interface Builder {

		/**
		 * Declares the request body type.
		 *
		 * When a client makes a request to the server, the client will need to pass an instance of this type.
		 *
		 * Under the hood, this method uses [Ktor's content negotiation](https://ktor.io/docs/serialization.html) features.
		 * Therefore, all types that would be valid with content negotiation can be used with this library.
		 * Note that you may need to perform some configuration on the Ktor side before using some types, see the
		 * official documentation for instructions.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * val list by post()
		 *     .request<UserCreationDto>()
		 *     .response<UserDto>()
		 * ```
		 *
		 * If this method is called multiple times, only the last call is retained.
		 *
		 * @see response
		 */
		fun <T : Any> request(kClass: KClass<T>): Builder

		/**
		 * Declares the response body type.
		 *
		 * When a client makes a request to the server, the server will respond with an instance of this type.
		 *
		 * Under the hood, this method uses [Ktor's content negotiation](https://ktor.io/docs/serialization.html) features.
		 * Therefore, all types that would be valid with content negotiation can be used with this library.
		 * Note that you may need to perform some configuration on the Ktor side before using some types, see the
		 * official documentation for instructions.
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * val list by post()
		 *     .request<UserCreationDto>()
		 *     .response<UserDto>()
		 * ```
		 *
		 * If this method is called multiple times, only the last call is retained.
		 *
		 * @see request
		 */
		fun <T : Any> response(kClass: KClass<T>): Builder

		/**
		 * Declares query parameters that the client will need to provide to the server.
		 *
		 * To learn more about representing parameters, see [Parameters].
		 *
		 * ### Example
		 *
		 * ```kotlin
		 * // Create a type to hold the parameters
		 * class UserListParams(data: ParameterStorage) : Parameters(data) {
		 *    var onlyActive: Boolean by parameter(default = false)
		 *    var createdAfter: Instant? by parameter()
		 * }
		 *
		 * val list by get()
		 *     .parameters(::UserListParams)
		 *     .response<UserDto>()
		 * ```
		 */
		fun <P : Parameters> parameters(build: ParameterConstructor<P>): Builder
	}
}

/**
 * A callable path in a [resource] for a given [method].
 *
 * This class regularly undergoes backwards-incompatible changes.
 * It is marked as deprecated to avoid users of this library accidentally using it and thus breaking in the future
 * when this class changes.
 * To avoid breakage, use [AnyEndpoint] instead in your code (however, you will lose access to the exact types used).
 */
@Deprecated(
	message = "The Endpoint class may go through source-incompatible changes in the future, even in minor releases. Use AnyEndpoint instead.",
	level = DeprecationLevel.HIDDEN,
)
class Endpoint<In : Any, Out : Any, Params : Parameters> internal constructor(
	override val resource: Resource,
	override val method: HttpMethod,
	override val path: Path.Segment?,
	override val requestType: KClass<In>,
	override val responseType: KClass<Out>,
	override val buildParameters: ParameterConstructor<Params>,
) : AnyEndpoint {

	operator fun getValue(thisRef: Any?, property: KProperty<*>) = this

	override fun toString() = "$method $path • $requestType → $responseType"

	// region Builder

	@Suppress("DEPRECATION_ERROR")
	internal fun asBuilder(onCreate: (AnyEndpoint) -> Unit) = EndpointBuilder(this, onCreate)

	@Deprecated(
		message = "The EndpointBuilder class may go through source-incompatible changes in the future, even in minor releases. Use AnyEndpointBuilder instead.",
		level = DeprecationLevel.HIDDEN,
	)
	@Suppress("DEPRECATION_ERROR")
	class EndpointBuilder<In : Any, Out : Any, Params : Parameters> internal constructor(
		private val endpoint: Endpoint<In, Out, Params>,
		private val onCreate: (AnyEndpoint) -> Unit,
	) : AnyEndpoint.Builder {

		override fun <T : Any> request(kClass: KClass<T>) = EndpointBuilder(
			Endpoint(endpoint.resource, endpoint.method, endpoint.path, kClass, endpoint.responseType, endpoint.buildParameters),
			onCreate
		)

		inline fun <reified T : Any> request() = request(T::class)

		override fun <T : Any> response(kClass: KClass<T>) = EndpointBuilder(
			Endpoint(endpoint.resource, endpoint.method, endpoint.path, endpoint.requestType, kClass, endpoint.buildParameters),
			onCreate
		)

		inline fun <reified T : Any> response() = response(T::class)

		override fun <P : Parameters> parameters(build: (ParameterStorage) -> P) = EndpointBuilder(
			Endpoint(endpoint.resource, endpoint.method, endpoint.path, endpoint.requestType, endpoint.responseType, build),
			onCreate
		)

		fun create(): Endpoint<In, Out, Params> {
			onCreate(endpoint)
			return endpoint
		}

		operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) = create()
	}

	// endregion
}

/**
 * The complete URL for this endpoint, starting at its [RootResource].
 *
 * @see AnyEndpoint.path
 */
val AnyEndpoint.fullSlug: String
	get() =
		if (path == null) resource.fullSlug
		else resource.fullSlug + "/" + path!!.text
