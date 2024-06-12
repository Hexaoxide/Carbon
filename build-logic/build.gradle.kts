plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  maven("https://oss.sonatype.org/content/repositories/snapshots/") {
    mavenContent { snapshotsOnly() }
  }
}

dependencies {
  implementation(libs.shadow)
  implementation(libs.indraCommon)
  implementation(libs.indraLicenseHeader)
  implementation(libs.mod.publish.plugin)
  implementation(libs.configurateYaml)
  implementation(libs.gremlin.gradle)
  implementation(libs.run.task)

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
