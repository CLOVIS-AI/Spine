plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(libsCommon.plugins.kotlinx.serialization)
}

kotlin {
	jvm()
	linuxX64()

	sourceSets.commonMain.dependencies {
		api(libs.ktor.server.core)
		api(libs.arrow.core)
		api(libs.kotlinx.coroutines)
	}

	sourceSets.commonTest.dependencies {
		implementation(libs.prepared)
		implementation(libs.prepared.arrow)
		implementation(libs.prepared.ktor)
		implementation(libs.ktor.server.contentNegotiation)
		implementation(libs.ktor.client.contentNegotiation)
		implementation(libs.ktor.kotlinxJson)
	}
}

library {
	name.set("Server-side Arrow helpers")
	description.set("Declared Arrow Typed Errors in your Ktor servers")
	homeUrl.set("https://gitlab.com/opensavvy/groundwork/spine")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
