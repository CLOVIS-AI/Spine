plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(libsCommon.plugins.kotlinx.serialization)
	alias(libsCommon.plugins.testBalloon)
}

kotlin {
	jvm()
	linuxX64()

	sourceSets.commonMain.dependencies {
		api(projects.api)

		api(libs.kotlinx.coroutines)
		api(libs.ktor.server.core)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.opensavvy.prepared.arrow)
		implementation(libsCommon.opensavvy.prepared.ktor)
		implementation(libsCommon.kotlin.test)
		implementation(libs.ktor.server.contentNegotiation)
		implementation(libs.ktor.client.contentNegotiation)
		implementation(libs.ktor.kotlinxJson)
		implementation(projects.client)
		implementation(projects.clientArrow)
	}

	sourceSets.all {
		if (name.endsWith("Test")) {
			languageSettings {
				enableLanguageFeature("ContextParameters")
			}
		}
	}
}

library {
	name.set("Server-side typesafe Spine schema usage")
	description.set("Declare your Ktor server-side API from an HTTP schema shared with the client")
	homeUrl.set("https://gitlab.com/opensavvy/groundwork/spine")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
