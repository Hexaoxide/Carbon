import net.draycia.carbon.*
import java.nio.charset.StandardCharsets

plugins {
  id("com.github.johnrengelman.shadow")
  id("net.minecrell.plugin-yml.bukkit") version "0.4.0"
}

dependencies {
  implementation(projects.carbonchatApi)

  // Server
  compileOnly("com.destroystokyo.paper:paper-api:$PAPER_API_VER")
  implementation("io.papermc", "paperlib", "1.0.6")

  // Adventure
  implementation("net.kyori:adventure-platform-bukkit:$ADVENTURE_PLATFORM_VER")
  implementation("net.kyori:adventure-text-serializer-bungeecord:$ADVENTURE_PLATFORM_VER")

  // Config
  implementation("org.spongepowered:configurate-yaml:$CONFIGURATE_VER")
  implementation("org.yaml:snakeyaml:$SNAKEYAML_VER")

  // Commands
  implementation("cloud.commandframework:cloud-paper:$CLOUD_VER")

  // Misc
  implementation("org.bstats:bstats-bukkit:$BSTATS_VER")

  // Plugins
  compileOnly("me.clip:placeholderapi:$PLACEHOLDER_API_VER")
  compileOnly("net.luckperms:api:$LUCKPERMS_API_VER")
  compileOnly("com.github.MilkBowl:VaultAPI:$VAULT_VER")
}

// Generates plugin.yml automatically
bukkit {
  name = rootProject.name
  version = rootProject.version as String
  main = "net.draycia.carbon.bukkit.CarbonChatBukkit"
  apiVersion = "1.13"
  author = "Draycia"
  depend = listOf("PlaceholderAPI", "Vault", "LuckPerms")
  loadBefore = listOf("Essentials")
  website = rootProject.ext["github"] as String
  permissions {
    create("carbonchat.channels.global.see") {
      //default = "TRUE"
    }

    create("carbonchat.channels.global.use") {
      //default = "TRUE"
    }
  }
}

/*
checkstyle {
  def configRoot = new File(rootProject.projectDir, '.checkstyle')
  toolVersion = vers['checkstyle']
  configDirectory = configRoot
  configProperties = [basedir: configRoot.getAbsolutePath()]
}
 */

// Set output directory and encoding
tasks {
  withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
    options.compilerArgs.addAll(listOf("-Xlint:all"))
  }
}

// Automatically shadowJar when building
tasks {
  build {
    dependsOn(shadowJar)
  }
}

// Output a single jar
tasks {
  shadowJar {
    minimize()
    archiveFileName.set(project.name + "-" + project.version + ".jar")
    doLast {
      val archive = archiveFile.get().asFile
      val libs = rootProject.buildDir.resolve("libs")
      libs.mkdirs()
      archive.copyTo(libs.resolve(archive.name), overwrite = true)
    }
    relocate("org.yaml", "net.draycia.carbon.libs.org.yaml")
    relocate("org.spongepowered.configurate", "net.draycia.carbon.libs.configurate")
    relocate("org.checkerframework", "net.draycia.carbon.libs.checkerframework")
    relocate("org.reactivestreams", "net.draycia.carbon.libs.reactivestreams")
    relocate("org.codehaus", "net.draycia.carbon.libs.codehaus")
    relocate("io.leangen.geantyref", "net.draycia.carbon.libs.typereference")
    relocate("io.lettuce", "net.draycia.carbon.libs.lettuce")
    relocate("co.aikar.idb", "net.draycia.carbon.libs.idb")
    relocate("cloud.commandframework", "net.draycia.carbon.libs.cloud")
    relocate("com.zaxxer.hikari", "net.draycia.carbon.libs.hikari")
    relocate("com.typesafe.config", "net.draycia.carbon.libs.typesafe.config")
    relocate("com.google.common", "net.draycia.carbon.libs.google.common")
    relocate("reactor", "net.draycia.carbon.libs.reactor")
    relocate("javax.annotation", "net.draycia.carbon.libs.javax.annotation")
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

// Cleanup custom output directory on clean task
tasks {
  clean {
    doFirst {
      delete("$rootDir/build/bundled")
    }
  }
}
