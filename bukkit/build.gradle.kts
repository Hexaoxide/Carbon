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
}

carbonShadowPlatform {
  relocateGuice.set(true)
}

tasks {
  shadowJar {
    relocateDependency("io.papermc.lib")
    relocateDependency("io.leangen.geantyref")
  }
  runServer {
    minecraftVersion("1.18.1")
  }
}

// Generates plugin.yml automatically
bukkit {
  name = rootProject.name
  version = project.version as String
  main = "net.draycia.carbon.bukkit.CarbonChatBukkit"
  apiVersion = "1.16"
  author = "Draycia"
  depend = listOf("LuckPerms")
  softDepend = listOf("PlaceholderAPI")
  loadBefore = listOf("Essentials")
  website = GITHUB_REPO_URL
}
