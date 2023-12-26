plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.internal)
}

kotlin {
	jvm()
	js {
		browser()
		nodejs()
	}
	linuxX64()
	// iosArm64()
	// iosSimulatorArm64()
	// iosX64()

	sourceSets.commonMain.dependencies {
		api(projects.spineShared)
	}
}
