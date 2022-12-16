plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  maven("https://repo.stellardrift.ca/repository/snapshots/")
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  implementation(libs.shadow)
  implementation(libs.indraCommon)
  implementation(libs.indraLicenseHeader)
  implementation(libs.licenser)
  implementation("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:1.4.0")

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
