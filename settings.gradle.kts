enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    // Paper API
    maven("https://papermc.io/repo/repository/maven-public/")
    // Sponge API
    maven("https://repo.spongepowered.org/repository/maven-public/")
    // Velocity API
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    // Velocity Proxy for run config
    ivy("https://versions.velocitypowered.com/download/") {
      patternLayout { artifact("[revision].[ext]") }
      metadataSources { artifact() }
      content { includeModule("com.velocitypowered", "velocity-proxy") }
    }
    // cloud snapshots repo
    //maven("https://repo.incendo.org/content/repositories/snapshots") {
    //  content { includeGroup("cloud.commandframework") }
    //}
    // temporary cloud snapshots repo for sponge-8
    maven("https://repo.jpenilla.xyz/snapshots/") {
      content { includeGroup("cloud.commandframework") }
    }
    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
      content { includeGroup("me.clip") }
    }
    maven("https://jitpack.io") {
      content { includeGroupByRegex("com\\.github\\..*") }
    }
    mavenLocal()
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  includeBuild("build-logic")
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "5.0.0"
}

rootProject.name = "CarbonChat"

sequenceOf(
  "api",
  "common",
  "bukkit",
  "sponge",
  "velocity"
).forEach {
  include("carbonchat-$it")
  project(":carbonchat-$it").projectDir = file(it)
}
