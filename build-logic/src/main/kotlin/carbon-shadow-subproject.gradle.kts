import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  id("com.github.johnrengelman.shadow")
}

tasks {
  named<Jar>("jar") {
    archiveClassifier.set("unshaded")
  }
  val shadowJar = named<ShadowJar>("shadowJar") {
    archiveClassifier.set(null as String?)
    configureShadowJar()
  }
  val copyJar = register<FileCopyTask>("copyJar") {
    fileToCopy.set(shadowJar.flatMap { it.archiveFile })
    destinationDirectory.set(rootProject.buildDir.resolve("libs"))
    dependsOn(shadowJar)
  }
  named<DefaultTask>("build") {
    dependsOn(copyJar)
  }
}
