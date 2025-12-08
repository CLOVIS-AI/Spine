package opensavvy.spine.api

import opensavvy.prepared.runner.testballoon.preparedSuite

val TypedTests by preparedSuite {
	paths()
	parameters()
	resolvedRouteTests()
}
