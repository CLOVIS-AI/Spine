package opensavvy.spine.api

abstract class RootResource(
	slug: String,
) : Resource(slug), Addressed {

	init {
		// Static resources' slug must be a valid path segment, since they appear as-is in the URL
		Path.Segment(slug)
	}

	/**
	 * The parent of this resource. Since [RootResource] cannot have a parent, always returns `null`.
	 */
	final override val parent: Nothing?
		get() = null

	override val path: Path
		get() = Path(slug)
}

val <R : RootResource> R.resolved: ResolvedResource<R>
	get() = ResolvedResource(this, this.path)
