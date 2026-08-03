import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  id("carbon.shadow-platform")
  id("xyz.jpenilla.run-velocity")
  alias(libs.plugins.resource.factory.velocity.convention)
}

indra {
  javaVersions {
    target(25)
  }
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
  implementation(libs.assistedInject)
}

velocityPluginJson {
  id = rootProject.name.lowercase()
  main = "net.draycia.carbon.velocity.CarbonVelocityBootstrap"
  name = rootProject.name
  version = project.version.toString()
  description = project.description
  url = GITHUB_REPO_URL
  authors = listOf("Draycia", "jmp")
  dependency("luckperms", false)
  dependency("miniplaceholders", true)
  dependency("signedvelocity", true)
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
  val luckperms = FetchLuckPermsJar.setup(project, "velocity")
  runVelocity {
    velocityVersion(libs.versions.velocityApi.get())
    pluginJars.from(prod)
    pluginJars.from(luckperms.flatMap { it.outputFile })
    downloadPlugins {
      github("MiniPlaceholders", "MiniPlaceholders", libs.versions.miniplaceholders.get(), "MiniPlaceholders-Velocity-${libs.versions.miniplaceholders.get()}.jar")
    }
  }
}

publishMods.modrinth {
  modLoaders.addAll("velocity")
  optional {
    slug = "signedvelocity"
  }
}
