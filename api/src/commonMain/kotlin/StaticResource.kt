package opensavvy.spine.api

/**
 * A resource with a hard-coded segment: `v1/users`, `v1/posts/favorites`.
 *
 * To declare a resource of this type, create a singleton:
 * ```kotlin
 * // URL: v1
 * object Api : RootResource("v1") {
 *
 *     // URL: v1/users
 *     object Users : StaticResource<Api>("users", parent = Api) {
 *
 *         // Endpoint: GET v1/users
 *         val list by get()
 *             .response<List<UserDto>>()
 *
 *     }
 *
 *     // URL: v1/posts
 *     object Posts : StaticResource<Api>("posts", parent = Api) {
 *
 *         // URL: v1/posts/favorites
 *         object Favorites : StaticResource<Posts>("favorites", parent = Posts) {
 *
 *             // Endpoint: GET v1/posts/favorites
 *             val all by get()
 *                 .response<List<PostDto>>()
 *
 *         }
 *     }
 * }
 * ```
 *
 * Note that static resources can be children of a [DynamicResource]. For example, `v1/users/{user}/key` is a static
 * resource `"key"` that is a child of the dynamic resource `"{user}"`, itself a child of the static resource `"users"`,
 * itself a child of the root resource `"v1"`.
 *
 * @see AnyEndpoint.Builder Declaring endpoints in a resource.
 * @param Parent The type of the direct parent of this resource.
 * Because of restrictions of the Kotlin language, it must be specified explicitly even if it already appears in the line
 * because it is passed to [parent].
 * @constructor Creates a new [StaticResource].
 * The passed [slug] should be a single word, which represents the hierarchy between this endpoint and its [parent].
 */
abstract class StaticResource<Parent : Resource>(
	slug: String,
	final override val parent: Parent,
) : Resource(slug) {

	init {
		// Static resources' slug must be a valid path segment, since they appear as-is in the URL
		Path.Segment(slug)
	}
}
