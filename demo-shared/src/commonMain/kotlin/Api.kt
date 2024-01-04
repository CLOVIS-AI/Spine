package opensavvy.spine.demo

import opensavvy.spine.typed.*
import opensavvy.spine.typed.Endpoint.*
import opensavvy.spine.typed.Endpoint.Builder.Companion.parameters
import opensavvy.spine.typed.Endpoint.Builder.Companion.request
import opensavvy.spine.typed.Endpoint.Builder.Companion.response
import opensavvy.spine.typed.paths.Id

object Api : StaticResource("v1", parent = null) {

	object Users : StaticResource("users", Api) {

		val get by get()
			.parameters(UserDto::Params)
			.response<List<Id<User>>, _, _, _>()

		val create by post()
			.request<UserDto.New, _, _, _>()

		val logIn by post("/token")
			.request<UserDto.LogIn, _, _, _>()

		object User : DynamicResource("user", Users) {

			val get by get()
				.response<UserDto, _, _, _>()

			val edit by patch()
				.request<UserDto.Edit, _, _, _>()

		}
	}
}

class UserDto {

	object New

	object LogIn

	object Edit

	class Params(
		data: ParameterStorage,
	) : Parameters(data) {
		var archived by parameter("archived", false)
	}
}

data class Foo(
	val username: String,
)
