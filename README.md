# Spine: Typesafe HTTP APIs with Ktor and Arrow

OpenSavvy Spine is a library to declare typesafe endpoints in code shared between your multiplatform clients and servers.

Declare your endpoints and DTOs in your common code:

```kotlin
// First, declare your DTOs:
// Here, we use KotlinX.Serialization.
// Any serialization library supported by Ktor is supported by this project. 
@Serializable
data class User(
	val name: String,
	val active: Boolean,
)

// Next, declare your API endpoints:
object Api : RootResource("api") {
	object Users : StaticResource("users", parent = Api) {

		class ListParameters(data: ParameterStorage) : Parameters(data) {
			var includeInactive by parameter(default = true)
		}

		val list by get()
			.parameters(::ListParameters)
			.response<User>()

		val create by post()
			.request<User>()
			.response<User>()

	}
}
```

Then, implement your routes on the server:

```kotlin
routing {
	route(Api.Users.list) {
		HttpStatusCode.OK to userRepository.list(includeInactive = parameters.includeInactive)
	}

	route(Api.Users.create) {
		val result = userRepository.create(body)
		HttpStatusCode.OK to result
	}
}
```

Finally, call the routes from your client:

```kotlin
val user = User("Test", active = true)
client.request(Api.Users.create, user).isSuccessful() shouldBe true
client.request(Api.Users.list).bodyOrNull() shouldContain user
```

## License

This project is licensed under the [Apache 2.0 license](LICENSE).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
- To learn more about our coding conventions and workflow, see the [OpenSavvy website](https://opensavvy.dev/open-source/index.html).
- This project is based on the [OpenSavvy Playground](docs/playground/README.md), a collection of preconfigured project templates.

If you don't want to clone this project on your machine, it is also available using [DevContainer](https://containers.dev/) (open in [VS Code](https://code.visualstudio.com/docs/devcontainers/containers) • [IntelliJ & JetBrains IDEs](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html)). Don't hesitate to create issues if you have problems getting the project up and running.
