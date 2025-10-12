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
    // Needed for mergeServiceFiles to work properly in Shadow 9+
    filesMatching("META-INF/services/**") {
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
  }
}

extensions.configure<CarbonPlatformExtension> {
  productionJar = tasks.shadowJar.flatMap { it.archiveFile }
}
