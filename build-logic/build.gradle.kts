plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  maven("https://repo.jpenilla.xyz/snapshots/") // todo: for shadow snapshot
}

// sadly can't use the version catalog here (?), need to keep in sync manually
dependencies {
  // temporary until shadow bumps jdependency for Record support
  //implementation("gradle.plugin.com.github.jengelman.gradle.plugins", "shadow", "7.0.0")
  implementation("com.github.johnrengelman", "shadow", "7.1.0-hexaoxide-SNAPSHOT")

  val indraVersion = "2.0.5"
  implementation("net.kyori", "indra-common", indraVersion)
  implementation("net.kyori", "indra-git", indraVersion)
}
