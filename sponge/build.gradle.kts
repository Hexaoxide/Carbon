import java.util.Locale
import org.spongepowered.plugin.metadata.PluginDependency
import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
  id("com.github.johnrengelman.shadow")
  id("org.spongepowered.gradle.plugin")
}

dependencies {
  implementation(projects.carbonchatApi)
  implementation(libs.adventureTextSerializerLegacy)
  implementation(libs.cloudSponge)
  //implementation(libs.bstatsSponge) // not updated for api 8 yet
}

tasks {
  runServer {
    classpath(shadowJar)
  }
  build {
    dependsOn(shadowJar)
  }
  shadowJar {
    archiveFileName.set(project.name + "-" + project.version + ".jar")
    configureShadow()
    dependencies {
      // included in sponge
      exclude(dependency("io.leangen.geantyref:geantyref"))
    }
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
