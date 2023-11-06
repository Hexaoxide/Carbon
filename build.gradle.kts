import io.papermc.hangarpublishplugin.model.Platforms

plugins {
  id("carbon.build-logic")
  alias(libs.plugins.hangar.publish)
  alias(libs.plugins.indra.publishing.sonatype)
}

val projectVersion: String by project // get from gradle.properties
version = projectVersion

hangarPublish.publications.register("plugin") {
  version.set(projectVersion)
  id.set("Carbon")
  channel.set(if (projectVersion.contains("-beta.")) "Beta" else "Release")
  changelog.set(releaseNotes)
  apiKey.set(providers.environmentVariable("HANGAR_UPLOAD_KEY"))
  platforms.register(Platforms.PAPER) {
    jar.set(project(":carbonchat-paper").the<CarbonPlatformExtension>().jarTask.flatMap { it.archiveFile })
    platformVersions.add("1.19.4-1.20.2")
    dependencies {
      url("LuckPerms", "https://luckperms.net/")
      hangar("Essentials") {
        required.set(false)
      }
      url("DiscordSRV", "https://www.spigotmc.org/resources/discordsrv.18494/") {
        required.set(false)
      }
      url("PlaceholderAPI", "https://www.spigotmc.org/resources/placeholderapi.6245/") {
        required.set(false)
      }
      hangar("MiniPlaceholders") {
        required.set(false)
      }
    }
  }
  platforms.register(Platforms.VELOCITY) {
    jar.set(project(":carbonchat-velocity").the<CarbonPlatformExtension>().jarTask.flatMap { it.archiveFile })
    platformVersions.add("3.2")
    dependencies {
      url("LuckPerms", "https://luckperms.net/")
      hangar("MiniPlaceholders") {
        required.set(false)
      }
      hangar("UnSignedVelocity") {
        required.set(false)
      }
    }
  }
}

indraSonatype {
  useAlternateSonatypeOSSHost("s01")
}
