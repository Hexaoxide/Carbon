plugins {
  id("carbon.base-conventions")
  id("com.modrinth.minotaur")
}

val platformExtension = extensions.create<CarbonPlatformExtension>("carbonPlatform")

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

val projectVersion = project.version as String

modrinth {
  projectId.set("QzooIsZI")
  versionType.set(if (projectVersion.contains("-beta.")) "beta" else "release")
  file.set(platformExtension.jarTask.flatMap { it.archiveFile })
  changelog.set(releaseNotes)
  token.set(providers.environmentVariable("MODRINTH_TOKEN"))
  required.project("luckperms")
  optional.project("miniplaceholders")
  if (project.name == "carbon-velocity") {
      optional.project("unsignedvelocity")
  }
  gameVersions.add("1.19.4")
}

//val projectVersion = version as String
//if (projectVersion.endsWith("-SNAPSHOT")) {
//  // Add git commit hash to version for platforms
//  val gitHash = latestGitHash() ?: error("Failed to retrieve git commit hash")
//  version = "$projectVersion+$gitHash"
//}
