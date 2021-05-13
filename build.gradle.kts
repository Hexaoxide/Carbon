import net.kyori.indra.repository.sonatypeSnapshots

plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.checkstyle")
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
    maven("https://oss.sonatype.org/content/groups/public/")
    sonatypeSnapshots()
    maven("https://nexus.proximyst.com/repository/maven-public/")
    //maven("https://repo.incendo.org/content/repositories/snapshots") // normal cloud snapshot repo
    maven("https://repo.jpenilla.xyz/snapshots/") { // temp cloud snapshot repo for sponge-8
      content {
        includeGroup("cloud.commandframework")
      }
    }
    maven("https://maven.enginehub.org/repo/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/ichbinjoe/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
  }

  dependencies {
    checkstyle(rootProject.libs.stylecheck)
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
