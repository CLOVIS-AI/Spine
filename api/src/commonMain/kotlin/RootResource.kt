package opensavvy.spine.api

abstract class RootResource(
	slug: String,
) : Resource(slug, parent = null), Addressed {

	init {
		// Static resources' slug must be a valid path segment, since they appear as-is in the URL
		Path.Segment(slug)
	}

	override val path: Path
		get() = Path(slug)
}

val <R : RootResource> R.resolved: ResolvedResource<R>
	get() = ResolvedResource(this, this.path)
