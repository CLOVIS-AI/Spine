plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(libsCommon.plugins.kotlinx.serialization)
}

kotlin {
	jvm()
	linuxX64()

	sourceSets.commonMain.dependencies {
		api(projects.server)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.kotest)
		implementation(libsCommon.opensavvy.prepared.arrow)
		implementation(libsCommon.opensavvy.prepared.ktor)
		implementation(libs.ktor.server.contentNegotiation)
		implementation(libs.ktor.client.contentNegotiation)
		implementation(libs.ktor.kotlinxJson)
		implementation(projects.clientArrow)
	}
}

library {
	name.set("Server-side typesafe Spine schema usage (with Arrow typed errors)")
	description.set("Declare your Ktor server-side API from an HTTP schema shared with the client")
	homeUrl.set("https://gitlab.com/opensavvy/groundwork/spine")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
