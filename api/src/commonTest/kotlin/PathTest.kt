package opensavvy.spine.api

import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows

infix fun Addressed.shouldBeAddressedBy(expectedPath: String) {
	check(this.path.toString() == expectedPath)
}

fun SuiteDsl.paths() = suite("Paths") {
	test("Valid segments") {
		check(Path.Segment("test").text == "test")
		check(Path.Segment("test-other2").text == "test-other2")
	}

	test("A segment shouldn't be empty") {
		checkThrows<IllegalArgumentException> { Path.Segment("") }
	}

	test("A segment shouldn't contain one of the forbidden characters") {
		checkThrows<IllegalArgumentException> { Path.Segment("other/test") }
	}

	test("Valid paths") {
		Path("api2", "users") shouldBeAddressedBy "/api2/users"
		Path("api2", "users", "123456789") shouldBeAddressedBy "/api2/users/123456789"
	}
}
