package opensavvy.spine.typed

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
	message = "The Endpoint class may go through source-incompatible changes in the future, even in minor releases. Read its documentation to learn more.",
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

	internal fun asBuilder(onCreate: (AnyEndpoint) -> Unit) = Builder(this, onCreate)

	@Suppress("DEPRECATION_ERROR")
	class Builder<In : Any, Out : Any, Params : Parameters> internal constructor(
		private val endpoint: Endpoint<In, Out, Params>,
		private val onCreate: (AnyEndpoint) -> Unit,
	) {

		fun <T : Any> request(kClass: KClass<T>) = Builder(
			Endpoint(endpoint.resource, endpoint.method, endpoint.path, kClass, endpoint.responseType, endpoint.buildParameters),
			onCreate
		)

		inline fun <reified T : Any> request() = request(T::class)

		fun <T : Any> response(kClass: KClass<T>) = Builder(
			Endpoint(endpoint.resource, endpoint.method, endpoint.path, endpoint.requestType, kClass, endpoint.buildParameters),
			onCreate
		)

		inline fun <reified T : Any> response() = response(T::class)

		fun <P : Parameters> parameters(build: (ParameterStorage) -> P) = Builder(
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
