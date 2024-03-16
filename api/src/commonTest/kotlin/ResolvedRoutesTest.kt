package opensavvy.spine.api

import opensavvy.prepared.suite.SuiteDsl
import opensavvy.spine.api.Root.Departments
import opensavvy.spine.api.Root.Departments.Department
import opensavvy.spine.api.Root.Users
import opensavvy.spine.api.Root.Users.User
import opensavvy.spine.api.Root.Users.User.Departments as UserDepartments
import opensavvy.spine.api.Root.Users.User.Departments.Department as UserDepartment

fun SuiteDsl.resolvedRouteTests() = suite("Resolved resource tests") {
	test("Simple static routes") {
		Root shouldBeAddressedBy "/api"
		Root / Users shouldBeAddressedBy "/api/users"
		Root / Departments shouldBeAddressedBy "/api/departments"
	}

	test("Simple dynamic routes") {
		Root / Users / User("test") shouldBeAddressedBy "/api/users/test"
		Root / Departments / Department("x777") shouldBeAddressedBy "/api/departments/x777"
	}

	test("Complex routes") {
		Root / Users / User("999") / UserDepartments / UserDepartment("111") shouldBeAddressedBy "/api/users/999/departments/111"
	}

	test("Resolve endpoints") {
		Root / Users / Users.create shouldBeAddressedBy "/api/users"
		Root / Users / User.preferences shouldBeAddressedBy "/api/users/preferences"
	}
}
