---
template: home.html
---

# Ktor is multiplatform, what if it were fullstack too?

Recently, many teams have adopted [Ktor](http://ktor.io). Server-side, Ktor is a refreshingly simple DSL to declare endpoints. Client-side, Ktor is available on all platforms to easily integrate with your backend of choice.

But what if you want to use both together?

## Doing it manually

Teams that attempt to use Ktor in fullstack applications invariably start the simple way: declaring an endpoint client-side, and the same endpoint server-side.

**Server-side**

```kotlin
get("/users") {
	// …
}
```

**Client-side**

```kotlin
val users: List<UserDto> = client.get("/users").body()
```

However, this approach scales poorly:

- Developers can make **typos in route names**.
- Developers can accidentally use the **wrong types on each side**. For example, using slightly different DTOs.
- Developers can make the same naming or typing mistakes for the call parameters, the returned body, possible failures, etc.
- It is hard to know if an endpoint is being used or not, **your IDE cannot help you**.
- It is hard to **navigate** from an endpoint call-site to its implementation.

Ktor provides its own system to solve this, [Ktor Resources](https://ktor.io/docs/server-resources.html). However, they are very limited. For example, they only type-safely encode the query parameters, but not the request nor response bodies. You can find a comparison between Ktor Resources and Spine [here](comparison-ktor-resources.md).

## Spine

Spine is a DSL to declare Ktor endpoints in `commonMain`, and effortlessly referencing them in your server-side and client-side code.

- Spine does not use code generation or annotation processing. **It's just regular Kotlin code!**
- Spine does not impact existing Ktor functionality, like content negotiation. Spine simply uses type parameters to tell the compiler what is expected, but everything underneath is **the regular Ktor you already use**. If you have a custom content negotiation configuration or plugin, it will work the same with Spine.
- Spine type-safely encodes the **request body, the response body, the query parameters, failure states and more**.
- With Spine, you can navigate from the client-side call of an endpoint to its definition in common code, to its server-side implementation in just a simple click of your IDE's "navigate to source" command.

**Common code**

We start by declaring a _resource_, which will hold our endpoints. We use nested objects to declare the structure of the endpoints, and simple methods for the endpoints themselves:

```kotlin
@Serializable
data class UserDto(val id: Long, val name: String)

@Serializable
data class UserCreationDto(val name: String)

@Serializable
data class NoUser(val id: Long)

/**
 * The root of your API.
 *
 * Typically, the root is used as a namespace for versioned APIs.
 */
object ApiV3 : RootResource("v3") {

	/**
	 * We declare our first static resource.
	 *
	 * A static resource is a group of endpoints that apply to the same business entity.
	 * Here, this resource declares the path `/v3/users`.
	 */
	object Users : StaticResource<ApiV3>("users", ApiV3) {

		/**
		 * We declare the existence of the `GET /v3/users` endpoint.
		 *
		 * This endpoint requires no query parameters nor body, and returns a list of [UserDto].
		 */
		val list by get()
			.response<List<UserDto>>()

		/**
		 * We declare the existing of the `POST /v3/users` endpoint.
		 *
		 * This endpoint has a request body of [UserCreationDto]. On success, it returns the created user.
		 */
		val create by post()
			.request<UserCreationDto>()
			.response<UserDto>()

		/**
		 * We declare our first dynamic resource.
		 *
		 * A dynamic resource is a resource that contains a path parameter.
		 * Here, this resource declares the path `/v3/users/{user}`.
		 */
		object User : DynamicResource<Users>("user", Users) {

			/**
			 * We declare an endpoint to access the information of a given user.
			 *
			 * If the user doesn't exist, this endpoint responds with HTTP 404 with a body of [NoUser].
			 */
			val get by get()
				.response<UserDto>()
				.failure<NoUser>(HttpStatusCode.NotFound)

		}
	}
}
```

Now that have declared the endpoints, we can create a server-side implementation.
For simplicity, we'll store data in an unsynchronized `HashMap`, but a real project would use some kind of database.

```kotlin
fun Route.apiV3() {
	val users = HashMap<Long, UserDto>()
	var nextId = 0

	/**
	 * We can refer to any Spine endpoint with the method 'route' inside the
	 * regular Ktor DSL:
	 */
	route(ApiV3.Users.list) {
		/**
		 * Instead of `call.respond()`, we use `respond()`, which verifies that
		 * the type matches the one declared in the endpoint.
		 *
		 * Of course, you can still use `call.` to access anything else from Ktor.
		 */
		respond(users.values.toList())
	}

	route(ApiV3.Users.create) {
		/**
		 * When we declare an endpoint with a request body, we can access it directly
		 * as `body`. It will automatically be deserialized to the type declared
		 * in the endpoint.
		 */
		val requestedName = body.name

		val id = nextId++
		val new = UserDto(id = id, name = requestedName)
		users[id] = new

		respond(new)
	}

	/**
	 * Endpoints with a path parameter are declared similarly…
	 */
	route(ApiV3.Users.User.get) {
		/**
		 * …but we can additionally access the path parameter for a given
		 * resource with 'idOf':
		 */
		val requestedId = idOf(ApiV3.Users.User).toLong()

		val user = users[requestedId]

		if (user != null) {
			respond(user)
		} else {
			/**
			 * An endpoint can call the `fail` function to return one of the
			 * declared failures.
			 * Here, this endpoint will return a 404 Not Found, as declared in the common code.
			 */
			fail(NoUser(requestedId))
		}
	}
}
```

Finally, we can implement a simple client that uses these endpoints:

```kotlin
val client = HttpClient()

/**
 * On the client-side, we cannot use the '.' notation for resource hierarchy,
 * we will see why just below.
 *
 * Instead, we use the '/' path operator.
 *
 * The Kotlin compiler verifies that the request body corresponds to the type
 * declared in the common code.
 */
client.request(ApiV3 / Users / Users.create, body = UserCreationDto("John"))

/**
 * We can access the output with `.body()`.
 *
 * It will automatically be deserialized to the type declared in the endpoint.
 */
val bobId = client.request(ApiV3 / Users / Users.create, body = UserCreationDto("Bob")).body().id

val users = client.request(ApiV3 / Users / Users.list).body()

check(users.find { it.name == "Bob" }?.id == bobId)

/**
 * We can access dynamic resources by instantiating them via their path
 * parameter.
 *
 * If we know that there may be an error, we can call 'handle' to type-safely handle all cases.
 * The code will not compile if a new failure is declared.
 */
val bob = client.request(ApiV3 / Users / User("$bobId") / User.get).handle(
	handle1 = { null },
	transform = { it },
)
// 'bob' is inferred by the compiler as `UserDto?`
```

Because endpoints are just regular Kotlin variables, you can easily navigate from the client-side to the server-side code and back using your IDE of choice!

Interested? Read on!

- **Configure Spine in your own project**
- **More about the different kinds of resources**
- **More about declaring endpoints**
- **More about query parameters**
- **More about failures**
- **More about failures with Arrow**

Don't hesitate to [star](https://gitlab.com/opensavvy/groundwork/spine) and share the project ❤️
