import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml.Load
import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("carbon.shadow-platform")
  id("xyz.jpenilla.resource-factory") version "1.1.1"
  id("xyz.jpenilla.resource-factory-paper-convention") version "1.1.1"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
  id("xyz.jpenilla.run-paper")
  id("carbon.permissions")
  id("carbon.configurable-plugins")
}

dependencies {
  implementation(projects.carbonchatCommon)

  // Server
  compileOnly(libs.foliaApi)
  implementation(libs.paperTrail)

  // Commands
  implementation(libs.cloudPaper)
  implementation(libs.cloudPaperSigned)

  // Misc
  implementation(libs.bstatsBukkit)

  // Plugins
  compileOnly(libs.placeholderapi)
  compileOnly(libs.miniplaceholders)
  compileOnly(libs.essentialsXDiscord) {
    exclude("org.spigotmc", "spigot-api")
  }
  compileOnly(libs.discordsrv) {
    isTransitive = false
  }
  compileOnly(libs.towny)
  compileOnly(libs.mcmmo) {
    isTransitive = false
  }
  compileOnly(libs.factionsUuid)
}

configurablePlugins {
  dependency(libs.towny)
  dependency(libs.mcmmo)
  dependency(libs.factionsUuid)
}

tasks {
  shadowJar {
    relocateDependency("io.papermc.papertrail")
    relocateDependency("io.leangen.geantyref")
    relocateDependency("xyz.jpenilla.reflectionremapper")
    relocateDependency("net.fabricmc.mappingio")
    relocateCloud()
  }
  withType(RunServer::class).configureEach {
    version.set(libs.versions.minecraft)
    downloadPlugins {
      url("https://download.luckperms.net/1543/bukkit/loader/LuckPerms-Bukkit-5.4.130.jar")
      github("MiniPlaceholders", "MiniPlaceholders", libs.versions.miniplaceholders.get(), "MiniPlaceholders-Paper-${libs.versions.miniplaceholders.get()}.jar")
      github("MiniPlaceholders", "PlaceholderAPI-Expansion", "1.2.0", "PlaceholderAPI-Expansion-1.2.0.jar")
      hangar("PlaceholderAPI", libs.versions.placeholderapi.get())
    }
  }
  register<RunServer>("runServer2") {
    pluginJars.from(shadowJar.flatMap { it.archiveFile })
    runDirectory.set(layout.projectDirectory.dir("run2"))
  }
}

runPaper.folia.registerTask()

paperPluginYaml {
  name = rootProject.name
  loader = "net.draycia.carbon.paper.CarbonPaperLoader"
  main = "net.draycia.carbon.paper.CarbonPaperBootstrap"
  apiVersion = "1.20"
  authors = listOf("Draycia", "jmp")
  website = GITHUB_REPO_URL
  foliaSupported = true

  dependencies {
    server("LuckPerms", Load.BEFORE, true)
    server("PlaceholderAPI", Load.BEFORE, false)
    server("EssentialsDiscord", Load.BEFORE, false)
    server("DiscordSRV", Load.BEFORE, false)
    server("MiniPlaceholders", Load.BEFORE, false)

    // Integrations
    server("Towny", Load.BEFORE, false)
    server("mcMMO", Load.BEFORE, false)
    server("Factions", Load.BEFORE, false)
  }
}

bukkitPluginYaml {
  name = rootProject.name
  main = "carbonchat.libs.io.papermc.papertrail.RequiresPaperPlugins"
  apiVersion = "1.20"
  authors = listOf("Draycia", "jmp")
  website = GITHUB_REPO_URL
}

carbonPermission.permissions.get().forEach {
  setOf(bukkitPluginYaml.permissions, paperPluginYaml.permissions).forEach { container ->
    container.register(it.string) {
      description = it.description
      it.children?.let { children.putAll(it) }
    }
  }
}

publishMods.modrinth {
  modLoaders.addAll("paper", "folia")
}

configurations.runtimeDownload {
  exclude("org.checkerframework", "checker-qual")
}
