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
		api(projects.spineShared)

		api(projects.typedClient)
		api(projects.safeClient)
	}

	sourceSets.commonTest.dependencies {
		implementation(libs.prepared)
		implementation(libs.prepared.arrow)
	}
}

library {
	name.set("Spine (server-side)")
	description.set("Typesafe HTTP APIs with Ktor and Arrow")
	homeUrl.set("https://gitlab.com/opensavvy/spine")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
