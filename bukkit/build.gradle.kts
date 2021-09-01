plugins {
  id("carbon.shadow-platform")
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
    minecraftVersion("1.17.1")
  }
}

// Generates plugin.yml automatically
bukkit {
  name = rootProject.name
  version = project.version as String
  main = "net.draycia.carbon.bukkit.CarbonChatBukkit"
  apiVersion = "1.16"
  author = "Draycia"
  softDepend = listOf("PlaceholderAPI", "Vault")
  loadBefore = listOf("Essentials")
  website = GITHUB_REPO_URL
}
