import com.google.gson.JsonParser
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
import java.io.InputStreamReader
import java.net.URL
import java.time.Duration

plugins {
  id("com.github.johnrengelman.shadow")
  id("net.minecrell.plugin-yml.bukkit")
}

dependencies {
  implementation(projects.carbonchatCommon)

  // Server
  compileOnly(libs.paperApi)
  implementation(libs.paperLib)

  // Commands
  implementation(libs.cloudPaper)

  // Misc
  implementation(libs.bstatsBukkit)

  // Plugins
  compileOnly("me.clip:placeholderapi:2.10.9")
  compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

tasks {
  build {
    dependsOn(shadowJar)
  }
  shadowJar {
    archiveFileName.set(project.name + "-" + project.version + ".jar")
    configureShadowJar()
    relocateDependency("io.papermc.lib")
    relocateDependency("io.leangen.geantyref")
  }
  register<JavaExec>("runServer") {
    workingDir = projectDir.resolve("run")
    standardInput = System.`in`
    dependsOn(shadowJar)
    val paperJar = workingDir.resolve("paperclip.jar")
    classpath(paperJar)
    args = listOf("nogui")
    systemProperty("disable.watchdog", true)

    doFirst {
      // Create working dir and plugins dir if needed
      if (!workingDir.exists()) workingDir.mkdir()
      val pluginsDir = workingDir.resolve("plugins")
      if (!pluginsDir.exists()) pluginsDir.mkdir()

      // Copy carbon jar
      val carbonJar = pluginsDir.resolve("carbon.jar")
      shadowJar.get().archiveFile.get().asFile.copyTo(carbonJar, overwrite = true)

      // Download latest Paperclip if we don't have one or if it's older than 3 days
      val paperVersion = "1.16.5"
      if (!paperJar.exists() || paperJar.lastModified() < System.currentTimeMillis() - Duration.ofDays(3).toMillis()) {
        val jsonParser = JsonParser()
        logger.lifecycle("Fetching latest Paper {} builds...", paperVersion)
        val builds = jsonParser.parse(InputStreamReader(URL("https://papermc.io/api/v2/projects/paper/versions/$paperVersion").openStream())).asJsonObject["builds"].asJsonArray
        val latest = builds.last().asString
        logger.lifecycle("Downloading Paper {} build {}...", paperVersion, latest)
        val jarUrl = URL("https://papermc.io/api/v2/projects/paper/versions/$paperVersion/builds/$latest/downloads/paper-$paperVersion-$latest.jar")
        paperJar.writeBytes(jarUrl.readBytes())
        logger.lifecycle("Done downloading Paper {} build {}.", paperVersion, latest)
      }
    }
  }
}

// Generates plugin.yml automatically
bukkit {
  name = rootProject.name
  version = project.version as String
  main = "net.draycia.carbon.bukkit.CarbonChatBukkitEntry"
  apiVersion = "1.16"
  author = "Draycia"
  depend = listOf("PlaceholderAPI", "Vault")
  loadBefore = listOf("Essentials")
  website = GITHUB_REPO_URL
  permissions {
    create("carbonchat.channels.global.see") {
      default = TRUE
    }
    create("carbonchat.channels.global.use") {
      default = TRUE
    }
  }
}
