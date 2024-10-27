package opensavvy.spine.api

import io.ktor.http.*

sealed class Resource(
	val slug: String,
) {

	/**
	 * The parent resource of this resource.
	 *
	 * Note that a [RootResource] has a `null` parent.
	 * In all other cases, this attribute is non-`null`.
	 *
	 * **Implementation note.**
	 * This attribute must be immutable and should always return the exact same instance.
	 */
	abstract val parent: Resource?

	private val _children = ArrayList<Resource>()
	private val _endpoints = ArrayList<AnyEndpoint>()

	init {
		// Mark the parent
		parent?.also {
			// We do not access the object directly, so it is safe
			@Suppress("LeakingThis")
			it._children += this
		}

		for (parent in hierarchy.filterNot { it == this }) {
			require(parent.slug != this.slug) { "This resource cannot have the same slug as one of its parents: '$slug' is shared by $this and $parent" }
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
		path = path?.let(Path::Segment),
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

private suspend fun SequenceScope<Resource>.hierarchy(self: Resource) {
	val parent = self.parent

	if (parent != null)
		hierarchy(parent)

	yield(self)
}

val Resource.hierarchy: Sequence<Resource>
	get() {
		val self = this
		return sequence { hierarchy(self) }
	}

val Resource.fullSlug: String
	get() = hierarchy.map { it.slug }.joinToString("/")

val Resource.endpoints: Sequence<AnyEndpoint>
	get() = directEndpoints + children.flatMap { it.endpoints }

internal fun Resource.extendPath(extension: String? = null) =
	if (extension != null) "$fullSlug/$extension"
	else fullSlug
