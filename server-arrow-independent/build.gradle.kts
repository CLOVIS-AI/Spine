plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(libsCommon.plugins.kotlinx.serialization)
	alias(libsCommon.plugins.testBalloon)
}

kotlin {
	jvm()
	linuxX64()
	linuxArm64()
	macosX64()
	macosArm64()
	iosArm64()
	iosSimulatorArm64()
	iosX64()
	watchosX64()
	watchosArm32()
	watchosArm64()
	watchosSimulatorArm64()
	tvosX64()
	tvosArm64()
	tvosSimulatorArm64()

	sourceSets.commonMain.dependencies {
		api(libs.ktor.server.core)
		api(libs.arrow.core)
		api(libs.kotlinx.coroutines)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.opensavvy.prepared.arrow)
		implementation(libsCommon.opensavvy.prepared.ktor)
		implementation(libsCommon.kotlin.test)
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
