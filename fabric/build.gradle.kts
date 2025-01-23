import java.util.function.Predicate
import kotlin.io.path.invariantSeparatorsPathString

plugins {
  id("carbon.shadow-platform")
  id("quiet-fabric-loom")
  alias(libs.plugins.blossom)
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
  modRuntimeOnly(libs.fabricApiDeprecated) // LuckPerms needs to work at dev time

  shade(projects.carbonchatModCommon) {
    isTransitive = false
  }
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

  modImplementation(libs.cloudFabric) {
    exclude("net.fabricmc.fabric-api")
  }
  include(libs.cloudFabric)
  implementation(libs.cloudSigned)
  include(libs.cloudSigned)
  modImplementation(libs.fabricPermissionsApi)
  include(libs.fabricPermissionsApi)

  modImplementation(libs.adventurePlatformFabric)
  include(libs.adventurePlatformFabric)

  // Until we upgrade adventure-platform-fabric
  implementation("net.kyori:option:1.1.0")
  include("net.kyori:option:1.1.0")

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

sourceSets.main {
  blossom {
    resources {
      properties.putAll(
        mapOf(
          "modId" to rootProject.name.lowercase(),
          "name" to rootProject.name,
          "version" to project.version,
          "description" to project.description,
          "githubUrl" to GITHUB_REPO_URL,
        )
      )
    }
  }
}

tasks {
  shadowJar {
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

  runServer {
    dependsOn(shadowJar)
    classpathFilter = Predicate {
      val s = it.toPath().toAbsolutePath().invariantSeparatorsPathString
      !s.contains("build/libs") && !s.contains("build/classes") && !s.contains("build/resources")
    }
    doFirst {
      val jar = shadowJar.get().archiveFile.get().asFile
      val mods = file("run/mods")
      mods.mkdirs()
      jar.copyTo(mods.resolve("carbonchat-dev.jar"), overwrite = true)
    }
  }
}

publishMods.modrinth {
  minecraftVersions.set(listOf(libs.versions.minecraft.get()))
  modLoaders.addAll("fabric")
}
