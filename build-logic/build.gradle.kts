plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  mavenCentral()
  maven("https://repo.stellardrift.ca/repository/snapshots/")
}

dependencies {
  implementation(libs.shadow)
  implementation(libs.indraCommon)
  implementation(libs.indraLicenseHeader)
  implementation(libs.pluginYml)
  implementation(libs.minotaur)
  implementation(libs.configurateYaml)
  implementation(libs.gremlin.gradle)
  compileOnly("com.fasterxml.jackson.core:jackson-core:2.14.0")
  compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.14.0")

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
