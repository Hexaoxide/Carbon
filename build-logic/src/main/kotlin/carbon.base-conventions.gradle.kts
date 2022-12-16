plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.publishing")
  id("net.kyori.indra.license-header")
}

indra {
  gpl3OnlyLicense()

  publishReleasesTo("parksReleases", "https://repo.parks.dev/repository/maven-releases/")
  publishSnapshotsTo("parksSnapshots", "https://repo.parks.dev/repository/maven-snapshots/")

  javaVersions {
    target(17)
  }

  github(GITHUB_ORGANIZATION, GITHUB_REPO)
}

license {
  header.set(resources.text.fromFile(rootProject.file("LICENSE_HEADER")))
  exclude("net/draycia/carbon/common/command/argument/CarbonPlayerArgument.java")
  exclude("net/draycia/carbon/common/command/argument/OptionValueParser.java")
  exclude("net/draycia/carbon/common/messages/PrefixedDelegateIterator.java")
  exclude("net/draycia/carbon/common/messages/StandardPlaceholderResolverStrategyButDifferent.java")
}

tasks {
  withType<JavaCompile> {
    // disable 'warning: No processor claimed any of these annotations' spam
    options.compilerArgs.add("-Xlint:-processing")
    options.compilerArgs.add("-parameters")
  }
}

dependencies {
  checkstyle(libs.stylecheck)
}

repositories {
  mavenCentral()
  maven("https://repo.racci.dev/snapshots/") {
    mavenContent {
      snapshotsOnly()
      includeGroup("ninja.egg82")
    }
  }
  // temporary cloud snapshots repo for sponge-8
  maven("https://repo.jpenilla.xyz/snapshots/") {
    mavenContent {
      snapshotsOnly()
      includeModule("cloud.commandframework", "cloud-sponge")
      includeGroup("xyz.jpenilla") // reflection-remapper
    }
  }
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
    mavenContent { snapshotsOnly() }
  }
  maven("https://oss.sonatype.org/content/repositories/snapshots/") {
    mavenContent { snapshotsOnly() }
  }
  // PaperMC
  maven("https://repo.papermc.io/repository/maven-public/")
  // Sponge API
  maven("https://repo.spongepowered.org/repository/maven-public/")
  // Velocity Proxy for run config
  ivy("https://versions.velocitypowered.com/download/") {
    patternLayout { artifact("[revision].[ext]") }
    metadataSources { artifact() }
    content { includeModule("com.velocitypowered", "velocity-proxy") }
  }
  // PlaceholderAPI
  maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
    content { includeGroup("me.clip") }
  }
  maven("https://jitpack.io") {
    content { includeGroupByRegex("com\\.github\\..*") }
  }
  // EssentialsDiscord
  maven("https://repo.essentialsx.net/releases/") {
    mavenContent {
      releasesOnly()
      includeGroup("net.essentialsx")
    }
  }
  maven("https://repo.essentialsx.net/snapshots/") {
    mavenContent {
      snapshotsOnly()
      includeGroup("net.essentialsx")
    }
  }
  // DiscordSRV
  maven("https://m2.dv8tion.net/releases")
  maven("https://nexus.scarsz.me/content/groups/public/")
}
