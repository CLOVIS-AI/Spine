package opensavvy.spine.demo.server

import io.ktor.http.*
import io.ktor.server.routing.*
import opensavvy.spine.demo.Api
import opensavvy.spine.demo.UserDto
import opensavvy.spine.typed.server.route

fun Route.users() {
	route(Api.Users.get) {
		println("The client requested users that are archived: ${parameters.archived}")
		HttpStatusCode.OK to emptyList()
	}

	route(Api.Users.create) {
		println("Received a new user: $body")
		HttpStatusCode.Created to Unit
	}

	route(Api.Users.logIn) {
		HttpStatusCode.OK to Unit
	}

	route(Api.Users.User.get) {
		HttpStatusCode.OK to UserDto()
	}

	route(Api.Users.User.edit) {
		HttpStatusCode.OK to Unit
	}
}
