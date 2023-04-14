import io.papermc.hangarpublishplugin.model.Platforms

plugins {
  id("carbon.shadow-platform")
  id("net.minecrell.plugin-yml.bukkit")
  id("paper-plugin-yml")
  id("xyz.jpenilla.run-paper")
  id("carbon.permissions")
  id("io.papermc.hangar-publish-plugin")
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

val projectVer = project.version as String

hangarPublish.publications.register("plugin") {
  version.set(projectVer)
  owner.set("Vicarious")
  slug.set("Carbon")
  channel.set(if (projectVer.contains("+beta.")) "Beta" else "Release")
  changelog.set(releaseNotes)
  apiKey.set(providers.environmentVariable("HANGAR_UPLOAD_KEY"))
  platforms.register(Platforms.PAPER) {
    jar.set(carbonPlatform.jarTask.flatMap { it.archiveFile })
    platformVersions.add("1.19.4")
    dependencies {
      url("LuckPerms", "https://luckperms.net/")
      url("EssentialsDiscord", "https://essentialsx.net/") {
        required.set(false)
      }
      url("DiscordSRV", "https://www.spigotmc.org/resources/discordsrv.18494/") {
        required.set(false)
      }
      url("PlaceholderAPI", "https://www.spigotmc.org/resources/placeholderapi.6245/") {
        required.set(false)
      }
    }
  }
}
