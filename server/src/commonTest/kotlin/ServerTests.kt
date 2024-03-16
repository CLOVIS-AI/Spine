package opensavvy.spine.typed.server

import io.kotest.core.spec.style.StringSpec
import opensavvy.prepared.runner.kotest.preparedSuite

class ServerTests : StringSpec({
	preparedSuite {
		routeTest()
	}
})
