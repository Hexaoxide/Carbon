import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.sonatypeSnapshots
import org.checkerframework.gradle.plugin.CheckerFrameworkPlugin
import org.checkerframework.gradle.plugin.CheckerFrameworkExtension

plugins {
  `maven-publish`
  id("net.kyori.indra") version Versions.INDRA
  id("net.kyori.indra.checkstyle") version Versions.INDRA
  id("org.ajoberstar.grgit") version Versions.GRGIT
  id("org.checkerframework") version Versions.CHECKER_PLUGIN apply false
}

// Gets the git commit hash of the latest commit, used for version string
val gitHash = grgit.head().abbreviatedId

group = "net.draycia"
description = "CarbonChat"
version = "${Versions.CARBON_BASE}-$gitHash"

subprojects {
  apply<MavenPublishPlugin>()
  apply<IndraPlugin>()
  apply<IndraCheckstylePlugin>()
  apply<CheckerFrameworkPlugin>()

  repositories {
    mavenCentral()
    sonatypeSnapshots()

    maven("https://repo.maven.apache.org/maven2")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/ichbinjoe/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.minebench.de/")
  }

  dependencies {
    checkstyle("ca.stellardrift", "stylecheck", Versions.STYLECHECK)
  }

  extensions.configure<CheckerFrameworkExtension> {
    checkers = listOf(
      "org.checkerframework.checker.nullness.NullnessChecker"
    )
  }
}
