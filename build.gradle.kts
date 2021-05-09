import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin
import net.kyori.indra.repository.sonatypeSnapshots
import org.checkerframework.gradle.plugin.CheckerFrameworkExtension
import org.checkerframework.gradle.plugin.CheckerFrameworkPlugin

plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.publishing") apply false
  id("com.github.johnrengelman.shadow") apply false
  id("org.checkerframework") apply false
}

group = "net.draycia"
description = "CarbonChat - A modern chat plugin"
val projectVersion: String by project // get from gradle.properties
version = projectVersion

subprojects {
  apply<IndraPlugin>()
  apply<IndraCheckstylePlugin>()
  apply<IndraPublishingPlugin>()
  apply<CheckerFrameworkPlugin>()

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

  configure<CheckerFrameworkExtension> {
    checkers = listOf(
      "org.checkerframework.checker.nullness.NullnessChecker"
    )
  }

  indra {
    gpl3OnlyLicense()

    javaVersions {
      target(11)
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
