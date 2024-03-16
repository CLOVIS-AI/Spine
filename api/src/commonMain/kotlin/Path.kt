package opensavvy.spine.api

import kotlin.jvm.JvmInline

/**
 * The path section in a URL.
 */
class Path(
	val segments: List<Segment>,
) : Iterable<Path.Segment> by segments, Addressed {

	constructor(vararg segments: String) : this(segments.map(::Segment))

	operator fun plus(other: Segment): Path = Path(segments + other)
	operator fun plus(other: String): Path = this + Segment(other)

	override val path: Path
		get() = this

	// region Equals & hashCode

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Path) return false

		if (segments != other.segments) return false

		return true
	}

	override fun hashCode(): Int {
		return segments.hashCode()
	}

	// endregion

	override fun toString() = segments.joinToString(separator = "/", prefix = "/")

	@JvmInline
	value class Segment(val text: String) {
		init {
			require(text.isNotEmpty()) { "Segments should not be empty: '$text'" }
			// TODO forbid all characters reserved in URL paths
			require('/' !in text) { "Segments cannot contain the character '/': '$text'" }
		}

		override fun toString() = text
	}
}

/**
 * An object that has a [Path].
 */
interface Addressed {
	val path: Path
}
