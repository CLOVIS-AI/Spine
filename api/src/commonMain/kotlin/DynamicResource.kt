package opensavvy.spine.api

abstract class DynamicResource<Parent : Resource>(
	slug: String,
	final override val parent: Parent,
) : Resource("{$slug}") {

	class Identified<Parent : Resource, Child : DynamicResource<Parent>> internal constructor(
		val id: Path.Segment,
		val self: Child,
	)
}

operator fun <Parent : Resource, Child : DynamicResource<Parent>> Child.invoke(id: String) =
	DynamicResource.Identified(Path.Segment(id), this)
