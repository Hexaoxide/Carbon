import net.draycia.carbon.STYLECHECK_VER
import org.checkerframework.gradle.plugin.CheckerFrameworkPlugin
import org.checkerframework.gradle.plugin.CheckerFrameworkExtension
import net.kyori.indra.IndraExtension
import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin

plugins {
  val indraVersion = "2.0.2"
  id("net.kyori.indra") version indraVersion apply false
  id("net.kyori.indra.checkstyle") version indraVersion apply false
  id("net.kyori.indra.publishing") version indraVersion apply false
  id("com.github.johnrengelman.shadow") version "7.0.0" apply false
  id("org.checkerframework") version "0.5.20" apply false
}

ext["github"] = "https://github.com/Hexaoxide/Carbon"

val projectVersion: String by project

group = "net.draycia"
description = "CarbonChat - A modern chat plugin"
version = projectVersion // todo: include git hash

subprojects {
  apply<IndraPlugin>()
  apply<IndraCheckstylePlugin>()
  apply<IndraPublishingPlugin>()
  apply<CheckerFrameworkPlugin>()

  repositories {
    mavenCentral()
    maven("https://nexus.proximyst.com/repository/maven-public/")
    maven("https://repo.maven.apache.org/maven2")
    maven("https://oss.sonatype.org/content/groups/public/")
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
    "checkstyle"("ca.stellardrift:stylecheck:$STYLECHECK_VER")
  }

  configure<JavaPluginConvention> {
    disableAutoTargetJvm()
  }

  configure<CheckerFrameworkExtension> {
    checkers = listOf(
      "org.checkerframework.checker.nullness.NullnessChecker"
    )
  }

  configure<IndraExtension> {
    gpl3OnlyLicense()

    javaVersions {
      target(11)
    }
  }

}

tasks {
  // Root project has no useful artifacts
  withType<Jar> {
    onlyIf { false }
  }
}
