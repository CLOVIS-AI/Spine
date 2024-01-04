package opensavvy.spine.typed

import io.ktor.http.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

typealias AnyEndpoint = Endpoint<*>

class Endpoint<Types> internal constructor(
	val resource: Resource,
	val method: HttpMethod,
	val path: String,
	val types: Types,
) {

	init {
		@Suppress("DEPRECATION_ERROR")
		require(types is TypeWrapper<*, *, *>) { "An Endpoint instance was created with a type parameter that isn't of the special wrapper type: $types" }
	}

	operator fun getValue(thisRef: Any?, property: KProperty<*>) = this

	override fun toString() = buildString {
		append("$method $path • ")

		@Suppress("DEPRECATION_ERROR")
		if (types is TypeWrapper<*, *, *>)
			append("${types.requestType} → ${types.responseType}")
		else
			append("INVALID DATA TYPE")
	}

	// region Builder

	internal fun asBuilder(onCreate: (AnyEndpoint) -> Unit) = Builder(this, onCreate)

	class Builder<Types> internal constructor(
		private val endpoint: Endpoint<Types>,
		private val onCreate: (AnyEndpoint) -> Unit,
	) {

		@Suppress("MemberVisibilityCanBePrivate") // some people prefer calling '.build()' than the 'by' keyword magic
		fun build(): Endpoint<Types> {
			onCreate(endpoint)
			return endpoint
		}

		operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) = build()

		companion object {
			@Suppress("DEPRECATION_ERROR")
			fun <T : Any, In : Any, Out : Any, Params : Parameters> Builder<TypeWrapper<In, Out, Params>>.request(kClass: KClass<T>) = Builder(
				Endpoint(endpoint.resource, endpoint.method, endpoint.path, endpoint.types.withRequest(kClass)),
				onCreate
			)

			@Suppress("DEPRECATION_ERROR")
			inline fun <reified T : Any, In : Any, Out : Any, Params : Parameters> Builder<TypeWrapper<In, Out, Params>>.request() = request(T::class)

			@Suppress("DEPRECATION_ERROR")
			fun <T : Any, In : Any, Out : Any, Params : Parameters> Builder<TypeWrapper<In, Out, Params>>.response(kClass: KClass<T>) = Builder(
				Endpoint(endpoint.resource, endpoint.method, endpoint.path, endpoint.types.withResponse(kClass)),
				onCreate
			)

			@Suppress("DEPRECATION_ERROR")
			inline fun <reified T : Any, In : Any, Out : Any, Params : Parameters> Builder<TypeWrapper<In, Out, Params>>.response() = response(T::class)

			@Suppress("DEPRECATION_ERROR")
			fun <In : Any, Out : Any, Params : Parameters, P : Parameters> Builder<TypeWrapper<In, Out, Params>>.parameters(build: (ParameterStorage) -> P) = Builder(
				Endpoint(endpoint.resource, endpoint.method, endpoint.path, endpoint.types.withParameters(build)),
				onCreate
			)
		}
	}

	// endregion
	// region Data storage & type parameters

	/**
	 * Internal storage medium for data relating to an [Endpoint].
	 *
	 * This is a low-level detail of the library. Users of the library should use [Endpoint] instead.
	 * Using it in downstream projects is dangerous because it is likely that new type parameters will be added
	 * in the future to add more features to the project. We do not consider this class to be a part of the
	 * public API, and will thus make source-incompatible changes even in minor releases (however, we are honoring
	 * binary-compatibility).
	 *
	 * This type is designed to be used with inferred types and never be written explicitly in the codebase:
	 * ```kotlin
	 * val access by get()
	 *     .request<User>()
	 * ```
	 * In this example, the `access` variable is an [Endpoint] that has an instance of this class as type parameter.
	 * However, this class never appears in the code, guaranteeing the code will continue compiling in future versions.
	 */
	@Deprecated(
		message = "The TypeWrapper interface may go through source-incompatible changes in the future, even in minor releases. Read its documentation to learn more.",
		level = DeprecationLevel.HIDDEN,
	)
	class TypeWrapper<In : Any, Out : Any, Params : Parameters> internal constructor(
		internal val requestType: KClass<In>,
		internal val responseType: KClass<Out>,
		internal val buildParameters: (ParameterStorage) -> Params,
	) {

		@Suppress("DEPRECATION_ERROR")
		internal fun <T : Any> withRequest(kClass: KClass<T>) = TypeWrapper(kClass, responseType, buildParameters)

		@Suppress("DEPRECATION_ERROR")
		internal fun <T : Any> withResponse(kClass: KClass<T>) = TypeWrapper(requestType, kClass, buildParameters)

		@Suppress("DEPRECATION_ERROR")
		internal fun <P : Parameters> withParameters(builder: (ParameterStorage) -> P) = TypeWrapper(requestType, responseType, builder)

	}

	// endregion
}

@Suppress("DEPRECATION_ERROR")
val <In : Any, Out : Any, Params : Parameters> Endpoint<Endpoint.TypeWrapper<In, Out, Params>>.requestType get() = types.requestType

@Suppress("DEPRECATION_ERROR")
val <In : Any, Out : Any, Params : Parameters> Endpoint<Endpoint.TypeWrapper<In, Out, Params>>.responseType get() = types.responseType

@Suppress("DEPRECATION_ERROR")
val <In : Any, Out : Any, Params : Parameters> Endpoint<Endpoint.TypeWrapper<In, Out, Params>>.buildParameters get() = types.buildParameters
