plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  maven("https://repo.stellardrift.ca/repository/snapshots/")
}

dependencies {
  implementation(libs.shadow)
  implementation(libs.indraCommon)
  implementation(libs.indraLicenseHeader)
  implementation(libs.licenser)

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
