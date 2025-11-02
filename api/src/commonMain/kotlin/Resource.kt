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
		failureSpec = FailureSpec.Never,
	).asBuilder { _endpoints += it }

	/**
	 * Creates a [`GET`][HttpMethod.Get] HTTP endpoint in this resource.
	 *
	 * `GET` endpoints are used to access information. They should not modify the state of any resources.
	 *
	 * ### Properties
	 *
	 * - SHOULD NOT declare a [request][AnyEndpoint.Builder.request] body
	 * - should declare a [response][AnyEndpoint.Builder.response] body
	 * - should be [safe](https://developer.mozilla.org/en-US/docs/Glossary/Safe/HTTP)
	 * - should be [idempotent](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent)
	 * - should be [cacheable](https://developer.mozilla.org/en-US/docs/Glossary/Cacheable)
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * object User : DynamicResource<Users>("user", parent = Users) {
	 *
	 *     // GET …/{user}
	 *     val get by get()
	 *         .response<UserDto>()
	 *
	 *     // GET …/{user}/favorites
	 *     val favorites by get("favorites")
	 *         .response<UserFavoriteDto>()
	 *
	 * }
	 * ```
	 *
	 * To learn more about what can be customized on an endpoint, see [AnyEndpoint.Builder].
	 *
	 * Learn more about [`GET` (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/GET).
	 */
	protected fun get(path: String? = null) = endpoint(HttpMethod.Get, path)

	/**
	 * Creates a [`POST`][HttpMethod.Post] HTTP endpoint in this resource.
	 *
	 * `POST` endpoints create new entities.
	 *
	 * Each new request must create a new entity, even if it is identical to a
	 * prior request. If your endpoint only creates a new entity on the very first request, use [put] instead.
	 *
	 * ### Properties
	 *
	 * - should declare a [request][AnyEndpoint.Builder.request] body
	 * - should declare a [response][AnyEndpoint.Builder.response] body
	 *
	 * Additionally, `POST` endpoints:
	 * - are not [safe](https://developer.mozilla.org/en-US/docs/Glossary/Safe/HTTP), as they modify the server's state
	 * - are not [idempotent](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent), as the same request executed
	 * twice will create two entities
	 * - are not [cacheable](https://developer.mozilla.org/en-US/docs/Glossary/Cacheable), as a new entity must be created each time
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * object User : DynamicResource<Users>("user", parent = Users) {
	 *
	 *     // POST …/{user}
	 *     val create by post()
	 *         .request<UserCreationDto>()
	 *         .response<UserDto>()
	 *
	 * }
	 * ```
	 *
	 * To learn more about what can be customized on an endpoint, see [AnyEndpoint.Builder].
	 *
	 * Learn more about [`POST` (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST).
	 */
	protected fun post(path: String? = null) = endpoint(HttpMethod.Post, path)

	/**
	 * Creates a [`PUT`][HttpMethod.Put] HTTP endpoint in this resource.
	 *
	 * `PUT` endpoints set the state of an entity.
	 *
	 * If the entity does not yet exist, it is created (upsert behavior).
	 *
	 * If the same request is executed multiple times, and the state of the entity hasn't changed in the meantime,
	 * the second request should not do anything.
	 * To instead create a new entity each time, use [post].
	 *
	 * ### Properties
	 *
	 * - should declare a [request][AnyEndpoint.Builder.request] body
	 * - should declare a [response][AnyEndpoint.Builder.response] body
	 *
	 * Additionally, `PUT` endpoints:
	 * - are not [safe](https://developer.mozilla.org/en-US/docs/Glossary/Safe/HTTP), as they modify the server's state
	 * - are [idempotent](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent), as the same request executed
	 * twice will only create one entity
	 * - are not [cacheable](https://developer.mozilla.org/en-US/docs/Glossary/Cacheable)
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * object User : DynamicResource<Users>("user", parent = Users) {
	 *
	 *     // PUT …/{user}
	 *     val update by put()
	 *         .response<UserDto>()
	 *
	 *     // PUT …/{user}/favorites
	 *     val addFavorite by put("favorites")
	 *         .request<UserFavoriteId>()
	 *         .response<UserFavoriteDto>()
	 *
	 * }
	 * ```
	 *
	 * To learn more about what can be customized on an endpoint, see [AnyEndpoint.Builder].
	 *
	 * Learn more about [`PUT` (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PUT).
	 */
	protected fun put(path: String? = null) = endpoint(HttpMethod.Put, path)

	/**
	 * Creates a [`PATCH`][HttpMethod.Patch] HTTP endpoint in this resource.
	 *
	 * `PATCH` endpoints partially set the state of an entity.
	 *
	 * Typically, a `PATCH` endpoint takes as input the same data as the matching `GET` outputs,
	 * but with all fields optional, interpreting missing fields as "keep the existing value".
	 *
	 * ### Properties
	 *
	 * - should declare a [request][AnyEndpoint.Builder.request] body
	 * - may or may not declare a [response][AnyEndpoint.Builder.response] body
	 *
	 * Additionally, `PATCH` endpoints:
	 * - are not [safe](https://developer.mozilla.org/en-US/docs/Glossary/Safe/HTTP), as they modify the server's state
	 * - are not [idempotent](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent)
	 * - are not [cacheable](https://developer.mozilla.org/en-US/docs/Glossary/Cacheable)
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * object User : DynamicResource<Users>("user", parent = Users) {
	 *
	 *     // PATCH …/{user}
	 *     val update by patch()
	 *         .response<UserDto>()
	 *
	 *     // PATCH …/{user}/favorites
	 *     val favorites by patch("favorites")
	 *         .request<UserFavoriteId>()
	 *         .response<UserFavoriteDto>()
	 *
	 * }
	 * ```
	 *
	 * To learn more about what can be customized on an endpoint, see [AnyEndpoint.Builder].
	 *
	 * Learn more about [`PATCH` (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/PATCH).
	 */
	protected fun patch(path: String? = null) = endpoint(HttpMethod.Patch, path)

	/**
	 * Creates a [`DELETE`][HttpMethod.Delete] HTTP endpoint in this resource.
	 *
	 * `DELETE` endpoints delete an entity.
	 *
	 * ### Properties
	 *
	 * - may or may not declare a [request][AnyEndpoint.Builder.request] body
	 * - may or may not declare a [response][AnyEndpoint.Builder.response] body
	 *
	 * Additionally, `DELETE` endpoints:
	 * - are not [safe](https://developer.mozilla.org/en-US/docs/Glossary/Safe/HTTP), as they modify the server's state
	 * - are [idempotent](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent)
	 * - are not [cacheable](https://developer.mozilla.org/en-US/docs/Glossary/Cacheable)
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * object User : DynamicResource<Users>("user", parent = Users) {
	 *
	 *     // DELETE …/{user}
	 *     val delete by delete()
	 *
	 *     // DELETE …/{user}/favorites
	 *     val favorites by delete("favorites")
	 *
	 * }
	 * ```
	 *
	 * To learn more about what can be customized on an endpoint, see [AnyEndpoint.Builder].
	 *
	 * Learn more about [`DELETE` (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/DELETE).
	 */
	protected fun delete(path: String? = null) = endpoint(HttpMethod.Delete, path)

	/**
	 * Creates a [`HEAD`][HttpMethod.Head] HTTP endpoint in this resource.
	 *
	 * To learn more about what can be customized on an endpoint, see [AnyEndpoint.Builder].
	 *
	 * Learn more about [`HEAD` (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/HEAD).
	 */
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
@Suppress("KDocUnresolvedReference") // `[Api]` is not a link, it's just text
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
