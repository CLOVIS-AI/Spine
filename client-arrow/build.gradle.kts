plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()
	js {
		browser()
		nodejs()
	}
	linuxX64()
	iosArm64()
	iosSimulatorArm64()
	iosX64()

	sourceSets.commonMain.dependencies {
		api(projects.client)
		api(libs.arrow.core)
		api(libs.kotlinx.coroutines)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.kotest)
		implementation(libsCommon.opensavvy.prepared.arrow)
	}

	sourceSets.all {
		languageSettings {
			enableLanguageFeature("ContextParameters")
		}
	}
}

library {
	name.set("Client-side typesafe Spine schema usage (with Arrow typed errors)")
	description.set("Declare your Ktor client-side API from an HTTP schema shared with the server")
	homeUrl.set("https://gitlab.com/opensavvy/groundwork/spine")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
