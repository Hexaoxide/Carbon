enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent {
        snapshotsOnly()
        includeModuleByRegex("de\\.hexaoxi", "messenger-.*")
        includeModule("cloud.commandframework", "cloud-sponge")
      }
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    // PaperMC
    maven("https://repo.papermc.io/repository/maven-public/")
    // Sponge API
    maven("https://repo.spongepowered.org/repository/maven-public/")
    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
      content { includeGroup("me.clip") }
    }
    maven("https://jitpack.io") {
      content { includeGroupByRegex("com\\.github\\..*") }
    }
    // EssentialsDiscord
    maven("https://repo.essentialsx.net/releases/") {
      mavenContent {
        releasesOnly()
        includeGroup("net.essentialsx")
      }
    }
    maven("https://repo.essentialsx.net/snapshots/") {
      mavenContent {
        snapshotsOnly()
        includeGroup("net.essentialsx")
      }
    }
    // DiscordSRV
    maven("https://m2.dv8tion.net/releases")
    maven("https://nexus.scarsz.me/content/groups/public/")
    // Glare's repo for Towny
    maven("https://repo.glaremasters.me/repository/towny/") {
      content { includeGroup("com.palmergames.bukkit.towny") }
    }
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://repo.stellardrift.ca/repository/snapshots/")
  }
  includeBuild("build-logic")
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
  id("quiet-fabric-loom") version "1.4-SNAPSHOT"
}

rootProject.name = "CarbonChat"

sequenceOf(
  "api",
  "common",
  "paper",
  // "sponge", // TODO API 10
  "fabric",
  "velocity"
).forEach {
  include("carbonchat-$it")
  project(":carbonchat-$it").projectDir = file(it)
}
