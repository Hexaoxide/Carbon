import net.kyori.indra.repository.sonatypeSnapshots

plugins {
  id("carbon-build-logic")
  id("net.kyori.indra")
  id("net.kyori.indra.git")
}

group = "net.draycia"
description = "CarbonChat - A modern chat plugin"
val projectVersion: String by project // get from gradle.properties
version = projectVersion

subprojects {
  plugins.apply("net.kyori.indra")
  plugins.apply("net.kyori.indra.checkstyle")
  plugins.apply("net.kyori.indra.publishing")

  if (projectVersion.endsWith("-SNAPSHOT")) {
    // Add git commit hash to version for platforms, but not for API
    if (this != rootProject.projects.carbonchatApi.dependencyProject) {
      version = "$projectVersion+${indraGit.commit()?.name?.substring(0, 7) ?: error("Failed to retrieve git commit hash")}"
    }
  }

  repositories {
    mavenCentral()
    sonatypeSnapshots()
    // Paper API
    maven("https://papermc.io/repo/repository/maven-public/")
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

  dependencies {
    "checkstyle"(rootProject.libs.stylecheck)
  }

  configure<JavaPluginConvention> {
    disableAutoTargetJvm()
  }

  indra {
    gpl3OnlyLicense()

    javaVersions {
      target(16)
    }

    github("Hexaoxide", "Carbon")
  }
}

tasks {
  // Root project has no useful artifacts
  withType<Jar> {
    onlyIf { false }
  }
}
