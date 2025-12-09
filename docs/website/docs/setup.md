# Make your Ktor app fullstack with Spine

Spine is a library to declare Ktor endpoints in your `commonMain` code, which you can then reference easily in your server-side and client-side code.

In this article, we'll show you how to configure Spine in your existing Ktor application.

To get an overview of what Spine is, [visit the home page](index.md).

## Sharing code between client and server

There are two main ways to share code between client and server.

- If you know that your server and client are for different Kotlin platforms (for example, a JVM server and a JS client), you could create a single module that has both platforms registered, and share code in `commonMain`.
- If you may have platforms that have both clients and servers, it is better to create three modules: one for shared code, one for the server, one for the client.

Spine works equally well no matter the approach, but we recommend the second because it is more versatile and makes testing easier (as we will see later).

Therefore, you should have something like:

```text
your-project/
  backend/
    build.gradle.kts
  frontend/
    build.gradle.kts
  shared/
    build.gradle.kts
  build.gradle.kts
  settings.gradle.kts
```

In this tutorial, we will create a simple `/ping` endpoint which responds "Pong" followed by the initial request body.

## Your first Spine endpoint

For now, we will edit the shared module (called `shared` in our example). Start by adding the Spine API module to its dependencies:

=== "Kotlin JVM"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("jvm") version "…"
    }

    dependencies {
        api("dev.opensavvy.spine:api:VERSION") //(1)!
    }
    ```

    1. [List of versions](news/)

    Then, create the file `src/main/kotlin/Api.kt`.

=== "Kotlin Multiplatform"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("multiplatform") version "…"
    }

    kotlin {
        jvm()
        js { browser() } 
        // add any other platform you want to target…

        sourceSets.commonMain.dependencies {
            api("dev.opensavvy.spine:api:VERSION") //(1)!
        }
    }
    ```

    1. [List of versions](news/)

    Then, create the file `src/commonMain/kotlin/Api.kt`.

All Spine endpoints are grouped in _resources_. A resource is responsible for the path of the endpoints.

Resources form a chain that starts at a `RootResource`. Each new resource may be a `StaticResource` or a `DynamicResource`, and always refers to its parent. In this example, we will only use a single resource.

```kotlin title="Api.kt"
package your.app.shared

import opensavvy.spine.api.*

object Ping : RootResource("ping") { //(1)!

	val ping by put() //(2)!
		.request<String>() //(3)!
		.response<String>() //(4)!

}
```

1. We declare a new `RootResource` with the path `/ping`. All nested resources and endpoints will have children paths of `/ping`.
2. We declare an endpoint `PUT /ping` (because we use the method `put()` in the resource with path `/ping`). The name of the variable doesn't matter, but a good name can help readability. Note the `by` instead of `=` when declaring the endpoint.
3. We declare that this endpoint requires a request body of type `String`. Under the hood, Ktor's usual content negotiation system is used with your existing configuration.
4. We declare that this endpoint will respond with a body of type `String`. Under the hood, Ktor's usual content negotiation system is used with your existing configuration.

That's it, we declare the existence of our `PUT /ping` endpoint! Now, all that's left is implementing the server and client sides.

To learn more about the concepts from this section, see:

- [**Learn more about the different kinds of resources**](resources.md)
- [**Learn more about declaring endpoints**](endpoints.md)

## Server-side implementation

Start by adding the Spine dependency to your server-side module.

=== "Kotlin JVM"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("jvm") version "…"
    }

    dependencies {
        implementation("dev.opensavvy.spine:server:VERSION") //(1)!
        implementation(project(":shared")) //(2)!
    }
    ```

    1. [List of versions](news/)
    2. Ensure the backend module has access to the endpoints we just declared.

    Then, create the file `src/main/kotlin/Ping.kt`.

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
            api("dev.opensavvy.spine:server:VERSION") //(1)!
            implementation(project(":shared")) //(2)!
        }
    }
    ```

    1. [List of versions](news/)
    2. Ensure the backend module has access to the endpoints we just declared.

    Then, create the file `src/commonMain/kotlin/Ping.kt`.

Usually, Ktor applications are separated as functions which handle different groups of resources. We'll do the same, and create a new file specifically for this API.

```kotlin title="Ping.kt"
package your.app.server

import opensavvy.spine.api.*
import opensavvy.spine.server.*
import io.ktor.http.*
import io.ktor.server.routing.*
import your.app.shared.*

fun Route.ping() {

	// Here, we would usually declare routes with 'get {}' or 'post {}'.
	// However, we already declared the method in the common code,
	// so we just refer to it.
	route(Ping.ping) {
		respond("Pong: $body")
	}
}
```

