package opensavvy.spine.server.arrow.independent

import arrow.core.raise.ensureNotNull
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import opensavvy.prepared.compat.ktor.preparedClient
import opensavvy.prepared.compat.ktor.preparedServer
import opensavvy.prepared.runner.testballoon.preparedSuite
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

val ServerRaiseTest by preparedSuite {

	val server by preparedServer {
		install(ServerContentNegotiation) {
			json()
		}

		routing {
			put("/product") {
				raise {
					val first = call.parameters["first"]
					val second = call.parameters["second"]
					ensureNotNull(first) { HttpFailure("Missing first argument", HttpStatusCode.UnprocessableEntity) }
					ensureNotNull(second) { HttpFailure("Missing second argument", HttpStatusCode.UnprocessableEntity) }

					val firstParsed = first.toIntOrNull()
					val secondParsed = second.toIntOrNull()
					ensureNotNull(firstParsed) { HttpFailure("The first argument should be an integer: '$first'", HttpStatusCode.UnprocessableEntity) }
					ensureNotNull(secondParsed) { HttpFailure("The second argument should be an integer: '$second'", HttpStatusCode.UnprocessableEntity) }

					call.respond(firstParsed * secondParsed)
				}
			}
		}
	}

	val client by server.preparedClient {
		install(ClientContentNegotiation) {
			json()
		}
	}

	test("Product of two integers") {
		check(client().put("/product?first=6&second=2").body<Int>() == 12)
	}

}
