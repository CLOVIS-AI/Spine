package opensavvy.spine.api

import io.ktor.http.*

/**
 * Common parent for all resource types.
 *
 * Users of the library cannot directly subclass this.
 * Instead, they should subclass one of its subtypes.
 */
sealed class Resource(
	/**
	 * The URL segment relating to this specific resource.
	 *
	 * For [StaticResource] and [RootResource], it is a single string like `"v1"` or `"users"`.
	 *
	 * For [DynamicResource], it is a wildcard, like `"{user}"` or `"{id}"`.
	 *
	 * To get the complete URL of this resource, starting from the root resource, see [fullSlug].
	 */
	val slug: String,
) {

	/**
	 * The parent resource of this resource.
	 *
	 * Note that a [RootResource] has a `null` parent.
	 * In all other cases, this attribute is non-`null`.
	 *
	 * To follow the chain of parents, see [hierarchy].
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
			require(parent.slug != this.slug) { "This resource cannot have the same slug as one of its parents: '${this@Resource.slug}' is shared by $this and $parent" }
		}
	}

	/**
	 * Returns resources that are direct children of the current resource.
	 *
	 * Note that resources are registered when they are first initialized by the runtime, on first access.
	 * If nothing in the program refers to a specific resource, it is possible that it doesn't appear
	 * in this sequence, even if it should.
	 */
	val children: Sequence<Resource>
		get() = _children.asSequence()

	/**
	 * Returns all endpoints that are declared on this resource.
	 *
	 * Note that endpoints are typically declared during construction of the resource.
	 * When construction is not over yet, this sequence may be incomplete.
	 *
	 * To get all endpoints, including transitive children, see [endpoints].
	 */
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

/**
 * Returns the hierarchy of this resource: following the [parent][Resource.parent] chain.
 *
 * For example, if we declare a resource:
 * ```kotlin
 * object Api : RootResource("v1") {
 *     object Users : StaticResource("users") {
 *         object User : DynamicResource("user")
 *     }
 * }
 * ```
 * then the hierarchy of each of them is their path:
 * - `Api`: `[Api]`
 * - `Api.Users`: `[Api, Users]`
 * - `Api.Users.User`: `[Api, Users, User]`
 */
val Resource.hierarchy: Sequence<Resource>
	get() {
		val self = this
		return sequence { hierarchy(self) }
	}

/**
 * The complete URL of this resource, starting from the [RootResource], to this resource.
 *
 * For example, a [RootResource] may have a slug `"v1"`.
 *
 * A [StaticResource] usually has a slug like `"v1/users"`.
 *
 * A [DynamicResource] has a slug like `"v1/users/{user}"`.
 *
 * @see Resource.slug The segment of this specific resource.
 */
val Resource.fullSlug: String
	get() = hierarchy.map { it.slug }.joinToString("/")

/**
 * Returns all endpoints that are declared on this resource or any of its children.
 *
 * See [children][Resource.children] and [directEndpoints][Resource.directEndpoints] for more information
 * on initialization order and cases where this sequence may be incomplete.
 */
val Resource.endpoints: Sequence<AnyEndpoint>
	get() = directEndpoints + children.flatMap { it.endpoints }

internal fun Resource.extendPath(extension: String? = null) =
	if (extension != null) "$fullSlug/$extension"
	else fullSlug
