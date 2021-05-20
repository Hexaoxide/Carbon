plugins {
  `kotlin-dsl`
  `kotlin-dsl-precompiled-script-plugins`
}

repositories {
  gradlePluginPortal()
  maven("https://repo.jpenilla.xyz/snapshots/") // todo: for shadow to be compatible with Records
}

dependencies {
  // sadly can't use the version catalog here (?), need to keep in sync manually
  //implementation("gradle.plugin.com.github.jengelman.gradle.plugins", "shadow", "7.0.0") // temporary until shadow bumps jdependency for Record support
  implementation("com.github.johnrengelman", "shadow", "7.1.0-hexaoxide-SNAPSHOT")  // temporary until shadow bumps jdependency for Record support
}
