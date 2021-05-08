import net.draycia.carbon.*

//import org.checkerframework.gradle.plugin.CheckerFrameworkPlugin

plugins {
  `java-library`
  `maven-publish`
  id("com.github.johnrengelman.shadow") version "7.0.0" apply false
  //checkstyle
  //id("org.checkerframework") version "0.5.12"
}

ext["github"] = "https://github.com/Hexaoxide/Carbon"

val projectVersion: String by project

allprojects {
  group = "net.draycia"
  description = "CarbonChat - A modern chat plugin"
  version = projectVersion // todo: include git hash
}

allprojects {
  //configure<JavaPluginConvention> {
  //  sourceCompatibility = JavaVersion.VERSION_11
  //  targetCompatibility = sourceCompatibility
  //  disableAutoTargetJvm()
  //}

  //checkstyle {
  //  toolVersion = "8.36.2"
  //  val configRoot = rootProject.projectDir
  //  configDirectory.set(configRoot)
  //  configProperties["basedir"] = configRoot.absolutePath
  //}
}

subprojects {
  apply<JavaLibraryPlugin>()
  apply<MavenPublishPlugin>()
  //apply<CheckstylePlugin>()
  //apply<CheckerFrameworkPlugin>()

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
    //checkstyle("ca.stellardrift:stylecheck:$STYLECHECK_VER")
  }

//  checkerFramework {
//    checkers = [
//      "org.checkerframework.checker.nullness.NullnessChecker"
//    ]
//  }
}

tasks {
  // Root project has no useful artifacts
  withType<Jar> {
    onlyIf { false }
  }
}
