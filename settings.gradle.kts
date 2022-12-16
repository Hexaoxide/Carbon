enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://repo.stellardrift.ca/repository/snapshots/")
  }
  includeBuild("build-logic")
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "6.0.1"
  id("quiet-fabric-loom") version "1.0-SNAPSHOT" apply false
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

include("carbonchat-paper:internals-accessor-api")
project(":carbonchat-paper:internals-accessor-api").projectDir = file("paper/internals-accessor-api")
include("carbonchat-paper:internals-accessor-impl")
project(":carbonchat-paper:internals-accessor-impl").projectDir = file("paper/internals-accessor-impl")
