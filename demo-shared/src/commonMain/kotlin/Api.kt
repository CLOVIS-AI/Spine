package opensavvy.spine.demo

import opensavvy.spine.typed.*

object Api : RootResource("v1") {

	object Users : StaticResource("users", Api) {

		val get by get()
			.parameters(UserDto::Params)
			.response<List<Id>>()

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

data class Id(val segments: List<String>)

class UserDto {

	object New

	object LogIn

	object Edit

	class Params(
		data: ParameterStorage,
	) : Parameters(data) {
		var archived by parameter(false)
	}
}

data class Foo(
	val username: String,
)
