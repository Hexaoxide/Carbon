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
  implementation(libs.minotaur)
  implementation(libs.configurateYaml)
  implementation(libs.gremlin.gradle)

  implementation(libs.pluginYml)
  // Implementation dependencies of plugin-yml
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.3")

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
