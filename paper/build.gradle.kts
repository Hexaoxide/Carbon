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
  compileOnly(libs.paperApi)
  implementation(libs.paperTrail)

  // Commands
  implementation(libs.cloudPaper)

  // Misc
  implementation(libs.bstatsBukkit)

  // Plugins
  compileOnly("me.clip:placeholderapi:2.10.9") // TODO: move this to libs.versions.yml
  compileOnly(libs.essentialsXDiscord)
  compileOnly(libs.discordsrv)
}

carbonShadowPlatform {
  relocateGuice.set(true)
}

tasks {
  shadowJar {
    relocateDependency("io.papermc.papertrail")
    relocateDependency("io.leangen.geantyref")
  }
  runServer {
    minecraftVersion("1.19.4")
  }
}

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
  website = GITHUB_REPO_URL
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
