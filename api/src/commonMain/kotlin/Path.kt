package opensavvy.spine.api

import kotlin.jvm.JvmInline

/**
 * The path section in a URL.
 */
class Path(
	val segments: List<Segment>,
) : Iterable<Path.Segment> by segments, Addressed {

	/**
	 * Convenience constructor that builds a [Path] from a list of string [segments].
	 *
	 * Each element is validated and wrapped as a [Segment]. See [Segment] for constraints.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * // /api/v2/users/123/edit
	 * val p = Path("api", "v2", "users", "123", "edit")
	 * ```
	 */
	constructor(vararg segments: String) : this(segments.map(::Segment))

	/**
	 * Returns a new [Path] with [other] appended as the last segment.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * val p: Path = …
	 *
	 * val p2 = p + Segment("foo")
	 * ```
	 */
	operator fun plus(other: Segment): Path = Path(segments + other)

	/**
	 * Returns a new [Path] with [other] appended as the last segment.
	 *
	 * ### Example
	 *
	 * ```kotlin
	 * val p: Path = …
	 *
	 * val p2 = p + "foo"
	 * ```
	 */
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

	/**
	 * A single segment of a URL [Path].
	 *
	 * The [text] must be a non-empty string and must not contain the '/' character.
	 * Invalid values are rejected with an [IllegalArgumentException].
	 */
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
