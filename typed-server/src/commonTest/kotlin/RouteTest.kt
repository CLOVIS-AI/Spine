package opensavvy.spine.typed.server

import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import opensavvy.prepared.compat.ktor.preparedClient
import opensavvy.prepared.compat.ktor.preparedServer
import opensavvy.prepared.suite.*
import opensavvy.spine.typed.*
import opensavvy.spine.typed.client.bodyOrThrow
import opensavvy.spine.typed.client.request
import opensavvy.spine.typed.server.Routes.Users
import opensavvy.spine.typed.server.Routes.Users.User
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

// region API declaration

@Serializable
private data class UserDto(val id: String, val name: String, val enabled: Boolean)

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

		object User : DynamicResource<Users>("user", Users) {

			val get by get()
				.response<UserDto>()

			val delete by delete()
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
			OK to dataLock.withLock("list") { data.filter { it.enabled || parameters.includeDisabled } }
		}

		route(Users.create) {
			dataLock.withLock("create $body") {
				require(data.none { it.id == body.id })
				data += body
			}
			Created to Unit
		}

		route(User.get) {
			val id = idOf(User)

			val user = dataLock.withLock("get $id") { data.find { it.id == id } }

			if (user != null) {
				OK to user
			} else {
				NotFound to TODO()
			}
		}

		route(User.delete) {
			val id = idOf(User)

			dataLock.withLock("delete $id") { data.removeAll { it.id == id } }

			NoContent to Unit
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

private suspend fun HttpClient.createUser(user: UserDto) = request(Routes / Users / Users.create, user).bodyOrThrow()

private suspend fun HttpClient.getUser(id: String) = request(Routes / Users / Users.User(id) / User.get).bodyOrThrow()

private suspend fun HttpClient.deleteUser(id: String) = request(Routes / Users / Users.User(id) / User.delete).bodyOrThrow()

// endregion

fun SuiteDsl.routeTest() = suite("Route test") {
	val userId by randomInt(0, 999).map { it.toString() }

	test("Listing users when there are no users should return an empty list") {
		client().listUsers(includeDisabled = false) shouldBe emptyList()
	}

	test("Listing users when there are no users should return an empty list, even if we want to access disabled users") {
		client().listUsers(includeDisabled = true) shouldBe emptyList()
	}

	test("Creating a user") {
		client().createUser(UserDto(userId(), "test", true))
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

		client().listUsers(includeDisabled = false) shouldBe listOf(enabledUser())
	}

	test("Listing all users") {
		enabledUser()
		disabledUser()

		client().listUsers(includeDisabled = true) shouldBe listOf(enabledUser(), disabledUser())
	}

	test("Accessing the details of a user") {
		val user = enabledUser()

		client().getUser(user.id) shouldBe user
	}

	test("Deleting a user") {
		val user = enabledUser()

		client().deleteUser(user.id)
		client().listUsers() shouldBe emptyList()
	}
}
