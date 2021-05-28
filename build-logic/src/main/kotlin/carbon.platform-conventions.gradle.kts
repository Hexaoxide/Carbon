plugins {
  id("carbon.base-conventions")
}

val platformExtension = extensions.create<CarbonPlatformExtension>("carbonPlatformExtension", project)

tasks {
  val copyJar = register<FileCopyTask>("copyJar") {
    fileToCopy.set(platformExtension.jarTask.flatMap { it.archiveFile })
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn(platformExtension.jarTask)
  }
  build {
    dependsOn(copyJar)
  }
}

val projectVersion = version as String
if (projectVersion.endsWith("-SNAPSHOT")) {
  // Add git commit hash to version for platforms
  version = "$projectVersion+${indraGit.commit()?.name?.substring(0, 7) ?: error("Failed to retrieve git commit hash")}"
}
