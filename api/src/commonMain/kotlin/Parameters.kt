package opensavvy.spine.api

import kotlin.jvm.JvmInline
import kotlin.reflect.KProperty

typealias ParameterStorage = MutableMap<String, String>
typealias ParameterConstructor<P> = (ParameterStorage) -> P

/**
 * Additional parameters for [endpoints][Endpoint].
 *
 * Endpoints declare mandatory parameters, such as the identifier of the resource.
 * This abstract class allows to declare additional parameters in a type-safe manner.
 *
 * ### Usage
 *
 * In common code, declare a class that inherits from [Parameters], and use it to declare the name and type of the available parameters:
 * ```kotlin
 * class MyRequestParameters(data: ParameterStorage) : Parameters(data) {
 *     var param1: String? by parameter()
 *     var isSubscribed: Boolean by parameter("is_subscribed", default = false)
 * }
 * ```
 *
 * To create a new parameter bundle, use the helper [buildParameters] function:
 * ```kotlin
 * val example = buildParameters(::MyRequestParameters) {
 *     param1 = "value"
 *     isSubscribed = true
 * }
 * ```
 *
 * The values can be accessed in a type-safe manner:
 * ```kotlin
 * println(example.param1)
 * ```
 * The values can also be accessed via their string representation:
 * ```kotlin
 * println(example.data["param1"])
 * ```
 */
abstract class Parameters(
	/**
	 * Internal string representation of the parameters.
	 */
	val data: ParameterStorage = HashMap(),
) {

	/**
	 * Declares an optional parameter of type [T].
	 *
	 * If the parameter is missing, reading it will return [default] instead.
	 *
	 * The parameter is automatically named after the variable it is assigned to.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class ListUsers(data: ParameterStorage) : Parameters(data) {
	 *     var includeArchived by parameter(default = false)
	 * }
	 * ```
	 */
	protected fun <T : Any> parameter(default: T) = UnnamedParameter(default)

	/**
	 * Declares an optional parameter [name] of type [T].
	 *
	 * If the parameter is missing, reading it will return [default] instead.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class ListUsers(data: ParameterStorage) : Parameters(data) {
	 *     var includeArchived by parameter("include_archived", default = false)
	 * }
	 * ```
	 */
	protected fun <T : Any> parameter(name: String, default: T) = Parameter(name, default)

	/**
	 * Declares a parameter of type [T].
	 *
	 * If [T] is nullable, this function declares an optional parameter with a default value of `null`.
	 * If [T] is non-nullable, this function declares a mandatory parameter which will throw a [NoSuchElementException] if no value is provided.
	 *
	 * The parameter is automatically named after the variable it is assigned to.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class ListUsers(data: ParameterStorage) : Parameters(data) {
	 *     var includeArchived: String? by parameter()
	 * }
	 * ```
	 */
	protected fun <T : Any?> parameter() = UnnamedParameter<T>(null)

	/**
	 * Declares a parameter [name] of type [T].
	 *
	 * If [T] is nullable, this function declares an optional parameter with a default value of `null`.
	 * If [T] is non-nullable, this function declares a mandatory parameter which will throw a [NoSuchElementException] if no value is provided.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * class ListUsers(data: ParameterStorage) : Parameters(data) {
	 *     var includeArchived: String? by parameter("include_archived")
	 * }
	 * ```
	 */
	protected fun <T : Any?> parameter(name: String) = Parameter<T>(name, null)

	/**
	 * Internal type used by the parameter declaration syntax.
	 *
	 * See [Parameters].
	 */
	@JvmInline
	value class UnnamedParameter<T> internal constructor(val defaultValue: T?)

	/**
	 * A declared query parameter in an API schema.
	 *
	 * See [Parameters].
	 */
	class Parameter<T>(
		/**
		 * Name of the parameter as it appears in the URL.
		 */
		val name: String,

		/**
		 * The value that is substituted in if the URL does not contain this parameter.
		 */
		val defaultValue: T?,
	) {

		init {
			require(name.isNotBlank()) { "The name of a parameter cannot be empty: '$name'" }
			require(name.none { it.isWhitespace() }) { "The name of a parameter cannot contain whitespace: '$name'" }
		}
	}

	/**
	 * The default parameter instance.
	 *
	 * Use this instance for operations that take no parameters.
	 */
	object Empty : Parameters()
}

/**
 * Internal method used by the parameter declaration syntax.
 *
 * See [Parameters.parameter].
 */
inline operator fun <T> Parameters.UnnamedParameter<T>.provideDelegate(thisRef: Parameters, property: KProperty<*>) =
	Parameters.Parameter(property.name, this.defaultValue)

/**
 * Internal method used by the parameter declaration syntax.
 *
 * See [Parameters.parameter].
 */
inline operator fun <reified T> Parameters.Parameter<T>.getValue(thisRef: Parameters, property: KProperty<*>): T {
	val value = thisRef.data[name] ?: return run {
		if (defaultValue is T)
			defaultValue
		else
			throw NoSuchElementException("The parameter '${name}' is mandatory, but no value was provided.")
	}

	return when (T::class) {
		String::class -> value as T
		Boolean::class -> value.toBooleanStrict() as T
		Byte::class -> value.toByte() as T
		Short::class -> value.toShort() as T
		Int::class -> value.toInt() as T
		Long::class -> value.toLong() as T
		UByte::class -> value.toUByte() as T
		UShort::class -> value.toUShort() as T
		UInt::class -> value.toUInt() as T
		ULong::class -> value.toULong() as T
		Float::class -> value.toFloat() as T
		Double::class -> value.toDouble() as T
		else -> throw UnsupportedOperationException("The type ${T::class.simpleName ?: T::class.toString()} is not currently supported in parameters.")
	}
}

/**
 * Internal method used by the parameter declaration syntax.
 *
 * See [Parameters.parameter].
 */
inline operator fun <reified T> Parameters.Parameter<T>.setValue(thisRef: Parameters, property: KProperty<*>, value: T) {
	thisRef.data[name] = when (value) {
		null -> value.toString()
		is String -> value
		is Boolean -> value.toString()
		is Byte -> value.toString()
		is Short -> value.toString()
		is Int -> value.toString()
		is Long -> value.toString()
		is UByte -> value.toString()
		is UShort -> value.toString()
		is UInt -> value.toString()
		is ULong -> value.toString()
		is Float -> value.toString()
		is Double -> value.toString()
		else -> throw UnsupportedOperationException("The type ${T::class.simpleName ?: T::class.toString()} is not currently supported in parameters.")
	}
}

/**
 * Creates and configures a [Parameters] instance.
 *
 * ### Example
 *
 * ```kotlin
 * class Example(data: ParameterStorage) : Parameters(data) {
 *     var includeArchived by parameter(default = false)
 * }
 *
 * val example = buildParameters(::Example) {
 *     includeArchived = true
 * }
 * ```
 */
fun <P : Parameters> buildParameters(construct: ParameterConstructor<P>, block: P.() -> Unit): P {
	return construct(HashMap()).apply(block)
}
