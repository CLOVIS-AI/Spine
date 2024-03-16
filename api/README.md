# Module Multiplatform API schema declaration

Describe your Ktor API in code shared between the client and the server.

<a href="https://search.maven.org/search?q=g:dev.opensavvy.spine.api"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.spine/api.svg?label=Maven%20Central"></a>

<a href="https://gitlab.com/opensavvy/wiki/-/blob/main/stability.md#stability-levels"><img src="https://badgen.net/static/Stability/experimental/purple"></a>

When creating fullstack projects, using both Ktor as a client and a server, we need to make sure we are calling the same endpoints on both sides, with the same expected DTOs, etc.

Using Kotlin Multiplatform and KotlinX.Serialization, we can easily share DTOs, but the structure of our API is often more than that.

## Declaring a schema

Spine is a library to declare a schema of our API in pure Kotlin. Once it is declared, we can use it identically on the client and server sides.

We define that:

- [a **resource**](opensavvy.spine.typed.Resource) is an imaginary data collection,
- [an **endpoint**](opensavvy.spine.typed.Endpoint) is a single operation that acts on a given resource.

In HTTP terms, a resource is a URI, and an endpoint is a record of a URI, an HTTP method, a specific request body type…