In this example:

- Because the endpoint's HTTP method is already declared, we don't have to do it again, and can just refer to the endpoint.
- Spine adds the `body` variable that automatically contains the deserialized request body of the type declared in the endpoint (`String` in this example).
- Spine adds the `respond()` function that accepts and serializes the response body of the type declared in the endpoint (`String` in this example).

Don't worry—you can still access the variable `call` to do anything else you may want with Ktor.

Finally, we can register this endpoint by calling our `ping()` function in the `routing {}` section of your Ktor application:

```kotlin
fun Application.yourApp() {
	install(ContentNegotiation) {
		json()
	}

	// …

	routing {
		ping()
	}
}
```

To learn more about creating a Ktor application and configuring plugins, see the [official Ktor tutorial](https://ktor.io/docs/server-create-restful-apis.html): Spine doesn't impact the configuration.

To learn more about the concepts from this section, see:

- [**Learn more about declaring the request and response bodies**](endpoints.md)

## Client-side implementation

Finally, we can call these methods on the frontend side. We'll start by declaring the dependency:

=== "Kotlin JVM"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("jvm") version "…"
    }

    dependencies {
        implementation("dev.opensavvy.spine:client:VERSION") //(1)!
        implementation(project(":shared")) //(2)!
    }
    ```

    1. [List of versions](news/)
    2. Ensure the frontend module has access to the endpoints we declared.

    Then, create the file `src/main/kotlin/Ping.kt`.

=== "Kotlin Multiplatform"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("multiplatform") version "…"
    }

    kotlin {
        jvm()
        js { browser() }
        // add any other platform you want to target…

        sourceSets.commonMain.dependencies {
            api("dev.opensavvy.spine:client:VERSION") //(1)!
            implementation(project(":shared")) //(2)!
        }
    }
    ```

    1. [List of versions](news/)
    2. Ensure the frontend module has access to the endpoints we declared.

    Then, create the file `src/commonMain/kotlin/Ping.kt`.

If it's your first Ktor client, also follow the [official Ktor client tutorial](https://ktor.io/docs/client-create-new-application.html).

On the client-side, Ktor is organized around the `HttpClient` class.

```kotlin title="Ping.kt"
package your.app.client

import opensavvy.spine.api.*
import opensavvy.spine.client.*
import io.ktor.http.*
import io.ktor.client.*
import your.app.shared.*

suspend fun main() {
	val client = HttpClient {
		install(DefaultRequest) { //(1)!
			url("https://your-app.com")
		}

		install(ContentNegotiation) {
			json()
		}
	}

	val pong = client.request(Ping / Ping.ping, "From the client!").body() //(2)!
	println("Got: $pong")
}
```

