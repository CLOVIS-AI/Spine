package opensavvy.spine.api

/**
 * The root resource of an API.
 *
 * The root resource is a special kind of [StaticResource] that doesn't have a [parent].
 *
 * It is expected that users of the library use this class to define the root of their API:
 * ```kotlin
 * object Api : RootResource("v1") {
 *     object Users : StaticResource<Api>("/users", parent = Api)
 *     object Posts : StaticResource<Api>("/posts", parent = Api)
 * }
 * ```
 *
 * @constructor Creates a new [RootResource].
 * The passed [slug] should be used to differentiate between multiple APIs deployed on the same server.
 * For example, `"v1"` and `"v2"`.
 * To select the exact URL used by the server, client should use the [DefaultRequest plugin](https://ktor.io/docs/client-default-request.html) to specify a base URL.
 */
abstract class RootResource(
	slug: String,
) : Resource(slug), Addressed {

	init {
		// Static resources' slug must be a valid path segment, since they appear as-is in the URL
		Path.Segment(slug)
	}

	/**
	 * The parent of this resource. Since [RootResource] cannot have a parent, always returns `null`.
	 */
	final override val parent: Nothing?
		get() = null

	override val path: Path
		get() = Path(this@RootResource.slug)
}

/**
 * Constructs a [ResolvedResource] out of a [RootResource].
 *
 * See [ResolvedResource] to learn more.
 */
val <R : RootResource> R.resolved: ResolvedResource<R>
	get() = ResolvedResource(this, this.path)
