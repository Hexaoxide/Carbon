import kotlin.io.path.invariantSeparatorsPathString

plugins {
  id("carbon.shadow-platform")
  id("quiet-fabric-loom")
}

val shade: Configuration by configurations.creating

configurations.implementation {
  extendsFrom(shade)
}

dependencies {
  minecraft(libs.fabricMinecraft)
  mappings(loom.officialMojangMappings())
  modImplementation(libs.fabricLoader)
  modImplementation(libs.fabricApi)
  modImplementation(libs.fabricApiDeprecated) // LuckPerms needs to work at dev time

  shade(projects.carbonchatCommon) {
    exclude("net.kyori", "adventure-api")
    exclude("net.kyori", "adventure-text-serializer-gson")
    exclude("net.kyori", "adventure-text-serializer-plain")
    exclude("cloud.commandframework", "cloud-core")
    exclude("cloud.commandframework", "cloud-services")
    exclude("cloud.commandframework", "cloud-brigadier")
    exclude("io.leangen.geantyref")
  }

  modImplementation(libs.cloudFabric)
  include(libs.cloudFabric)
  modImplementation(libs.fabricPermissionsApi)

  modImplementation(libs.adventurePlatformFabric)
  include(libs.adventurePlatformFabric)

  modImplementation(libs.miniplaceholders)

  runtimeDownload(libs.mysql)
  include(libs.jarRelocator)
  runtimeOnly(libs.jarRelocator) {
    isTransitive = false
  }
  runtimeDownload(libs.checkerQual)
}

carbonPlatform {
  productionJar = tasks.remapJar.flatMap { it.archiveFile }
}

tasks {
  shadowJar {
    configurations = listOf(shade)
    relocateDependency("cloud.commandframework.minecraft.extras")
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
    replace("fabric.mod.json", mapOf(
      "modId" to rootProject.name.lowercase(),
      "name" to rootProject.name,
      "version" to project.version,
      "description" to project.description,
      "github_url" to GITHUB_REPO_URL
    ))
  }

  runServer {
    dependsOn(shadowJar)
    doFirst {
      val jar = shadowJar.get().archiveFile.get().asFile
      val mods = file("run/mods")
      mods.mkdirs()
      jar.copyTo(mods.resolve("carbonchat-dev.jar"), overwrite = true)
      val newClasspath = classpath.filter {
        val s = it.toPath().toAbsolutePath().invariantSeparatorsPathString
        !s.contains("build/libs") && !s.contains("build/classes") && !s.contains("build/resources")
      }.files
      classpath = files(newClasspath)
    }
  }
}

publishMods.modrinth {
  minecraftVersions.set(listOf(libs.versions.minecraft.get()))
  modLoaders.addAll("fabric")
}
