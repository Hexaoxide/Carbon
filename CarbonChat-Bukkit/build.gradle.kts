import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default

plugins {
  id("com.github.johnrengelman.shadow") version "6.1.0"
  id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

description = "CarbonChat-Bukkit"

dependencies {
  implementation(project(":CarbonChat-Common"))

  // Server
  compileOnly("com.destroystokyo.paper", "paper-api", Versions.PAPER)

  // Config
  implementation("org.spongepowered", "configurate-yaml", Versions.CONFIGURATE)
  implementation("org.spongepowered", "configurate-hocon", Versions.CONFIGURATE)
  implementation("org.yaml", "snakeyaml", Versions.SNAKEYAML)

  // Commands
  implementation("cloud.commandframework", "cloud-paper", Versions.CLOUD)
  
  // SLF4J Binding
  implementation("org.slf4j", "slf4j-jdk14", Versions.SLF4J)

  // Implementation
  implementation("org.bstats", "bstats-bukkit", Versions.BSTATS)

  // Plugins
  compileOnly("com.gmail.nossr50.mcMMO", "mcMMO", Versions.MCMMO) {
    exclude(group = "org.jetbrains", module = "annotations")
  }
  compileOnly("com.github.TownyAdvanced", "Towny", Versions.TOWNY)
  compileOnly("com.sk89q.worldguard", "worldguard-bukkit", Versions.WORLDGUARD) {
    exclude(group = "org.spigotmc", module = "spigot-api") // Honestly?
  }
  compileOnly("me.clip", "placeholderapi", Versions.PAPI)
  compileOnly("net.luckperms", "api", Versions.LUCKPERMS)
  compileOnly("com.github.MilkBowl", "VaultAPI", Versions.VAULT)
}

// Generates plugin.yml automatically
bukkit {
  name = "CarbonChat"
  version = rootProject.version as String
  main = "net.draycia.carbon.bukkit.CarbonChatBukkit"
  apiVersion = "1.16"
  author = "Draycia"
  depend = listOf("PlaceholderAPI", "Vault")
  softDepend = listOf("LuckPerms", "Towny", "mcMMO", "WorldGuard")
  loadBefore = listOf("Essentials")
  permissions {
    register("carbonchat.channels.global.see") {
      default = Default.TRUE
    }

    register("carbonchat.channels.global.use") {
      default = Default.TRUE
    }
  }
}

tasks {
  // Automatically shadowJar when building
  build {
    dependsOn(withType<ShadowJar>())
  }

  // Output a single jar
  withType<ShadowJar> {
    minimize()
    archiveFileName.set(project.description + "-" + Versions.CARBON_BASE + ".jar")
    destinationDirectory.set(rootProject.getBuildDir())
    relocate("org.yaml", "net.draycia.carbon.libs.org.yaml")
    relocate("net.kyori", "net.draycia.carbon.libs.kyori")
    relocate("org.spongepowered.configurate", "net.draycia.carbon.libs.configurate")
    relocate("org.checkerframework", "net.draycia.carbon.libs.checkerframework")
    relocate("org.reactivestreams", "net.draycia.carbon.libs.reactivestreams")
    relocate("org.codehaus", "net.draycia.carbon.libs.codehaus")
    relocate("org.slf4j", "net.draycia.carbon.libs.slf4j")
    relocate("io.leangen.geantyref", "net.draycia.carbon.libs.typereference")
    relocate("io.lettuce", "net.draycia.carbon.libs.lettuce")
    relocate("co.aikar.idb", "net.draycia.carbon.libs.idb")
    relocate("cloud.commandframework", "net.draycia.carbon.libs.cloud")
    relocate("com.zaxxer.hikari", "net.draycia.carbon.libs.hikari")
    relocate("com.typesafe.config", "net.draycia.carbon.libs.typesafe.config")
    relocate("com.google.common", "net.draycia.carbon.libs.google.common")
    relocate("de.themoep.minedown", "net.draycia.carbon.libs.minedown")
    relocate("reactor", "net.draycia.carbon.libs.reactor")
    relocate("org.bstats", "net.draycia.carbon.libs.bstats")
    relocate("javax.annotation", "net.draycia.carbon.libs.javax.annotation")
  }

  jar {
    archiveFileName.set(project.description + "-" + Versions.CARBON_BASE + ".jar")
  }

  // Cleanup custom output directory on clean task
  clean{
    doFirst {
      delete("$rootDir/build/bundled")
    }
  }
}

//// Automatically relocate all shaded dependencies
//import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
//
//task relocateShadowJar(type: ConfigureShadowRelocation) {
//  target = tasks.shadowJar
//  prefix = "net.draycia.carbon.libs" // Default value is "shadow"
//}
//
//tasks.shadowJar.dependsOn tasks.relocateShadowJar


