package opensavvy.spine.typed

import io.ktor.http.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

typealias AnyEndpoint = Endpoint<*, *, *>

class Endpoint<In : Any, Out : Any, Params : Parameters> internal constructor(
	val resource: Resource,
	val method: HttpMethod,
	val path: String,
	val requestType: KClass<In>,
	val responseType: KClass<Out>,
	val buildParameters: (ParameterStorage) -> Params,
) {

	operator fun getValue(thisRef: Any?, property: KProperty<*>) = this

	override fun toString() = "$method $path • $requestType → $responseType"

	// region Builder

	internal fun asBuilder(onCreate: (AnyEndpoint) -> Unit) = Builder(this, onCreate)

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
