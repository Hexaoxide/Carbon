import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE

plugins {
  id("com.github.johnrengelman.shadow")
  id("net.minecrell.plugin-yml.bukkit")
  id("xyz.jpenilla.run-paper")
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
  runServer {
    minecraftVersion("1.16.5")
    pluginJarName("carbon.jar")
  }
}

// Generates plugin.yml automatically
bukkit {
  name = rootProject.name
  version = project.version as String
  main = "net.draycia.carbon.bukkit.CarbonChatBukkitEntry"
  apiVersion = "1.16"
  author = "Draycia"
  //depend = listOf("PlaceholderAPI", "Vault") // We don't need these yet
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
