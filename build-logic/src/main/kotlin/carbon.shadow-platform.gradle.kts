plugins {
  id("carbon.platform-conventions")
  id("com.gradleup.shadow")
}

tasks {
  jar {
    archiveClassifier = "unshaded"
  }
  shadowJar {
    archiveClassifier.set(null as String?)
    configureShadowJar()
    mergeServiceFiles()
  }
}

extensions.configure<CarbonPlatformExtension> {
  productionJar = tasks.shadowJar.flatMap { it.archiveFile }
}
