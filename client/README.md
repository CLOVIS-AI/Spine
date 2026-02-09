# Module Client-side typesafe Spine schema usage

Call a Ktor API described with type-safety in common code. 

<a href="https://central.sonatype.com/artifact/dev.opensavvy.spine/client"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.spine/api.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/alpha/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.spine/client"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

Spine allows us to [declare Ktor endpoints in code shared between the client and server](https://spine.opensavvy.dev/setup.html).

## Calling a fullstack endpoint

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

Create a new module, with a dependency on `dev.opensavvy.spine:client`. Instantiate a Ktor client by following [our tutorial](https://spine.opensavvy.dev/setup.html#client-side-implementation). You can now call the endpoint like this:

```kotlin
val user = client.request(Api / Users / Users.create, CreateUserRequest(name = "John"))
	.bodyOrThrow()
```

## Learn more

- [`request`][opensavvy.spine.client.request] calls an endpoint declared in shared code.
- [`SpineResponse`][opensavvy.spine.client.SpineResponse] encapsulates multiple strategies for facing failures.
