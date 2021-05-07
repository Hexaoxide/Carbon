import org.checkerframework.gradle.plugin.CheckerFrameworkPlugin
import net.draycia.carbon.*

plugins {
  java
  `java-library`
  `maven-publish`
  checkstyle
  id("org.checkerframework") version "0.5.12"
}

allprojects {
  group = "net.draycia"
  description = "CarbonChat"
  version = project.property("projectVersion") // TODO: include git hash
}

allprojects {
  configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility
    disableAutoTargetJvm()
  }

  checkstyle {
    toolVersion = "8.36.2"
    val configRoot = rootProject.projectDir
    configDirectory.set(configRoot)
    configProperties["basedir"] = configRoot.absolutePath
  }
}

subprojects {
  apply {
    plugin<JavaPlugin>()
    plugin<JavaLibraryPlugin>()
    plugin<MavenPublishPlugin>()
    plugin<CheckstylePlugin>()
    plugin<CheckerFrameworkPlugin>()
  }

  repositories {
    mavenCentral()

    maven { url = uri("https://nexus.proximyst.com/repository/maven-public/") }
    maven { url = uri("https://repo.maven.apache.org/maven2") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://dl.bintray.com/ichbinjoe/public/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://repo.glaremasters.me/repository/public/") }
    maven { url = uri("https://repo.aikar.co/content/groups/aikar/") }
  }

  dependencies {
    checkstyle("ca.stellardrift:stylecheck:$STYLECHECK_VER")
  }

//  checkerFramework {
//    checkers = [
//      "org.checkerframework.checker.nullness.NullnessChecker"
//    ]
//  }
}
