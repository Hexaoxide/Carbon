import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  id("carbon.shadow-platform")
  id("xyz.jpenilla.run-velocity")
}

val bstats: Configuration by configurations.creating
configurations.compileOnly {
  extendsFrom(bstats)
}

dependencies {
  implementation(projects.carbonchatCommon)
  bstats(libs.bstatsVelocity)

  compileOnly(libs.velocityApi)

  implementation(libs.cloudVelocity)
  compileOnly(libs.miniplaceholders)

  runtimeDownload(libs.mysql)

  compileOnly("javax.inject:javax.inject:1")
}

gremlin {
  defaultJarRelocatorDependencies.set(true)
}

runVelocityExtension.detectPluginJar = false

tasks {
  val bStatsJar = register<ShadowJar>("bStatsShadowJar") {
    archiveClassifier = "bStats"
    configurations = listOf(bstats)
    relocateDependency("org.bstats")
  }
  shadowJar {
    archiveClassifier = "shadowJar"
    relocateCloud()
    standardRuntimeRelocations()
    relocateDependency("io.leangen.geantyref")
    relocateGuice()
  }
  val prod = register<Zip>("productionJar") {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    archiveFileName.set("carbonchat-velocity-${project.version}.jar")
    from(zipTree(shadowJar.flatMap { it.archiveFile }))
    from(zipTree(bStatsJar.flatMap { it.archiveFile })) {
      exclude("META-INF/**")
    }
  }
  carbonPlatform.productionJar = prod.flatMap { it.archiveFile }
  writeDependencies {
    standardRuntimeRelocations()
    relocateDependency("io.leangen.geantyref")
    relocateGuice()
  }
  runVelocity {
    velocityVersion(libs.versions.velocityApi.get())
    pluginJars.from(prod)
    downloadPlugins {
      url("https://download.luckperms.net/1533/velocity/LuckPerms-Velocity-5.4.120.jar")
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
