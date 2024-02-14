package opensavvy.spine.typed

abstract class StaticResource<Parent : Resource>(
	slug: String,
	parent: Parent,
) : Resource(slug, parent) {

	init {
		// Static resources' slug must be a valid path segment, since they appear as-is in the URL
		Path.Segment(slug)
	}
}
