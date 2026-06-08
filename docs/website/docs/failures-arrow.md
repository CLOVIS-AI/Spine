# Declare Ktor failures with Spine and Arrow

Spine provides helper functions for the [Arrow Typed Errors library](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/). They are based on the `Raise` DSL and [context parameters](https://kotlinlang.org/docs/context-parameters.html), a new feature in [Kotlin 2.4.0](https://kotlinlang.org/docs/whatsnew24.html).

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

    1. [List of versions](news/index.md)

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

    1. [List of versions](news/index.md)

When declaring routes, replace `route` by [`routeWithRaise`](api/server-arrow/opensavvy.spine.server.arrow/route-with-raise.md):

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
    If you want to use the `Raise` DSL with Ktor, but don't want to use Spine, you can use our module `server-arrow-independent` which adds the function [`raise`](api/server-arrow-independent/opensavvy.spine.server.arrow.independent/raise.md) to regular Ktor endpoints.

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

    1. [List of versions](news/index.md)

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

    1. [List of versions](news/index.md)

In addition to `.bodyOrThrow()`, `.bodyOrNull()` and `.handle()` ([learn more](failures.md#client-side)), this module adds the [`.body()`](api/client-arrow/opensavvy.spine.client.arrow/body.md) function which raises each failure.

```kotlin
context(Raise<NotFound>)
fun HttpClient.getUser(id: String): User =
	this.request(Users / User(id) / User.get).body()

fun HttpClient.getUserOrNull(id: String): User? =
	recover(block = { getUser(id) }, recover = { null })
```
