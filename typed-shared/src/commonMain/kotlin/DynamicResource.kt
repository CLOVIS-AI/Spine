package opensavvy.spine.typed

abstract class DynamicResource<Parent : Resource>(
	slug: String,
	parent: Parent,
) : Resource("{$slug}", parent) {

	class Identified<Parent : Resource, Child : DynamicResource<Parent>> internal constructor(
		val id: Path.Segment,
		val self: Child,
	)
}

operator fun <Parent : Resource, Child : DynamicResource<Parent>> Child.invoke(id: String) =
	DynamicResource.Identified(Path.Segment(id), this)
