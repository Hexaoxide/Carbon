plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  // sadly can't use the version catalog here (?), need to keep in sync manually
  implementation("gradle.plugin.com.github.jengelman.gradle.plugins", "shadow", "7.0.0")
}
