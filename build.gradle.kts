import io.papermc.hangarpublishplugin.model.Platforms

plugins {
  id("carbon.build-logic")
  id("io.papermc.hangar-publish-plugin")
}

group = "net.draycia"
description = "CarbonChat - A modern chat plugin"
val projectVersion: String by project // get from gradle.properties
version = projectVersion

hangarPublish.publications.register("plugin") {
  version.set(projectVersion)
  owner.set("Vicarious")
  slug.set("Carbon")
  channel.set(if (projectVersion.contains("+beta.")) "Beta" else "Release")
  changelog.set(releaseNotes)
  apiKey.set(providers.environmentVariable("HANGAR_UPLOAD_KEY"))
  platforms.register(Platforms.PAPER) {
    jar.set(project(":carbonchat-paper").the<CarbonPlatformExtension>().jarTask.flatMap { it.archiveFile })
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
