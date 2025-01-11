# Module Multiplatform Ktor schema declaration

Describe your Ktor API in code shared between the client and the server.

<a href="https://search.maven.org/search?q=g:dev.opensavvy.spine%26a:api"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.spine/api.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/alpha/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.spine/api"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

When creating fullstack projects, using both Ktor as a client and a server, we need to make sure we are calling the same endpoints on both sides, with the same expected DTOs, etc.

Using Kotlin Multiplatform and KotlinX.Serialization, we can easily share DTOs, but the structure of our API is often more than that.

## Declaring a schema

Spine is a library to declare a schema of our API in pure Kotlin. Once it is declared, we can use it identically on the client and server sides.

We define that:

- [a **resource**](opensavvy.spine.api.Resource) is an imaginary data collection,
- [an **endpoint**](opensavvy.spine.api.AnyEndpoint) is a single operation that acts on a given resource.

In HTTP terms, a resource is a URI, and an endpoint is a record of a URI, an HTTP method, a specific request body type…

Typically, resources are declared as singletons:
```kotlin
// Declare our root endpoint: /v1
object Api : RootResource("v1") {
	
	// Declare a nested resource: /v1/users
	object Users : StaticResource<Api>("/users", parent = Api) {
		
		// GET /v1/users
		// which returns a list of UserDto
		val list by get()
			.response<List<UserDto>>()
		
		// Declare a nested resource: /v1/users/{user}
		object User : DynamicResource<Users>("user", parent = Users) {
			
			// GET /v1/users/{user}
			// which returns a UserDto
			val get by get()
				.response<UserDto>()
			
			// POST /v1/users/{user}
			// which accepts a UserCreationDto and returns a UserDto
			val create by post()
				.request<UserCreationDto>()
				.response<UserDto>()
			
			// PUT /v1/users/{user}/friend
			val addFriend by put("friend")

			// DELETE /v1/users/{user}/friend
			val removeFriend by delete("friend")
		}
	}
}
```

We can then refer to any endpoint easily. For example, `Api.Users.User.removeFriend` is the `DELETE /v1/users/{user}/friend` endpoint.

## Learn more

**Resources** describe a grouping of endpoints under a single URL:
- [`RootResource`][opensavvy.spine.api.RootResource] is the root of a URL.
- [`StaticResource`][opensavvy.spine.api.StaticResource] is a hard-coded segment in a URL, for example `/users` or `/posts`.
- [`DynamicResource`][opensavvy.spine.api.DynamicResource] is a wildcard segment in a URL, which could be replaced by a user's ID, for example.

**Endpoints** describe a specific HTTP method along with its expected input and output types, parameters, etc.
- [`AnyEndpoint`][opensavvy.spine.api.AnyEndpoint] allows introspecting information about an endpoint.
- [`AnyEndpoint.Builder`][opensavvy.spine.api.AnyEndpoint.Builder] allows declaring information about an endpoint.
- [`Parameters`][opensavvy.spine.api.Parameters] represent query parameters.

To learn how to use the APIs on the client or on the server, see the documentation of the `client` and `server` modules.
