package opensavvy.spine.typed

abstract class RootResource(
	slug: String,
) : Resource(slug, parent = null) {

	init {
		// Static resources' slug must be a valid path segment, since they appear as-is in the URL
		Path.Segment(slug)
	}
}
