package opensavvy.spine.demo

import opensavvy.spine.typed.DynamicResource
import opensavvy.spine.typed.ParameterStorage
import opensavvy.spine.typed.Parameters
import opensavvy.spine.typed.StaticResource
import opensavvy.spine.typed.paths.Id

object Api : StaticResource("v1", parent = null) {

	object Users : StaticResource("users", Api) {

		val get by get()
			.parameters(UserDto::Params)
			.response<List<Id<User>>>()

		val create by post()
			.request<UserDto.New>()

		val logIn by post("/token")
			.request<UserDto.LogIn>()

		object User : DynamicResource("user", Users) {

			val get by get()
				.response<UserDto>()

			val edit by patch()
				.request<UserDto.Edit>()

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
