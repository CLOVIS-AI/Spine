package opensavvy.spine.typed.server

import io.ktor.serialization.kotlinx.json.*
import opensavvy.prepared.compat.ktor.preparedClient
import opensavvy.prepared.compat.ktor.preparedServer
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.spine.api.*
import opensavvy.spine.client.bodyOrThrow
import opensavvy.spine.client.request
import opensavvy.spine.server.respond
import opensavvy.spine.server.route
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

private object MinimalSpineApi : RootResource("api") {
	object Profiles : StaticResource<MinimalSpineApi>("profiles", MinimalSpineApi) {
		object Username : DynamicResource<Profiles>("username", Profiles) {
			val get by get()
				.response<String>()
		}
	}
}

private val blankPathServer by preparedServer {
	install(ServerContentNegotiation) { json() }
	routing {
		route(MinimalSpineApi.Profiles.Username.get) {
			val id = idOf(MinimalSpineApi.Profiles.Username)
			respond(id)
		}
	}
}

private val blankPathClient by blankPathServer.preparedClient {
	install(ClientContentNegotiation) { json() }
}

fun SuiteDsl.blankPathRegressionTest() = suite("Blank path") {
	test("URL-encoded spaces should be accepted as path segments") {
		val result = blankPathClient().request(
			MinimalSpineApi / MinimalSpineApi.Profiles / MinimalSpineApi.Profiles.Username("%20") / MinimalSpineApi.Profiles.Username.get
		).bodyOrThrow()
		check(result == "%20")
	}

	test("Literal whitespace should be accepted as path segments") {
		val result = blankPathClient().request(
			MinimalSpineApi / MinimalSpineApi.Profiles / MinimalSpineApi.Profiles.Username(" ") / MinimalSpineApi.Profiles.Username.get
		).bodyOrThrow()
		check(result == " ")
	}
}
