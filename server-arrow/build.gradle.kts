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
		api(projects.server)
		api(projects.serverArrowIndependent)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.testBalloon)
		implementation(libsCommon.opensavvy.prepared.arrow)
		implementation(libsCommon.opensavvy.prepared.ktor)
		implementation(libsCommon.kotlin.test)
		implementation(libs.ktor.server.contentNegotiation)
		implementation(libs.ktor.client.contentNegotiation)
		implementation(libs.ktor.kotlinxJson)
		implementation(projects.clientArrow)
	}

	sourceSets.all {
		languageSettings {
			enableLanguageFeature("ContextParameters")
		}
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
