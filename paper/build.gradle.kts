import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("carbon.shadow-platform")
  id("net.minecrell.plugin-yml.bukkit")
  id("paper-plugin-yml")
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

  runtimeDownload(libs.guice) {
    exclude("com.google.guava")
  }
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
    relocateCloud()
  }
  withType(RunServer::class).configureEach {
    version.set(libs.versions.minecraft)
    downloadPlugins {
      url("https://download.luckperms.net/1521/bukkit/loader/LuckPerms-Bukkit-5.4.108.jar")
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

paper {
  name = rootProject.name
  version = project.version as String
  loader = "net.draycia.carbon.paper.CarbonPaperLoader"
  main = "net.draycia.carbon.paper.CarbonPaperBootstrap"
  apiVersion = "1.19"
  authors = listOf("Draycia", "jmp")
  website = GITHUB_REPO_URL
  foliaSupported = true

  dependency("LuckPerms", PaperPluginDescription.Load.BEFORE, true)
  dependency("PlaceholderAPI", PaperPluginDescription.Load.BEFORE, false)
  dependency("EssentialsDiscord", PaperPluginDescription.Load.BEFORE, false)
  dependency("DiscordSRV", PaperPluginDescription.Load.BEFORE, false)
  dependency("MiniPlaceholders", PaperPluginDescription.Load.BEFORE, false)

  // Integrations
  dependency("Towny", PaperPluginDescription.Load.BEFORE, false)
  dependency("mcMMO", PaperPluginDescription.Load.BEFORE, false)
  dependency("Factions", PaperPluginDescription.Load.BEFORE, false)
}

bukkit {
  name = rootProject.name
  version = project.version as String
  main = "carbonchat.libs.io.papermc.papertrail.RequiresPaperPlugins"
  apiVersion = "1.19"
  authors = listOf("Draycia", "jmp")
  website = GITHUB_REPO_URL
}

carbonPermission.permissions.get().forEach {
  setOf(bukkit.permissions, paper.permissions).forEach { container ->
    container.register(it.string) {
      description = it.description
      childrenMap = it.children
    }
  }
}

publishMods.modrinth {
  modLoaders.addAll("paper", "folia")
}

configurations.runtimeDownload {
  exclude("org.checkerframework", "checker-qual")
}
