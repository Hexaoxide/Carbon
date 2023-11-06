import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("carbon.shadow-platform")
  id("net.minecrell.plugin-yml.bukkit")
  id("paper-plugin-yml")
  alias(libs.plugins.runPaper)
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
    relocateCloud()
  }
  runServer {
    version.set(libs.versions.minecraft)
    downloadPlugins {
      url("https://download.luckperms.net/1515/bukkit/loader/LuckPerms-Bukkit-5.4.102.jar")
    }
  }
  register<RunServer>("runServer2") {
    version.set(libs.versions.minecraft)
    pluginJars.from(shadowJar.flatMap { it.archiveFile })
    runDirectory.set(layout.projectDirectory.dir("run2"))
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
  website = GITHUB_REPO_URL
  foliaSupported = true

  loadAfter("LuckPerms")
  loadAfter("EssentialsDiscord")
  loadAfter("DiscordSRV")
  loadAfter("PlaceholderAPI")

  dependency("LuckPerms", true)
  dependency("PlaceholderAPI", false)
  dependency("EssentialsDiscord", false)
  dependency("DiscordSRV", false)
  dependency("MiniPlaceholders", false)
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

configurations.runtimeDownload {
  exclude("org.checkerframework", "checker-qual")
}
