@file:OptIn(ExperimentalTraceApi::class)

package opensavvy.spine.typed.server

import arrow.core.raise.ExperimentalTraceApi
import arrow.core.raise.Raise
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import opensavvy.prepared.compat.arrow.core.failOnRaise
import opensavvy.prepared.compat.ktor.preparedClient
import opensavvy.prepared.compat.ktor.preparedServer
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.map
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.random.nextInt
import opensavvy.prepared.suite.random.random
import opensavvy.prepared.suite.random.randomInt
import opensavvy.spine.api.*
import opensavvy.spine.api.Parameters
import opensavvy.spine.client.arrow.body
import opensavvy.spine.client.bodyOrThrow
import opensavvy.spine.client.handle
import opensavvy.spine.client.request
import opensavvy.spine.server.fail
import opensavvy.spine.server.respond
import opensavvy.spine.server.route
import opensavvy.spine.typed.server.Routes.Users
import opensavvy.spine.typed.server.Routes.Users.User
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

// region API declaration

@Serializable
private data class UserDto(val id: String, val name: String, val enabled: Boolean)

@Serializable
private data class NotFound(val id: String) {

	companion object : FailureCompanion<NotFound>(HttpStatusCode.NotFound)
}

@Serializable
private data class AlreadyExists(val id: String)

@Serializable
private data class NotAllowed(val reason: String) {

	companion object : FailureCompanion<NotAllowed>(HttpStatusCode.Forbidden)
}

private class UserSearchParams(data: ParameterStorage) : Parameters(data) {
	var includeDisabled by parameter(false)
}

private object Routes : RootResource("routes") {

	object Users : StaticResource<Routes>("users", Routes) {

		val list by get()
			.response<List<UserDto>>()
			.parameters(::UserSearchParams)

		val create by post()
			.request<UserDto>()
			.failure<AlreadyExists>(HttpStatusCode.Conflict)

		object User : DynamicResource<Users>("user", Users) {

			val get by get()
				.response<UserDto>()
				.failure(NotFound)

			val delete by delete()
				.failure(NotFound)
				.failure(NotAllowed)
		}
	}
}

// endregion
// region Server-side implementation

private val server by preparedServer {
	install(ServerContentNegotiation) {
		json()
	}

	val data = ArrayList<UserDto>()
	val dataLock = Mutex()

	routing {
		route(Users.list) {
			respond(
				dataLock.withLock("list") { data.filter { it.enabled || parameters.includeDisabled } }
			)
		}

		route(Users.create) {
			dataLock.withLock("create $body") {
				if (data.any { it.id == body.id }) {
					fail(AlreadyExists(body.id))
				}
				data += body
			}
			respond(Created)
		}

		route(User.get) {
			val id = idOf(User)

			val user = dataLock.withLock("get $id") { data.find { it.id == id } }

			if (user == null) {
				fail(NotFound(id))
			}

			respond(user)
		}

		route(User.delete) {
			val id = idOf(User)

			dataLock.withLock("delete $id") { data.removeAll { it.id == id } }

			respond()
		}
	}
}

// endregion
// region Client-side implementation

val client by server.preparedClient {
	install(ClientContentNegotiation) {
		json()
	}
}

private suspend fun HttpClient.listUsers(includeDisabled: Boolean = false) = request(
	endpoint = Routes / Users / Users.list,
	parameters = {
		this.includeDisabled = includeDisabled
	},
).bodyOrThrow()

private suspend fun HttpClient.createUser(user: UserDto) = request(Routes / Users / Users.create, user).handle(
	handle1 = { throw RuntimeException("Could not find user ${it.id}") },
	transform = { },
)

private suspend fun HttpClient.getUser(id: String) = request(Routes / Users / User(id) / User.get).handle(
	handle1 = { null },
	transform = { it },
)

context(_: Raise<NotFound>, _: Raise<NotAllowed>)
private suspend fun HttpClient.deleteUser(id: String) = request(Routes / Users / User(id) / User.delete).body()

// endregion

fun SuiteDsl.routeTest() = suite("Route test") {
	val userId by randomInt(0, 999).map { it.toString() }

	test("Listing users when there are no users should return an empty list") {
		check(client().listUsers(includeDisabled = false) == emptyList<UserDto>())
	}

	test("Listing users when there are no users should return an empty list, even if we want to access disabled users") {
		check(client().listUsers(includeDisabled = true) == emptyList<UserDto>())
	}

	test("Creating a user") {
		client().createUser(UserDto(userId(), "test", true))
	}

	test("Cannot create two users with the same ID") {
		client().createUser(UserDto(userId(), "test", true))

		val e = checkThrows<RuntimeException> {
			client().createUser(UserDto(userId(), "test", true))
		}
		check(e.message == "Could not find user ${userId()}")
	}

	val enabledUser by prepared {
		UserDto(random.nextInt(0, 999).toString(), "enabled user", true)
			.also { client().createUser(it) }
	}

	val disabledUser by prepared {
		UserDto(random.nextInt(0, 999).toString(), "disabled user", false)
			.also { client().createUser(it) }
	}

	test("Listing enabled users") {
		enabledUser()
		disabledUser()

		check(client().listUsers(includeDisabled = false) == listOf(enabledUser()))
	}

	test("Listing all users") {
		enabledUser()
		disabledUser()

		check(client().listUsers(includeDisabled = true) == listOf(enabledUser(), disabledUser()))
	}

	test("Accessing the details of a user") {
		val user = enabledUser()

		check(client().getUser(user.id) == user)
	}

	test("Deleting a user") {
		val user = enabledUser()

		failOnRaise { client().deleteUser(user.id) }
		check(client().listUsers() == emptyList<UserDto>())
	}
}
