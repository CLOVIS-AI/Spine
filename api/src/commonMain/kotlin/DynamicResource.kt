package opensavvy.spine.api

/**
 * A resource with a wildcard segment: `v1/users/{user}`, `v1/posts/{post}/subscribers/{user}`.
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
 *         // URL: v1/users/{user}
 *         object User : DynamicResource<Users>("user", parent = Users) {
 *
 *             // Endpoint: GET v1/users/{user}
 *             val get by get()
 *                 .response<UserDto>()
 *         }
 *     }
 * }
 * ```
 *
 * @see AnyEndpoint.Builder Declaring endpoints in a resource.
 * @param Parent The type of the direct parent of this resource.
 * Because of restrictions of the Kotlin language, it must be specified explicitly even if it already appears in the line
 * because it is passed to [parent].
 * @constructor Creates a new [DynamicResource].
 * The passed [slug] should be a single word, which is the name of the wildcard added to the [parent]'s URL: for example,
 * `"user"` or `"id"`. When such a resource is imported into Ktor, it recognizes that it is a wildcard. The exact value
 * can be accessed on the server using the `idOf` function.
 */
abstract class DynamicResource<Parent : Resource>(
	slug: String,
	final override val parent: Parent,
) : Resource("{$slug}") {

	/**
	 * Holder for a [DynamicResource] and a specific [slug] that matches its declared wildcard.
	 *
	 * This class is rarely used directly; it is used internally to construct instances of [ResolvedResource].
	 * See [DynamicResource.invoke].
	 */
	class Identified<Parent : Resource, Self : DynamicResource<Parent>> internal constructor(
		val slug: Path.Segment,
		val resource: Self,
	)
}

/**
 * Binds a specific identifier into a [DynamicResource]'s [slug][DynamicResource.slug].
 *
 * This operator is part of the syntax for constructing instances of [ResolvedResource].
 *
 * ```kotlin
 * object Api : RootResource("v1") {
 *     object Users : StaticResource<Api>("users", Api) {
 *         object User : DynamicResource<Users>("user", Users) {
 *             object Favorites : StaticResource<User>("favorites", User) {
 *                 object Favorite : DynamicResource<Favorites>("favorite", Favorites)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * To refer to the above resources:
 *
 * | Desired path                    | Kotlin code                                                   |
 * |:--------------------------------|:--------------------------------------------------------------|
 * | `"v1"`                          | `Api.resolved` (the root resource is special, see [resolved]) |
 * | `"v1/users"`                    | `Api / Users`                                                 |
 * | `"v1/users/1234"`               | `Api / Users / User("1234")`                                  |
 * | `"v1/users/1234/favorites"`     | `Api / Users / User("1234") / Favorites`                      |
 * | `"v1/users/1234/favorites/789"` | `Api / Users / User("1234") / Favorites / Favorite("789")`    |
 */
operator fun <Parent : Resource, Child : DynamicResource<Parent>> Child.invoke(id: String) =
	DynamicResource.Identified(Path.Segment(id), this)
