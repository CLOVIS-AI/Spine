# Module Server-side typesafe Spine schema usage

Implement a Ktor API described with type-safety in common code.

<a href="https://central.sonatype.com/artifact/dev.opensavvy.spine/server"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.spine/server.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/alpha/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.spine/server"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

Spine allows us to [declare Ktor endpoints in code shared between the client and server](https://spine.opensavvy.dev/setup.html).

## Declaring a fullstack endpoint

First, create a module that will contain the endpoint definition (see the `api` module), with the following API:

```kotlin
object Api : RootResource("v1") {
	object Users : StaticResource<Api>("users", Api) {
		val create by post()
			.request<CreateUserRequest>()
			.response<User>()
	}
}
```

Create a new module, with a dependency on `dev.opensavvy.spine:server`. Instantiate a Ktor server by following [our tutorial](https://spine.opensavvy.dev/setup.html#server-side-implementation). You can now declare the endpoint in [your existing `routing` block](https://ktor.io/docs/server-routing.html):

```kotlin
routing {
	route(Api.Users.create) {
		// The variable 'body' is automatically created with the correct type
		println("Creating a user: ${body.username}")

		// The 'respond' method expects the correct response type
		respond(User(username = body.username))
	}
}
```

## Learn more

- [`route`][opensavvy.spine.server.route] declares an endpoint to Ktor.
- [`body`][opensavvy.spine.server.TypedResponseScope.body] deserializes the request body type-safely.
- [`parameters`][opensavvy.spine.server.TypedResponseScope.parameters] provides access to query parameters.
- [`idOf`][opensavvy.spine.server.TypedResponseScope.idOf] provides access to path parameters.
- [`respond`][opensavvy.spine.server.respond] serializes the response type-safely.
- [`fail`][opensavvy.spine.server.fail] responds type-safely with one of the declared failures.
