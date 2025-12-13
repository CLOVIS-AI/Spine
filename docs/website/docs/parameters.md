# Declare query parameters with Spine

> Reference: [`Parameters`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-parameters)

With Spine, query parameters can be declared in your multiplatform code and type-safely referred to on the server and client side.

```kotlin
val listUsers by get() //(1)!
	.parameters(::ListUsersParameters)
```

1. Declares an endpoint with HTTP method `GET`. [Learn more](endpoints.md).

Unlike the request and response bodies, which use Ktor's content negotiation and the syntax `.request<Foo>()`, query parameters are a Spine feature and use the syntax `.parameters(::Foo)`. This allows introspection of the different parameters, without using code generation nor compiler plugins.

Each query parameter is declared as a property of a dedicated class for that endpoint.
This class must always have a single-parameter constructor that accepts the backing storage:

```kotlin
class ListUsersParameters(data: ParameterStorage) : Parameters(data) {
	var sort: String by parameter(default = "new")
	var isArchived: Boolean by parameter(name = "archived", default = false)
}
```

Parameters should inherit from `Parameters` and have a constructor that passes through the `ParameterStorage`.

## Declaring parameters

Each parameter is defined by calling the `by parameter()` function within a class that inherits from `Parameters`. Parameters should be declared as `var` to allow [client-side initialization](#client-side).

If a parameter is nullable or has a default value, it is optional. Otherwise, it is mandatory and an error will be thrown if the client hasn't specified it. Here are a few examples:

```kotlin
var includeArchived: Boolean by parameter(default = false) //(1)!
var sort: String? by parameter() //(2)!
var startDate: String by parameter(name = "start_date") //(3)!
var endDate: String? by parameter(name = "end_date", default = null) //(4)!
```

1. Declares the optional query parameter `includeArchived`, which is `#!kotlin false` when omitted by the client.
2. Declares the optional query parameter `sort`, which is `#!kotlin null` when omitted by the client.
3. Declares the mandatory query parameter `start_date`.
4. Declares the optional query parameter `end_date`, which is `#!kotlin null` when omitted by the client.

## Server-side

On the server-side, parameters are accessed via the special variable `parameters`:

```kotlin
route(Users.list) {
	println("Only enabled users: ${!parameters.includeArchived}")
	println("Sort order: ${parameters.sort}")
}
```

## Client-side

On the client-side, parameters are set via the parameter block:

```kotlin
client.request(
	endpoint = Users.list,
	parameters = {
		isArchived = true
		sort = "oldest"
	}
)
```
