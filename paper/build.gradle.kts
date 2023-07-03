plugins {
  id("carbon.shadow-platform")
  id("net.minecrell.plugin-yml.bukkit")
  id("paper-plugin-yml")
  id("xyz.jpenilla.run-paper")
  id("carbon.permissions")
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
  compileOnly(libs.essentialsXDiscord)
  compileOnly(libs.discordsrv)

  runtimeDownload(libs.guice) {
    exclude("com.google.guava")
  }
}

tasks {
  shadowJar {
    relocateDependency("io.papermc.papertrail")
    relocateDependency("io.leangen.geantyref")
  }
  runServer {
    minecraftVersion("1.20.1")
  }
  writeDependencies {
  }
}

runPaper.folia.registerTask()

paper {
  name = rootProject.name
  version = project.version as String
  loader = "net.draycia.carbon.paper.CarbonPaperLoader"
  main = "net.draycia.carbon.paper.CarbonPaperBootstrap"
  apiVersion = "1.19"
  author = "Draycia"
  loadAfter += PaperPluginDescription.LoadInfo("LuckPerms")
  loadAfter += PaperPluginDescription.LoadInfo("EssentialsDiscord")
  loadAfter += PaperPluginDescription.LoadInfo("DiscordSRV")
  loadAfter += PaperPluginDescription.LoadInfo("PlaceholderAPI")
  dependencies += PaperPluginDescription.Dependency("LuckPerms", true)
  dependencies += PaperPluginDescription.Dependency("PlaceholderAPI", false)
  dependencies += PaperPluginDescription.Dependency("EssentialsDiscord", false)
  dependencies += PaperPluginDescription.Dependency("DiscordSRV", false)
  dependencies += PaperPluginDescription.Dependency("MiniPlaceholders", false)
  website = GITHUB_REPO_URL
  foliaSupported = true
}

bukkit {
  name = rootProject.name
  version = project.version as String
  main = "net.draycia.carbon.libs.io.papermc.papertrail.RequiresPaperPlugins"
  apiVersion = "1.19"
  author = "Draycia"
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

modrinth {
  loaders.addAll("paper", "folia")
}
