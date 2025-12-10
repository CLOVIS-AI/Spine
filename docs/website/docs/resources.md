# Declare fullstack Ktor resources with Spine

With Spine, endpoints are grouped in resources. Each resource describes its path, while the endpoints within describe what the client can do.

Resources form a tree: each resource has a reference to its parent, up to the API root.

There are three types of resources:

- [`RootResource`](#the-root-resource) is the root of the API.
- [`StaticResource`](#static-resources) is a typical hierarchy marker.
- [`DynamicResource`](#dynamic-resources) represents a path parameter.

In Kotlin, resources are typically described as nested classes, though that is not mandatory.
Here is an example that describes the endpoints:

- `GET /v2/users`
- `POST /v2/users`
- `GET /v2/users/123`
- `DELETE /v2/users/123`

=== "Nested"

    ```kotlin
    object V2 : RootResource("v2") {

        object Users : StaticResource<V2>("users", V2) {
            
            val list by get()
            val create by post()
 
            object User : DynamicResource<Users>("user", Users) {

               val get by get()
               val delete by delete()

            }
        }
    }
    ```

    - Server-side notation: `#!kotlin V2.Users.User.get`
    - Client-side notation: `#!kotlin V2 / Users / User("123") / User.get`

=== "Non-nested"

    ```kotlin
    object V2 : RootResource("v2")

    object Users : StaticResource<V2>("users", V2) {

        val list by get()
        val create by post()

    }

    object User : DynamicResource<Users>("user", Users) {

       val get by get()
       val delete by delete()

    }
    ```

    - Server-side notation: `#!kotlin V2.Users.User.get`
    - Client-side notation: `#!kotlin V2 / Users / User("123") / User.get`

## The root resource

> Reference: [`RootResource`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-root-resource)

The root resource is the root of a specific API.

A single project could have multiple roots. This is typically used in versioned APIs:

```kotlin
object Api2 : RootResource("v2")

object Api3 : RootResource("v3")
```

## Static resources

> Reference: [`StaticResource`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-static-resource)

A static resource represents a path within another resource. Each static resource must refer to its direct parent in the constructor call.

For example, if we want to declare the endpoints:

- `GET /v2/users`
- `GET /v2/blog`
- `GET /v2/blog/archives`

```kotlin
object Api2 : RootResource("v2") {
	object Users : StaticResource<Api2>("users", Api2)
	object Blog : StaticResouce<Api2>("blog", Api2) {
		object Archives : StaticResource<Blog>("archives", Blog)
	}
}

println(Api2.Blog.Archives.path) // /v2/users/blog/archives
```

To refer to a static resource on the server-side, you can use the regular Kotlin `.` notation: `Api2.Blog.Archives`.

To refer to a static resource on the client-side, you can use the slash notation with member imports: `Api2 / Blog / Archives`. Alternatively, without member imports: `Api2 / Api2.Blog / Api2.Blog.Archives`. This is necessary to correctly interpret dynamic resources.

## Dynamic resources

> Reference: [`DynamicResource`](api/-multiplatform%20-ktor%20schema%20declaration/opensavvy.spine.api/-dynamic-resource)

Dynamic resources represent a segment that can have multiple different values at runtime. Typically, this is used for the ID of a resource.

For example, if we want to declare the endpoints:

- `GET /v2/users`
- `GET /v2/users/{user}`
- `GET /v2/users/{user}/friends`
- `GET /v2/users/{user}/friends/{friend}`

```kotlin
object V2 : RootResource("v2") {
	object Users : StaticResource<Api2>("users", Api2) {
		object User : DynamicResource<Users>("user", Users) {
			object Friends : StaticResource<User>("friends", User) {
				object Friend : DynamicResource<Friends>("friend", Friends)
			}
		}
	}
}
```

To refer to a dynamic resource on the server-side, you can use the regular Kotlin `.` notation: `Api2.Users.User.Friends.Friend`.

When implementing a dynamic resource on the server-side, you can access the value of the path parameter using the method [`idOf`](api/-server-side%20typesafe%20-spine%20schema%20usage/opensavvy.spine.server/-typed-response-scope/id-of.md):

```kotlin
route(Api2.Users.User.Friends.Friend) {
	val userId = idOf(Api2.Users.User)
	val friendId = idOf(Api2.Users.User.Friends.Friend)

	// …
}
```

When calling a dynamic resource on the client-side, you can use the slash notation with member imports: `Api2 / Users / User("123") / Friends / Friend("456")`.

Resources describe the overall structure of the API. Within each resource, you can declare [endpoints](endpoints.md) to describe the sent and received data.
