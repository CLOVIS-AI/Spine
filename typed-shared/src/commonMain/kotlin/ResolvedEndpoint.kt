package opensavvy.spine.typed

/**
 * A resolved [Endpoint][AnyEndpoint].
 *
 * The [AnyEndpoint] interface represents the _declaration_ of an endpoint.
 * For example, `GET /api/users/{user}` is not a 'real' endpoint.
 * This class, [ResolvedEndpoint], represents 'real' endpoints: 'GET /api/users/111' and 'GET /api/users/222' are possible
 * values of this class.
 *
 * To resolve an endpoint, start by [resolving its resource][ResolvedResource] then follow with the endpoint:
 * ```kotlin
 * println(Root / Users / User("999") / User.get)
 * ```
 */
class ResolvedEndpoint<E : AnyEndpoint> internal constructor(
	val resource: Resource,
	val data: E,
	override val path: Path,
) : Addressed

operator fun <R : Resource, Endpoint : AnyEndpoint> ResolvedResource<R>.div(endpoint: Endpoint) = ResolvedEndpoint(
	resource,
	endpoint,
	if (endpoint.path == null) path
	else path + endpoint.path!!
)
