enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://repo.racci.dev/snapshots/") {
      mavenContent {
        snapshotsOnly()
        includeGroup("ninja.egg82")
      }
    }
    // temporary cloud snapshots repo for sponge-8
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent {
        snapshotsOnly()
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
    // Velocity Proxy for run config
    ivy("https://versions.velocitypowered.com/download/") {
      patternLayout { artifact("[revision].[ext]") }
      metadataSources { artifact() }
      content { includeModule("com.velocitypowered", "velocity-proxy") }
    }
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
    // Libby (AlessioDP's fork)
    maven("https://repo.alessiodp.com/releases/")
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://repo.stellardrift.ca/repository/snapshots/")
  }
  includeBuild("build-logic")
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "6.0.1"
  id("quiet-fabric-loom") version "1.2-SNAPSHOT"
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
