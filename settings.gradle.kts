enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral {
      mavenContent { releasesOnly() }
    }
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent {
        snapshotsOnly()
        includeModuleByRegex("de\\.hexaoxi", "messenger-.*")
        includeModule("org.incendo", "cloud-sponge")
        includeModule("com.seiama", "registry")
        includeModule("com.seiama", "event-api")
      }
    }
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
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
    maven("https://nexus.scarsz.me/content/groups/public/") {
      mavenContent {
        includeGroup("com.discordsrv")
      }
    }
    // Glare's repo for Towny
    maven("https://repo.glaremasters.me/repository/towny/") {
      content { includeGroup("com.palmergames.bukkit.towny") }
    }
    // FactionsUUID
    maven("https://ci.ender.zone/plugin/repository/everything/") {
      content { includeGroup("com.massivecraft") }
    }
    // mcMMO
    maven("https://nexus.neetgames.com/repository/maven-releases/") {
      content {
        includeGroup("com.gmail.nossr50.mcMMO")
      }
    }
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://maven.fabricmc.net/")
    maven("https://repo.jpenilla.xyz/snapshots/")
  }
  includeBuild("build-logic")
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("quiet-fabric-loom") version "1.14-SNAPSHOT"
}

rootProject.name = "CarbonChat"

listOf(
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
