import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.util.*

plugins {
  id("carbon.shadow-platform")
  id("org.spongepowered.gradle.plugin")
}

dependencies {
  implementation(projects.carbonchatCommon)
  implementation(libs.cloudSponge)
  //implementation(libs.bstatsSponge) // not updated for api 8 yet
}

tasks {
  shadowJar {
    dependencies {
      // included in sponge
      exclude(dependency("io.leangen.geantyref:geantyref"))
      exclude(dependency("com.google.inject:guice"))
      exclude(dependency("aopalliance:aopalliance"))
      exclude(dependency("javax.inject:javax.inject"))
    }
  }
}

sponge {
  injectRepositories(false) // We specify repositories in settings.gradle.kts
  apiVersion("8.1.0-SNAPSHOT")
  plugin(rootProject.name.toLowerCase(Locale.ROOT)) {
    loader {
      name(PluginLoaders.JAVA_PLAIN)
      version("1.0")
    }
    displayName(rootProject.name)
    entrypoint("net.draycia.carbon.sponge.CarbonChatSponge")
    description(project.description)
    license("GPLv3")
    links {
      homepage(GITHUB_REPO_URL)
      source(GITHUB_REPO_URL)
      issues("$GITHUB_REPO_URL/issues")
    }
    contributor("Vicarious") {
      description("Lead Developer")
    }
    contributor("Glare") {
      description("Moral Support")
    }
    dependency("spongeapi") {
      loadOrder(PluginDependency.LoadOrder.AFTER)
      optional(false)
    }
    dependency("luckperms") {
      version(">=5.0.0")
      optional(false)
    }
  }
}
