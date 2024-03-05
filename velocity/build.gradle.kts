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
      url("https://download.luckperms.net/1532/velocity/LuckPerms-Velocity-5.4.119.jar")
      github("MiniPlaceholders", "MiniPlaceholders", libs.versions.miniplaceholders.get(), "MiniPlaceholders-Velocity-${libs.versions.miniplaceholders.get()}.jar")
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

publishMods.modrinth {
  modLoaders.addAll("velocity")
  optional {
    slug = "signedvelocity"
  }
}

configurations.runtimeDownload {
  exclude("org.checkerframework", "checker-qual")
}
