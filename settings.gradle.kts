enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    // Paper API
    maven("https://papermc.io/repo/repository/maven-public/")
    // Sponge API
    maven("https://repo.spongepowered.org/repository/maven-public/")
    // Velocity API
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    // Velocity Proxy for run config
    //ivy("https://versions.velocitypowered.com/download/") {
    ivy("https://ci.velocitypowered.com/job/velocity-3.0.0/lastSuccessfulBuild/artifact/proxy/build/libs/") {
      //patternLayout { artifact("[revision].[ext]") }
      patternLayout { artifact("velocity-proxy-3.0.0-SNAPSHOT-all.jar") }
      metadataSources { artifact() }
      content { includeModule("com.velocitypowered", "velocity-proxy") }
    }
    // Moonshine
    maven("https://nexus.proximyst.com/repository/maven-public/") {
      content { includeGroup("com.proximyst.moonshine") }
    }
    // cloud snapshots repo
    //maven("https://repo.incendo.org/content/repositories/snapshots") {
    //  content { includeGroup("cloud.commandframework") }
    //}
    // temporary cloud snapshots repo for sponge-8
    maven("https://repo.jpenilla.xyz/snapshots/") {
      content { includeGroup("cloud.commandframework") }
    }
    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
      content { includeGroup("me.clip") }
    }
    maven("https://jitpack.io") {
      content { includeGroupByRegex("com\\.github\\..*") }
    }
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://repo.jpenilla.xyz/snapshots/") // run-paper + shadow snapshot
  }
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "5.0.0"
}

rootProject.name = "CarbonChat"

includeBuild("build-logic")

setupSubproject("carbonchat-api") {
  projectDir = file("api")
}
setupSubproject("carbonchat-common") {
  projectDir = file("common")
}
setupSubproject("carbonchat-bukkit") {
  projectDir = file("bukkit")
}
setupSubproject("carbonchat-sponge") {
  projectDir = file("sponge")
}
setupSubproject("carbonchat-velocity") {
  projectDir = file("velocity")
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
  include(name)
  project(":$name").apply(block)
}
