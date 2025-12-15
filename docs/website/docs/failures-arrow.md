# Declare Ktor failures with Spine and Arrow

Spine provides helper functions for the [Arrow Typed Errors library](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/). They are based on the `Raise` DSL and [context parameters](https://kotlinlang.org/docs/context-parameters.html), which you may need to [enable](https://kotlinlang.org/docs/context-parameters.html#how-to-enable-context-parameters).

## Declaring failures

Declaring failures with the Arrow compatibility modules is identical to declaring failures with the base Spine module. [Read the dedicated article](failures.md#declaring-failures).

## Server-side

Add a dependency on the `server-arrow` module:

=== "Kotlin JVM"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("jvm") version "…"
    }

    dependencies {
        implementation("dev.opensavvy.spine:server-arrow:VERSION") //(1)!
    }
    ```

    1. [List of versions](news/)

=== "Kotlin Multiplatform"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("multiplatform") version "…"
    }

    kotlin {
        jvm()
        linuxX64()
        // add any other platform you want to target…

        sourceSets.commonMain.dependencies {
            api("dev.opensavvy.spine:server-arrow:VERSION") //(1)!
        }
    }
    ```

    1. [List of versions](news/)

When declaring routes, replace `route` by `routeWithRaise`:

the Arrow module adds:

```kotlin
routeWithRaise(Users.User.edit) {
	// …
}
```

The DSL is identical to usual Spine, but adds support for `Raise` for failure management.

For example, you can replace:

```kotlin
route(Users.User.edit) {
	if (body.user.name.length < 5)
		fail(UsernameTooShort)

	// …
}
```

by:

```kotlin
routeWithRaise(Users.User.edit) {
	ensure(body.user.name.length >= 5) { UsernameTooShort }
}
```

??? info "Raise in Ktor endpoints without Spine"
If you want to use the `Raise` DSL with Ktor, but don't want to use Spine, you can use our module `server-arrow-independent` which adds the function [`raise`](api/-server-side%20-arrow%20helpers/opensavvy.spine.server.arrow.independent/raise.md) to regular Ktor endpoints.

## Client-side

Add a dependency on the `client-arrow` module:

=== "Kotlin JVM"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("jvm") version "…"
    }

    dependencies {
        implementation("dev.opensavvy.spine:client-arrow:VERSION") //(1)!
    }
    ```

    1. [List of versions](news/)

=== "Kotlin Multiplatform"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("multiplatform") version "…"
    }

    kotlin {
        jvm()
        linuxX64()
        // add any other platform you want to target…

        sourceSets.commonMain.dependencies {
            api("dev.opensavvy.spine:client-arrow:VERSION") //(1)!
        }
    }
    ```

    1. [List of versions](news/)

In addition to `.bodyOrThrow()`, `.bodyOrNull()` and `.handle()` ([learn more](failures.md#client-side)), this module adds the [`.body()`](api/-client-side%20typesafe%20-spine%20schema%20usage%20(with%20-arrow%20typed%20errors)/opensavvy.spine.client.arrow/body.md) function which raises each failure.

```kotlin
context(Raise<NotFound>)
fun HttpClient.getUser(id: String): User =
	this.request(Users / User(id) / User.get).body()

fun HttpClient.getUserOrNull(id: String): User? =
	recover(block = { getUser(id) }, recover = { null })
```
