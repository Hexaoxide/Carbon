plugins {
  id("carbon.platform-conventions")
  id("com.github.johnrengelman.shadow")
}

val shadowPlatform = extensions.create<CarbonShadowPlatformExtension>("carbonShadowPlatform")

tasks {
  jar {
    archiveClassifier.set("unshaded")
  }
  shadowJar {
    archiveClassifier.set(null as String?)
    configureShadowJar()
    mergeServiceFiles()
  }
}

extensions.configure<CarbonPlatformExtension> {
  jarTask.set(tasks.shadowJar)
}

afterEvaluate {
  tasks.shadowJar {
    if (shadowPlatform.relocateCloud.get()) {
      relocateCloud()
    }
  }
}
