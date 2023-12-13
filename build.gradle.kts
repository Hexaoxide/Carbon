plugins {
  id("carbon.build-logic")
  alias(libs.plugins.hangar.publish)
  alias(libs.plugins.indra.publishing.sonatype)
}

val projectVersion: String by project // get from gradle.properties
version = projectVersion

fun Project.platformJar(): Provider<RegularFile> =
  extensions.getByType<CarbonPlatformExtension>().productionJar

hangarPublish.publications.register("plugin") {
  version = projectVersion
  id = "Carbon"
  channel = if (projectVersion.contains("-beta.")) "Beta" else "Release"
  changelog = releaseNotes
  apiKey = providers.environmentVariable("HANGAR_UPLOAD_KEY")
  platforms.paper {
    jar = project(":carbonchat-paper").platformJar()
    platformVersions.add("1.19.4-1.20.2")
    dependencies {
      url("LuckPerms", "https://luckperms.net/")
      hangar("Essentials") {
        required = false
      }
      url("DiscordSRV", "https://www.spigotmc.org/resources/discordsrv.18494/") {
        required = false
      }
      url("PlaceholderAPI", "https://www.spigotmc.org/resources/placeholderapi.6245/") {
        required = false
      }
      hangar("MiniPlaceholders") {
        required = false
      }
    }
  }
  platforms.velocity {
    jar = project(":carbonchat-velocity").platformJar()
    platformVersions.add("3.2")
    dependencies {
      url("LuckPerms", "https://luckperms.net/")
      hangar("MiniPlaceholders") {
        required = false
      }
      hangar("SignedVelocity") {
        required = false
      }
    }
  }
}

indraSonatype {
  useAlternateSonatypeOSSHost("s01")
}
