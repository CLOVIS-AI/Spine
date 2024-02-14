package opensavvy.spine.typed

/**
 * A resolved [Resource].
 *
 * The [Resource] class represents the _declaration_ of a resource.
 * For example, the `/api/users/{user}` is not a 'real' resource.
 * This class, [ResolvedResource], represents 'real' resources: '/api/users/111' and '/api/users/222' are possible
 * values of this class.
 *
 * To instantiate this class, specify the full path from the root, adding necessary runtime information where necessary.
 * For example:
 * ```kotlin
 * object Root : RootResource("api") {
 *     object Users : StaticResource<Root>("users", Root) {
 *         object User : DynamicResource<Users>("user", User)
 *     }
 * }
 *
 * // The root can be resolved with no further information, since it must be static:
 * println(Root.resolved)
 *
 * // Static routes can be resolved simply by specifying their path:
 * println(Root / Users)
 *
 * // Dynamic routes require specifying the dynamic segment during resolution:
 * println(Root / Users / User("123456789"))
 * ```
 */
class ResolvedResource<R : Resource> internal constructor(
	val resource: R,
	override val path: Path,
) : Addressed {

	override fun toString() = "$path (represented by $resource)"
}

operator fun <Current : Resource, Child : StaticResource<Current>> ResolvedResource<Current>.div(child: Child): ResolvedResource<Child> = ResolvedResource(child, path + child.slug)
operator fun <Current : Resource, Child : DynamicResource<Current>> ResolvedResource<Current>.div(child: DynamicResource.Identified<Current, Child>): ResolvedResource<Child> = ResolvedResource(child.self, path + child.id)

operator fun <Root : RootResource, Child : StaticResource<Root>> Root.div(child: Child) = this.resolved / child
operator fun <Root : RootResource, Child : DynamicResource<Root>> Root.div(child: DynamicResource.Identified<Root, Child>) = this.resolved / child
