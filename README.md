# Spine: Typesafe HTTP APIs with Ktor and Arrow

OpenSavvy Spine is a library to declare typesafe endpoints in code shared between your multiplatform clients and servers.

## Typed Ktor

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

## Safe Ktor

Safe Ktor is a collection of DSLs to make error management in HTTP endpoints explicit, using Arrow.

## Spine

Spine is the combination of Typed and Safe Ktor.

## License

This project is licensed under the [Apache 2.0 license](LICENSE).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
- To learn more about our coding conventions and workflow, see the [OpenSavvy Wiki](https://gitlab.com/opensavvy/wiki/-/blob/main/README.md#wiki).
- This project is based on the [OpenSavvy Playground](docs/playground/README.md), a collection of preconfigured project templates.
