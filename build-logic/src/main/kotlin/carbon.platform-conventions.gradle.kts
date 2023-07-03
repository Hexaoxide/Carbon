plugins {
  id("carbon.base-conventions")
  id("com.modrinth.minotaur")
}

val runtimeDownload: Configuration by configurations.creating {
  isCanBeResolved = true
  isCanBeConsumed = false
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
  }
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
  gameVersions.addAll("1.19.4", "1.20.1")
}

val writeDeps = tasks.register("writeDependencies", WriteDependencies::class) {
  tree.set(runtimeDownload.incoming.resolutionResult.rootComponent)
  files.from(runtimeDownload)
  outputFileName.set("carbon-dependencies.list")
  outputDir.set(layout.buildDirectory.dir("generated/dependencyList"))
  repos.add("https://repo.papermc.io/repository/maven-public/")
  repos.add("https://repo.maven.apache.org/maven2/")
}

sourceSets.main {
  resources {
    srcDir(writeDeps)
  }
}

//val projectVersion = version as String
//if (projectVersion.endsWith("-SNAPSHOT")) {
//  // Add git commit hash to version for platforms
//  val gitHash = latestGitHash() ?: error("Failed to retrieve git commit hash")
//  version = "$projectVersion+$gitHash"
//}
