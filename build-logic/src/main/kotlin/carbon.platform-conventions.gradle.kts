plugins {
  id("carbon.base-conventions")
  id("com.modrinth.minotaur")
  id("xyz.jpenilla.gremlin-gradle")
}

decorateVersion()

configurations.runtimeDownload {
  exclude("org.slf4j", "slf4j-api")
  exclude("com.google.errorprone", "error_prone_annotations")
  exclude("io.leangen.geantyref", "geantyref")
}

val platformExtension = extensions.create<CarbonPlatformExtension>("carbonPlatform")

dependencies {
  runtimeDownload(libs.h2)
  runtimeDownload(libs.postgresql)
  runtimeDownload(libs.mariadb)
  runtimeDownload(libs.zstdjni)
  runtimeDownload(libs.jdbiCore)
  runtimeDownload(libs.jdbiObject)
  runtimeDownload(libs.jdbiPostgres)
  runtimeDownload(libs.caffeine)
  runtimeDownload(libs.jedis) {
    exclude("com.google.code.gson", "gson")
  }
  runtimeDownload(libs.rabbitmq)
  runtimeDownload(libs.nats)
  runtimeDownload(libs.assistedInject) {
    isTransitive = false
  }
  runtimeDownload(libs.flyway) {
    exclude("com.google.code.gson", "gson")
  }
  runtimeDownload(libs.flywayMysql) {
    isTransitive = false
  }
}

tasks {
  jar {
    manifest {
      attributes(
        "carbon-version" to project.version,
        "carbon-commit" to lastCommitHash(),
        "carbon-branch" to currentBranch(),
      )
    }
  }
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
  gameVersions.addAll("1.19.4", "1.20.2")
}

tasks.writeDependencies {
  outputFileName.set("carbon-dependencies.txt")
  repos.add("https://repo.papermc.io/repository/maven-public/")
  repos.add("https://repo.maven.apache.org/maven2/")
}

gremlin {
  defaultJarRelocatorDependencies.set(false)
  defaultGremlinRuntimeDependency.set(false)
}

//val projectVersion = version as String
//if (projectVersion.endsWith("-SNAPSHOT")) {
//  // Add git commit hash to version for platforms
//  val gitHash = latestGitHash() ?: error("Failed to retrieve git commit hash")
//  version = "$projectVersion+$gitHash"
//}
