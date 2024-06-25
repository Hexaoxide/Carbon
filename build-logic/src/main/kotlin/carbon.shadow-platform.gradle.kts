plugins {
  id("carbon.platform-conventions")
  id("io.github.goooler.shadow")
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
