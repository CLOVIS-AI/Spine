package opensavvy.spine.api

import io.ktor.http.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * A callable path in a [resource] for a given [method].
 *
 * The only implementation of this interface is [Endpoint].
 * However, [Endpoint] regularly undergoes backwards-incompatible changes.
 * For this reason, we discourage using [Endpoint] directly, and instead recommend using this interface instead.
 *
 * All instances of this interface must be immutable.
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
	val buildParameters: (ParameterStorage) -> Parameters

	sealed interface Builder {
		fun <T : Any> request(kClass: KClass<T>): Builder
		fun <T : Any> response(kClass: KClass<T>): Builder
		fun <P : Parameters> parameters(build: (ParameterStorage) -> P): Builder
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
	override val buildParameters: (ParameterStorage) -> Params,
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

val AnyEndpoint.fullSlug: String
	get() =
		if (path == null) resource.fullSlug
		else resource.fullSlug + "/" + path!!.text
