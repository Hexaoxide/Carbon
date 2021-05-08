import java.util.Locale
import net.draycia.carbon.BSTATS_VER
import net.draycia.carbon.CLOUD_VER
import org.spongepowered.plugin.metadata.PluginDependency
import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
  id("com.github.johnrengelman.shadow")
  id("org.spongepowered.gradle.plugin") version "1.0.3"
}

dependencies {
  implementation(projects.carbonchatApi)
  implementation("cloud.commandframework:cloud-sponge:$CLOUD_VER")
  implementation("org.bstats:bstats-sponge:$BSTATS_VER")
}

tasks {
  runServer {
    classpath(shadowJar)
  }
  build {
    dependsOn(shadowJar)
  }
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

sponge {
  apiVersion("8.0.0")
  plugin(rootProject.name.toLowerCase(Locale.ROOT)) {
    loader(PluginLoaders.JAVA_PLAIN)
    displayName(rootProject.name)
    mainClass("net.draycia.carbon.sponge.CarbonChatSponge")
    description(project.description)
    links {
      val url = rootProject.ext["github"] as String
      homepage(url)
      source(url)
      issues("$url/issues")
    }
    contributor("Vicarious") {
      description("Lead Developer")
    }
    dependency("spongeapi") {
      loadOrder(PluginDependency.LoadOrder.AFTER)
      optional(false)
    }
  }
}