1. The [Ktor default request](https://ktor.io/docs/client-default-request.html) plugin is convenient to declare the URL, since Spine only encodes the path of the request.
2. Notice that the path is `Ping / Ping.ping` instead of just `Ping.ping` that we used on the server-side. This is to allow more complex URLs that contain path parameters. [Learn more](resources.md).

```terminaloutput
Got: Pong: From the client!
```

That's it! You created your first fullstack Ktor endpoint. Notice that the `request` function's second parameter serialized the request body as a `String`, as declared in the common code, and deserialized the response body automatically too.

To learn more about the concepts from this section, see:

- [**Learn more about the client-side path syntax**](resources.md) (`Ping / Ping.ping`)
- [**Learn more about declaring the request and response bodies**](endpoints.md)
- Or, read on and discover how to easily test your endpoint.

## Testing your application

Testing client-server systems is traditionally complex because the client and server live in different processes. Traditional test frameworks, like JUnit, expect tests to run in a single process. You must either start the server and use the client for tests, but you need to remember to restart the server each time it is modified, or you risk testing against an old version.

Ktor offers a simpler system: the [Ktor TestHost](https://ktor.io/docs/server-testing.html), which allows creating a fake server and a fake client in a single process. The TestHost uses all the mechanisms of a real server, including serialization, but skips the actual TCP socket.

The Ktor TestHost is a great fit for testing Spine endpoints because all the configuration for the endpoints is already in the common code. Typically, the process is as follows:

- The endpoint tests live in the `:backend` module, because it typically supports fewer platforms than the other modules.
- Add all the platforms supported by `:backend` to the `:frontend` module, even if you don't expect to use them. This way, the `:backend` module can import the `:frontend` module for its tests, which allows testing both in a single place. For example, if you want to create a JVM server and a JS frontend, you should still add the `jvm()` platform in the `:frontend` project; this way, the server can use the frontend's code, compiled for the JVM, to test itself.

Modify the configuration of the backend:

=== "Kotlin JVM"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("jvm") version "…"
    }

    dependencies {
        // What we had before:
        implementation("dev.opensavvy.spine:server:VERSION")
        implementation(project(":shared"))

        // New:
        testImplementation(project(":frontend")) //(1)!
        // Also, add a dependency on the Ktor TestHost
    }
    ```

    1. To test both client and server, the server's tests depend on the client.

    Then, create the file `src/test/kotlin/PingTest.kt`.

=== "Kotlin Multiplatform"

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("multiplatform") version "…"
    }

    kotlin {
        jvm()
        linuxX64()
        // add any other platform you want to target…

        // What we had before:
        sourceSets.commonMain.dependencies {
            api("dev.opensavvy.spine:server:VERSION")
            implementation(project(":shared"))
        }

        // New:
        sourceSets.commonTest.dependencies {
            implementation(project(":frontend")) //(1)!
            // Also, add a dependency on the Ktor TestHost
        }
    }
    ```

    1. To test both client and server, the server's tests depend on the client. For this to work, the `:frontend` must support at least all the platforms that the backend supports.

    Then, create the file `src/commonTest/kotlin/PingTest.kt`.

In this example, we'll use the [Prepared test framework](https://prepared.opensavvy.dev) using the [TestBalloon engine](https://prepared.opensavvy.dev/api-docs/runners/runner-testballoon/index.html) and the [Ktor compatibility module](https://prepared.opensavvy.dev/features/compat-ktor.html), which require additional configuration not shown here. However, you can follow the same steps with any other test framework.

```kotlin title="PingTest.kt"
package your.app.test

import opensavvy.spine.api.*
import opensavvy.spine.server.*
import opensavvy.spine.client.*
import io.ktor.http.*
import your.app.shared.*
import your.app.server.*
import your.app.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

private val server by preparedServer { //(2)!
	install(ServerContentNegotiation) { //(3)!
		json()
	}

	routing {
		ping() //(4)!
	}
}

private val client by server.preparedClient { //(5)!
	install(ClientContentNegotation) { //(6)!
		json()
	}
}

val PingTest by preparedSuite { //(1)!

	test("The /ping route should return 'Pong: xxx'") {
		val initial = "FOO"
		val expected = "Pong: FOO"

		check(client().request(Ping / Ping.ping, initial).body() == expected) //(7)!
	}

}
```

1. The `preparedSuite` DSL is the entrypoint for tests declared with [Prepared and TestBalloon](https://prepared.opensavvy.dev/api-docs/runners/runner-testballoon/index.html). If you use another test framework, it will be different.
2. The `preparedServer` DSL allows declaring the Ktor TestHost as [a special test fixture](https://prepared.opensavvy.dev/features/compat-ktor.html). If you use another test framework, this is probably replaced by calling the `testApplication {}` function within each test.
3. We always need at least `ContentNegotation`. Notice the import alias, used because we need both server and client negotiation in this file.
4. We can configure the Ktor test host, just like a real server, with the `routing {}` block. To simplify tests, however, we will only register the routes related to the test, instead of registering the entire API.
5. The `server.preparedClient` DSL allows declaring the Ktor TestHost client as [a special test fixture](https://prepared.opensavvy.dev/features/compat-ktor.html). If you use another test framework, this is probably replaced by calling the `createClient {}` function within the `testApplication {}` block within each test.
6. We always need at least `ContentNegotation`. Notice the import alias, used because we need both server and client negotiation in this file.
7. The [Power Assert plugin](https://kotlinlang.org/docs/power-assert.html) allows creating a nice error message for any Kotlin call, without the need for assertion libraries. We recommend it!

Because this entire test runs in a single process, you don't have to worry about the server being out of date. Also, you can debug and step from client-side to server-side.

To learn more about the concepts from this section, see:

- [**The official Ktor TestHost documentation**](https://ktor.io/docs/server-testing.html)
- [**The Prepared Ktor compatibility module**](https://prepared.opensavvy.dev/features/compat-ktor.html)

Congrats on getting this far, have fun with your new fullstack Ktor apps!
