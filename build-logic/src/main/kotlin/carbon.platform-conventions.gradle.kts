plugins {
  id("carbon.base-conventions")
}

val platformExtension = extensions.create<CarbonPlatformExtension>("carbonPlatform", project)

tasks {
  val copyJar = register<FileCopyTask>("copyJar") {
    fileToCopy.set(platformExtension.jarTask.flatMap { it.archiveFile })
    destination.set(rootProject.layout.buildDirectory.dir("libs").flatMap {
      it.file(fileToCopy.map { file -> file.asFile.name })
    })
  }
  build {
    dependsOn(copyJar)
  }
}

//val projectVersion = version as String
//if (projectVersion.endsWith("-SNAPSHOT")) {
//  // Add git commit hash to version for platforms
//  val gitHash = latestGitHash() ?: error("Failed to retrieve git commit hash")
//  version = "$projectVersion+$gitHash"
//}
