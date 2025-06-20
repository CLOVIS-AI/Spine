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
		api(libs.ktor.http)
	}

	sourceSets.commonTest.dependencies {
		implementation(libsCommon.opensavvy.prepared.kotest)
		implementation(libsCommon.opensavvy.prepared.arrow)
	}
}

library {
	name.set("Multiplatform Ktor schema declaration")
	description.set("Declare your Ktor API in code shared between your clients and servers")
	homeUrl.set("https://gitlab.com/opensavvy/groundwork/spine")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}

dokka.dokkaSourceSets.configureEach {
	skipDeprecated.set(false)
}
