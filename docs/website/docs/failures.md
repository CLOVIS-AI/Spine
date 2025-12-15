# Declare Ktor failures with Spine

> Reference: [`FailureSpec`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-failure-spec)

## Declaring failures

An [endpoint](endpoints.md) can declare zero or more failures. A failure is tied to a specific HTTP error code:

```kotlin
val create by post()
	.request<UserCreationDto>()
	.failure<NotAllowed>(HttpStatusCode.Forbidden)

@Serializable
data class UserCreationDto(
	val name: String,
)

@Serializable
data class NotAllowed(
	val yourRights: List<String>,
	val requiredRights: List<String>,
)
```

The failure type is serialized and deserialized using Ktor's content negotiation plugin. Any type compatible with Ktor's content negotiation can be used as a failure type.

The function `.failure()` may be called multiple times to declare multiple different failures. However, **only one failure can be declared for each HTTP status code**. If you want to declare multiple failures for a single HTTP status code, do so with a polymorphism feature provided by your content negotiation library. For example, with KotlinX.Serialization, use a sealed interface or sealed class:

```kotlin
val edit by patch()
	.request<UserModificationDto>()
	.failure<CannotProcessUserModification>(HttpStatusCode.UnprocessableEntity)

@Serializable
sealed interface CannotProcessUserModification

@Serializable
@SerialName("InvalidUsername")
data class InvalidUsername(val userId: String, val explain: String) : CannotProcessUserModification

@Serializable
@SerialName("InvalidAge")
data class InvalidAge(val userId: String, val explain: String) : CannotProcessUserModification
```

## Server-side

> If you use [Arrow Typed Errors](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/), you may be interested in [our dedicated support](failures-arrow.md).

On the server-side, Spine adds the [`fail()`](api/-server-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.server/fail.md) method to fail with one of the declared failures:

```kotlin
route(Users.User.patch) {
	val user = idOf(Users.User)

	if (body.name.length !in 3..18)
		fail(InvalidUsername(user, "Username too short or too long. Should be 3..18 characters, found: ${body.name.length}"))

	if (body.age < 13)
		fail(InvalidAge(user, "Users of this service should be at least 13 years of age."))

	// …
}
```

When `fail()` is called, the server responds with the configured HTTP status code. The response body is the failure itself.

Calling `fail()` interrupts the function, no further code is executed.

## Client-side

> If you use [Arrow Typed Errors](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/), you may be interested in [our dedicated support](failures-arrow.md).

On the client-side, users have the choice between:

- Throwing an exception on any kind of failure with [`bodyOrThrow`](api/-client-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.client/body-or-throw.md).
- Treating all failures as `#!kotlin null` with [`bodyOrNull`](api/-client-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.client/body-or-null.md).
- Handle each failure separately with [`handle`](api/-client-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.client/handle.md).

The `handle` method accepts one handler for each declared failure.

For example, with the endpoint:

```kotlin
val edit by patch()
	.request<UserModificationDto>()
	.failure<UserNotFound>(HttpStatusCode.NotFound)
	.failure<NotAllowed>(HttpStatusCode.Forbidden)
```

can be called as:

```kotlin
client.request(Users / User("123") / User.edit, UserModificationDto(/* … */)).handle(
	handle1 = { error("The server didn't find the user, but it must exist for the client to make the request: $it") },
	handle2 = { error("Not allowed to make this request: $it") },
	transform = { it },
)
```

Each handler is a lambda that accepts a single argument that contains the failure payload.

- The handler can throw another exception.
- The handler can return a normal value to "recover" from the failure.

The `transform` lambda allows converting the response body. Use `{ it }` if no conversion is necessary.
