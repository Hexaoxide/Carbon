enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://repo.jpenilla.xyz/snapshots/") // todo: for shadow to be compatible with Records
  }
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "5.0.0"
}

rootProject.name = "CarbonChat"

setupSubproject("carbonchat-api") {
  projectDir = file("api")
}
setupSubproject("carbonchat-common") {
  projectDir = file("common")
}
setupSubproject("carbonchat-bukkit") {
  projectDir = file("bukkit")
}
setupSubproject("carbonchat-sponge") {
  projectDir = file("sponge")
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
  include(name)
  project(":$name").apply(block)
}
