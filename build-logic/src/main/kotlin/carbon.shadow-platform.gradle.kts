plugins {
  id("carbon.platform-conventions")
  id("com.github.johnrengelman.shadow")
}

tasks {
  jar {
    archiveClassifier.set("unshaded")
  }
  shadowJar {
    archiveClassifier.set(null as String?)
    configureShadowJar()
  }
}

extensions.configure<CarbonPlatformExtension> {
  jarTask.set(tasks.shadowJar)
}
