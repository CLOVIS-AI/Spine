package opensavvy.spine.api

abstract class StaticResource<Parent : Resource>(
	slug: String,
	final override val parent: Parent,
) : Resource(slug) {

	init {
		// Static resources' slug must be a valid path segment, since they appear as-is in the URL
		Path.Segment(slug)
	}
}
