# Declare fullstack Ktor endpoints with Spine

> Reference: [`AnyEndpoint`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-any-endpoint)

Once you have described the [resources](resources.md) of your API, you can declare endpoints. Each endpoint is a combination of a path, an HTTP method, and more optional information, like the body type.

Endpoints can only be declared within a resource. They are declared with the `by` keyword followed by the name of the HTTP method they correspond to.

```kotlin
object Api2 : RootResource("v2") {

	val get by get()

}
```

The name of the variable itself doesn't matter.

For the remainder of this page, the enclosing resource is omitted from code samples for conciseness.

!!! danger "Do not explicitly write the type of your endpoints"
In Kotlin, we have the choice of explicitly writing a variable's type, or letting the compiler infer it.
```kotlin
val get: SomeTypeHere by get()
```
However, this should be avoided for Spine endpoints. We recommend letting the compiler infer the type. If you write the type explicitly, it is likely that your code will not compile anymore when new versions of Spine are released. If you're curious, you can learn more [here](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-any-endpoint/index.md#the-trick).

    If you really must write the type of an endpoint (for example if you write an introspection function that prints all endpoints), you should use the `AnyEndpoint` type, which has access to all endpoint informatino but without type safety.

## HTTP method

An endpoint is declared using `by` followed by the name of the HTTP method. The available methods are:

- `by get()`: HTTP `GET`
- `by post()`: HTTP `POST`
- `by put()`: HTTP `PUT`
- `by patch()`: HTTP `PATCH`
- `by delete()`: HTTP `DELETE`
- `by head()`: HTTP `HEAD`

## Specifying a path

Optionally, an endpoint can include an additional path segment. This is useful if you have one or two endpoints with a subpath and don't want to create a dedicated resource.

```kotlin
object Users : StaticResource("users") { //(1)!

	val list by get() //(2)!

	val listVips by get("vips") //(3)!

}
```

1. The [static resource](resources.md#static-resources) `/users`.
2. The `GET /users` endpoint.
3. The `GET /users/vips` endpoint.

is equivalent to:

```kotlin
object Users : StaticResource("users") { //(1)!

	val list by get() //(2)!

	object Vips : StaticResource("vips") { //(3)!

		val list by get() //(4)!

	}
}
```

1. The [static resource](resources.md#static-resources) `/users`.
2. The `GET /users` endpoint.
3. The [static resource](resources.md#static-resources) `/users/vips`.
4. The `GET /users/vips` endpoint.

## Request and response body

An endpoint can declare its request and response body.

The request and response body go through Ktor's usual content negotiation plugin.
For example, if you use KotlinX.Serialization, then the request and response bodies must be annotated with `@Serializable`.
Any other content negotiation library compatible with Ktor is also compatible with Spine.

```kotlin
val listUsers by get()
	.response<List<UserDto>>() //(1)!

val deleteUser by delete()
	.request<UserDeletionDto>() //(2)!

val createUser by post()
	.request<UserCreationDto>()
	.response<UserDto>()
```

1. [`request`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-any-endpoint/-builder/request.md) allows declaring the type of the data sent by the client. The value will go through Ktor's usual content negotiation, following your existing configuration.
2. [`response`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-any-endpoint/-builder/response.md) allows declaring the type of the data sent by the server. The value will go through Ktor's usual content negotiation, following your existing configuration.

On the server-side, the request body is accessible through the [`body`](api/-server-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.server/-typed-response-scope/body.md) variable, and the response body can be sent with the function [`respond`](api/-server-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.server/respond.md):

```kotlin
route(Users.createUser) {
	val user = UserDto(newId(), name = body.name)
	users.create(user)
	respond(user)
}
```

On the client-side, the request body is passed as the second argument of the function [`request`](api/-client-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.client/request.md), and the response is acquired via the result of [`bodyOrThrow`](api/-client-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.client/body-or-throw.md):

```kotlin
val user = client.request(Users.createUser, UserCreationDto(name = "Bob"))
	.bodyOrThrow()
```

## Query parameters

Query parameters can be declared with the `parameters` function:

```kotlin
val list by get()
	.parameters(::SearchParameters)
```

To learn more, read the article on [query parameters](parameters.md).

## Failures

Spine is able to type-safely represent the different failure conditions of an endpoint:

```kotlin
val createUser by get()
	.failure<UserAlreadyExists>(HttpMethod.UnprocessableEntity)
```

To learn more, read the article on [failures](failures.md).
