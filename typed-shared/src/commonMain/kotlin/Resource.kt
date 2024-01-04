package opensavvy.spine.typed

import io.ktor.http.*

abstract class Resource(
	val slug: String,
	val parent: Resource?,
) {

	private val _children = ArrayList<Resource>()
	private val _endpoints = ArrayList<AnyEndpoint>()

	init {
		if (parent != null) {
			// We do not access the object directly, so it is safe
			@Suppress("LeakingThis")
			parent._children += this
		}
	}

	val children: Sequence<Resource>
		get() = _children.asSequence()

	val directEndpoints: Sequence<AnyEndpoint>
		get() = _endpoints.asSequence()

	@Suppress("DEPRECATION_ERROR")
	private fun endpoint(method: HttpMethod, path: String? = null) = Endpoint(
		resource = this@Resource,
		method = method,
		path = extendPath(path),
		requestType = Unit::class,
		responseType = Unit::class,
		buildParameters = { Parameters.Empty },
	).asBuilder { _endpoints += it }

	protected fun get(path: String? = null) = endpoint(HttpMethod.Get, path)
	protected fun post(path: String? = null) = endpoint(HttpMethod.Post, path)
	protected fun put(path: String? = null) = endpoint(HttpMethod.Put, path)
	protected fun patch(path: String? = null) = endpoint(HttpMethod.Patch, path)
	protected fun delete(path: String? = null) = endpoint(HttpMethod.Delete, path)
	protected fun head(path: String? = null) = endpoint(HttpMethod.Head, path)
}

val Resource.pathSegments: Sequence<String>
	get() = sequence { generatePath(this@pathSegments) }

private suspend fun SequenceScope<String>.generatePath(self: Resource) {
	val parent = self.parent

	if (parent != null)
		generatePath(parent)

	yield(self.slug)
}

val Resource.path: String
	get() = pathSegments.joinToString("/")

val Resource.endpoints: Sequence<AnyEndpoint>
	get() = directEndpoints + children.flatMap { it.endpoints }

internal fun Resource.extendPath(extension: String? = null) =
	if (extension != null) "$path/$extension"
	else path
