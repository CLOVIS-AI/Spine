package opensavvy.spine.api

import io.kotest.core.spec.style.StringSpec
import opensavvy.prepared.runner.kotest.preparedSuite

class TypedTests : StringSpec({
	preparedSuite {
		paths()
		parameters()
		resolvedRouteTests()
	}
})
