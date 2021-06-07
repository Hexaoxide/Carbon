plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  maven("https://repo.jpenilla.xyz/snapshots/") // todo: for shadow snapshot
}

dependencies {
  implementation(libs.shadow)
  implementation(libs.indraCommon)

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
