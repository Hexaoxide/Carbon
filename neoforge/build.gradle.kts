plugins {
  id("carbon.shadow-platform")
  id("net.neoforged.moddev")
}

neoForge {
  enable {
    version = libs.versions.neoforge.get()
  }
  mods.register("carbonchat") {
    sourceSet(sourceSets.main.get())
    sourceSet(project(":carbonchat-mod-common").sourceSets.main.get())
  }
  runs.register("server") {
    server()
  }
}

val shade: Configuration by configurations.creating

configurations.implementation {
  extendsFrom(shade)
}

shade.extendsFrom(configurations.runtimeDownload.get())

dependencies {
  shade(projects.carbonchatCommon) {
    exclude("net.kyori", "adventure-api")
    exclude("net.kyori", "adventure-text-serializer-gson")
    exclude("net.kyori", "adventure-text-serializer-plain")
    exclude("org.incendo", "cloud-core")
    exclude("org.incendo", "cloud-services")
    exclude("org.incendo", "cloud-brigadier")
    exclude("org.incendo", "cloud-minecraft-signed-arguments")
    exclude("io.leangen.geantyref")
  }

  implementation(libs.cloudNeoforge)
  jarJar(libs.cloudNeoforge)
  implementation(libs.cloudSigned)
  jarJar(libs.cloudSigned)

  implementation(libs.adventurePlatformNeoforge)
  jarJar(libs.adventurePlatformNeoforge)

  // implementation(libs.miniplaceholders)

  runtimeDownload(libs.mysql)
  // jarJar(libs.jarRelocator)
  // runtimeOnly(libs.jarRelocator) {
  //   isTransitive = false
  // }
  runtimeDownload(libs.checkerQual)
}

val prodJar = tasks.register<Zip>("productionJar") {
  destinationDirectory = layout.buildDirectory.dir("libs")
  archiveClassifier = ""
  archiveExtension = "jar"
  from(zipTree(tasks.shadowJar.flatMap { it.archiveFile }))
  from(tasks.jarJar.flatMap { it.outputDirectory })
}

carbonPlatform {
  productionJar = prodJar.flatMap { it.archiveFile }
}

tasks {
  shadowJar {
    archiveClassifier = "dev-all"
    configurations = listOf(shade)
    relocateDependency("org.incendo.cloud.minecraft.extras")
    standardRuntimeRelocations()
    relocateGuice()
    relocateDependency("org.checkerframework")
  }
  writeDependencies {
    standardRuntimeRelocations()
    relocateGuice()
    relocateDependency("org.checkerframework")
  }
  processResources {
    replace("META-INF/neoforge.mods.toml", mapOf(
      "modId" to rootProject.name.lowercase(),
      "name" to rootProject.name,
      "version" to project.version,
      "description" to project.description,
      "github_url" to GITHUB_REPO_URL
    ))
  }
  jar {
    archiveClassifier = "dev"
  }
}

publishMods.modrinth {
  minecraftVersions.set(listOf(libs.versions.minecraft.get()))
  modLoaders.addAll("neoforge")
}
