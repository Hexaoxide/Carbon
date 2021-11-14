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

  carbon(projects.carbonchatCommon) {
    exclude("net.kyori", "adventure-api")
  }

  modImplementation(libs.cloudFabric)
  include(libs.cloudFabric)
  modImplementation(libs.adventurePlatformFabric)
  include(libs.adventurePlatformFabric)
}

carbonPlatform {
  jarTask.set(tasks.remapJar)
}

tasks {
  jar {
    archiveClassifier.set("dev")
  }
  shadowJar {
    configurations = arrayListOf(carbon) as List<FileCollection>
    archiveClassifier.set("dev-all")
  }
  remapJar {
    archiveClassifier.set(null as String?)
    input.set(shadowJar.flatMap { it.archiveFile })
  }
  runServer {
    standardInput = System.`in`
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
