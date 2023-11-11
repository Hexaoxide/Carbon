plugins {
  id("carbon.shadow-platform")
  id("xyz.jpenilla.run-velocity")
}

dependencies {
  implementation(projects.carbonchatCommon)
  implementation(libs.bstatsVelocity)

  compileOnly(libs.velocityApi)

  implementation(libs.cloudVelocity)
  compileOnly(libs.miniplaceholders)

  runtimeDownload(libs.mysql)
}

gremlin {
  defaultJarRelocatorDependencies.set(true)
}

tasks {
  shadowJar {
    relocateCloud()
    standardRuntimeRelocations()
    relocateDependency("com.google.inject.assistedinject")
    relocateDependency("io.leangen.geantyref")
  }
  writeDependencies {
    standardRuntimeRelocations()
    relocateDependency("com.google.inject.assistedinject")
    relocateDependency("io.leangen.geantyref")
  }
  runVelocity {
    velocityVersion(libs.versions.velocityApi.get())
    downloadPlugins {
      url("https://download.luckperms.net/1521/velocity/LuckPerms-Velocity-5.4.108.jar")
    }
  }
  processResources {
    replace("velocity-plugin.json", mapOf(
      "id" to rootProject.name.lowercase(),
      "name" to rootProject.name,
      "version" to project.version,
      "description" to project.description,
      "url" to GITHUB_REPO_URL
    ))
  }
}

modrinth {
  optional.project("unsignedvelocity")
}

configurations.runtimeDownload {
  exclude("org.checkerframework", "checker-qual")
}
