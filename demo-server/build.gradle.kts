plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.internal)
}

kotlin {
	jvm()
	linuxX64()

	sourceSets.commonMain.dependencies {
		api(projects.spineServer)
		api(projects.demoShared)
	}
}
