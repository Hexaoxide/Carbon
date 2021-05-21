import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE

plugins {
  id("carbon-shadow-subproject")
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
  shadowJar {
    relocateDependency("io.papermc.lib")
    relocateDependency("io.leangen.geantyref")

    // Guice
    relocateDependency("com.google.inject")
    relocateDependency("org.aopalliance")
    relocateDependency("javax.inject")
  }
  runServer {
    minecraftVersion("1.16.5")
    jvmArgs("-DLog4jContextSelector=org.apache.logging.log4j.core.selector.ClassLoaderContextSelector") // https://github.com/PaperMC/Paper/issues/4155
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
