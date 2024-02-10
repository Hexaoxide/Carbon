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

  implementation(libs.pluginYml)
  // Implementation dependencies of plugin-yml
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1")

  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
