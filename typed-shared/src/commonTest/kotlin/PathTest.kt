package opensavvy.spine.typed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import opensavvy.prepared.suite.SuiteDsl

infix fun Addressed.shouldBeAddressedBy(path: String) = withClue("Expecting $this to be addressed by $path") {
	this.path.toString() shouldBe path
}

fun SuiteDsl.paths() = suite("Paths") {
	test("Valid segments") {
		Path.Segment("test").text shouldBe "test"
		Path.Segment("test-other2").text shouldBe "test-other2"
	}

	test("A segment shouldn't be empty") {
		shouldThrow<IllegalArgumentException> { Path.Segment("") }
	}

	test("A segment shouldn't contain one of the forbidden characters") {
		shouldThrow<IllegalArgumentException> { Path.Segment("other/test") }
	}

	test("Valid paths") {
		Path("api2", "users") shouldBeAddressedBy "/api2/users"
		Path("api2", "users", "123456789") shouldBeAddressedBy "/api2/users/123456789"
	}
}
