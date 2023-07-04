import java.util.Locale

plugins {
  id("carbon.shadow-platform")
  id("net.kyori.blossom")
  id("xyz.jpenilla.run-velocity")
}

dependencies {
  implementation(projects.carbonchatCommon)

  compileOnly(libs.velocityApi)
  annotationProcessor(libs.velocityApi)

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
}

blossom {
  mapOf(
    "ID" to rootProject.name.lowercase(Locale.ROOT),
    "NAME" to rootProject.name,
    "VERSION" to version,
    "DESCRIPTION" to description,
    "URL" to GITHUB_REPO_URL
  ).forEach { (k, v) ->
    replaceToken("$[$k]", v)
  }
}

modrinth {
  optional.project("unsignedvelocity")
}
