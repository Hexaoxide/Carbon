import org.apache.tools.ant.filters.ReplaceTokens

plugins {
  id("carbon.shadow-platform")
  id("quiet-fabric-loom")
}

val carbon: Configuration by configurations.creating

configurations.implementation {
  extendsFrom(carbon)
}

dependencies {
  minecraft(libs.fabricMinecraft)
  mappings(loom.officialMojangMappings())
  modImplementation(libs.fabricLoader)
  modImplementation("net.fabricmc.fabric-api:fabric-api:0.83.0+1.20.1")
  modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:0.83.0+1.20.1") // LuckPerms needs to work at dev time

  carbon(projects.carbonchatCommon) {
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
  modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")

  modImplementation(libs.adventurePlatformFabric)
  include(libs.adventurePlatformFabric)

  modImplementation(libs.miniplaceholders)

  runtimeDownload(libs.jdbiCore)
  runtimeDownload(libs.jdbiObject)
  runtimeDownload(libs.jdbiPostgres)
  runtimeDownload(libs.postgresql)
  runtimeDownload(libs.mysql)
  runtimeDownload(libs.zstdjni)
  include(libs.jarRelocator)
  runtimeOnly(libs.jarRelocator) {
    isTransitive = false
  }
}

carbonPlatform {
  jarTask.set(tasks.remapJar)
}

carbonShadowPlatform {
  relocateGuice.set(true)
  relocateCloud.set(false)
}

tasks {
  shadowJar {
    configurations = arrayListOf(carbon) as List<FileCollection>
    relocateDependency("cloud.commandframework.minecraft.extras")
    relocateDependency("com.github.luben.zstd")
  }
  writeDependencies {
    relocateDependency("org.postgresql")
    relocateDependency("com.github.luben.zstd")
    relocateDependency("com.google.protobuf")
    relocateDependency("com.mysql.cj")
    relocateDependency("com.mysql.jdbc")

    relocateDependency("org.jdbi")
  }
  processResources {
    val props = mapOf(
      "modId" to rootProject.name.toLowerCase(),
      "name" to rootProject.name,
      "version" to project.version,
      "description" to project.description,
      "github_url" to GITHUB_REPO_URL
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
      filter<ReplaceTokens>(
        "beginToken" to "\${",
        "endToken" to "}",
        "tokens" to props
      )
    }
  }
}

modrinth {
  gameVersions.set(listOf(libs.versions.fabricMinecraft.get()))
  loaders.addAll("fabric")
}
