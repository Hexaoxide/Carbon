enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
