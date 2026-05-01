pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Hypixel"
			url = uri("https://repo.hypixel.net/repository/Hypixel/")
		}
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
	}
}

// Should match your modid
rootProject.name = "nopo"
