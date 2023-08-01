plugins {
  id("carbon.shadow-platform")
  id("xyz.jpenilla.run-velocity")
}

dependencies {
  implementation(projects.carbonchatCommon)

  compileOnly(libs.velocityApi)

  implementation(libs.cloudVelocity)
  compileOnly(libs.miniplaceholders)

  runtimeOnly(libs.jarRelocator)

  runtimeDownload(libs.mysql)
}

tasks {
  shadowJar {
    relocateCloud()
    standardRuntimeRelocations()
    relocateDependency("com.google.inject.assistedinject")
    relocateDependency("io.leangen.geantyref")
    relocateDependency("me.lucko.jarrelocator")
    relocateDependency("org.objectweb.asm")
  }
  writeDependencies {
    standardRuntimeRelocations()
    relocateDependency("com.google.inject.assistedinject")
  }
  runVelocity {
      velocityVersion(libs.versions.velocityApi.get())
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
