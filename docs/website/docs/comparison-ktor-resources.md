# Comparison between Spine and Ktor Resources

[Ktor Resources](https://ktor.io/docs/server-resources.html) are a first-party feature of Ktor that allows declaring endpoints as annotated classes.

[Spine](index.md) is a library that provides a DSL for declaring Ktor endpoints in code shared between client and server.

This page describes the differences in approach between these two solutions to help you choose the one you prefer.

## Features

### Declaring endpoints

#### Declare a simple endpoint

We define the existence of the path `/articles` in our API.

```kotlin title="Ktor Resources"
@Resource("/articles")
class Articles()
```

```kotlin title="Spine"
object Articles : RootResource("articles")
```

#### Declare a simple endpoint with a query parameter

We define the existence of the path `/articles` with the query parameter `?sort=new`.

```kotlin title="Ktor Resources"
@Resource("/articles")
class Articles(
	val sort: String? = "new",
)
```

```kotlin title="Spine"
object Articles : RootResource("articles") {

	class SearchParams(data: ParameterStorage) : Parameters(data) {
		var sort: String? by parameter("new")
	}
}
```

Because Spine doesn't use reflection nor compiler plugins, it requires slightly more configuration to declare query parameters.

#### Declare a simple endpoint with a query parameter and a method

Ktor Resources cannot encode the HTTP method as part of the resource. This means that all HTTP methods on a given resource must use the same query parameters.

```kotlin title="Spine"
object Articles : RootResource("articles") {

	class SearchParams(data: ParameterStorage) : Parameters(data) {
		var sort: String? by parameter("new")
	}

	val list by get() //(1)!
		.parameters(::SearchParams)

	val create by post() //(2)!
}
```

1. Configure the behavior of the `GET /articles` endpoint, which has a `sort` query parameter.
2. Configure the behavior of the `POST /articles` endpoint, which has no particular query parameters.

#### Declare a nested endpoint

We define the existence of the path `/articles/new`.

```kotlin title="Ktor Resources"
@Resource("/articles")
class Articles {

	@Resource("new")
	class New(val parent: Articles = Articles())
}
```

Notice that the child resource must refer to the parent one. If the parent resource has mandatory query parameters, the child resource must specify them too.

```kotlin title="Spine"
object Articles : RootResource("articles") {

	object New : StaticResource<Articles>("new", Articles) {

		val post by post()
	}
}
```

Note that we define which HTTP methods are allowed, which Ktor Resources cannot do.

Alternatively, we can avoid declaring a nested resource and simply declare the endpoint as-is:

```kotlin title="Spine"
object Articles : RootResource("articles") {

	val new by post("new")
}
```

#### Declare a nested endpoint with a path parameter

We define the existence of the path `/articles/{id}`.

```kotlin title="Ktor Resources"
@Resource("/articles")
class Articles {

	@Resource("{id}")
	class Id(val parent: Articles = Articles(), val id: Long)
}
```

Note that the name of the variable `id` must match the name of the path parameter.

```kotlin title="Spine"
object Articles : RootResource("articles") {

	object Id : DynamicResource<Articles>("id", Articles)
}
```

The path parameter is automatically handled by the `DynamicResource` class. However, it will be typed as `String`.

### Server-side

#### Implement an endpoint server-side

```kotlin title="Ktor Resources"
install(Resources)
routing {
	get<Articles> { resource ->
		println("Get all articles")
	}
}
```

```kotlin title="Spine"
routing {
	route(Articles.list) {
		println("Get all articles")
	}
}
```

#### Implement an endpoint with query parameters server-side

```kotlin title="Ktor Resources"
install(Resources)
routing {
	get<Articles> { resource ->
		println("Get all articles, sorted by ${resource.sort}")
	}
}
```

```kotlin title="Spine"
routing {
	route(Articles.list) {
		println("Get all articles, sorted by ${parameters.sort}")
	}
}
```

#### Implement an endpoint with path parameters server-side

```kotlin title="Ktor Resources"
install(Resources)
routing {
	get<Articles.Id> { resource ->
		println("Get the article ${resource.id}")
	}
}
```

```kotlin title="Spine"
routing {
	route(Articles.Id.get) {
		println("Get the article ${idOf(Articles.Id)}")
	}
}
```

### Client-side

#### Call an endpoint client-side

```kotlin title="Ktor Resources"
client.get(Articles()).body<List<ArticleDto>>()
```

```kotlin title="Spine"
client.request(Articles.list).body()
```

#### Call an endpoint with query parameters client-side

```kotlin title="Ktor Resources"
client.get(Articles(sort = "oldest")).body<List<ArticleDto>>()
```

```kotlin title="Spine"
client.request(Articles.list, parameters = { sort = "oldest" }).body()
```

#### Call an endpoint with path parameters client-side

```kotlin title="Ktor Resources"
client.get(Articles.Id(id = 12)).body<ArticleDto>()
```

```kotlin title="Spine"
client.request(Articles / Id("12") / Id.get).body()
```

## Conclusion

Here's a recap of features:

| Feature                                                 | Ktor Resources  | Spine            |
|---------------------------------------------------------|-----------------|------------------|
| Type-safe path                                          | ✓               | ✓                |
| Type-safe path parameters                               | ✓               | Must be `String` |
| Type-safe query parameters                              | ✓               | ✓                |
| Different query parameters depending on the HTTP method | No              | ✓                |
| Mandatory query parameters in non-leaf resources        | No              | ✓                |
| Declare the HTTP method along the resource              | No              | ✓                |
| Type-safe request body                                  | No              | ✓                |
| Type-safe response body                                 | No              | ✓                |
| Type-safe failures                                      | No              | ✓                |
| Access the path of an endpoint                          | ✓               | ✓                |
| List the parameters of an endpoint                      | With reflection | ✓                |

As a summary:

- Ktor Resources exist to avoid hard-coding endpoint paths. Instead, you can create a Resource class and reuse it in multiple places, between client and server.
- Spine exists to share most usual endpoint metadata, like the path and query parameters, but also request and response bodies, failure conditions, and more.
