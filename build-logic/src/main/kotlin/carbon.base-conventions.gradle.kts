plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.licenser.spotless")
}

repositories {
  mavenCentral {
    mavenContent { releasesOnly() }
  }
  maven("https://repo.jpenilla.xyz/snapshots/") {
    mavenContent {
      snapshotsOnly()
      includeModuleByRegex("de\\.hexaoxi", "messenger-.*")
      includeModule("org.incendo", "cloud-sponge")
      includeModule("com.seiama", "registry")
    }
  }
  maven("https://central.sonatype.com/repository/maven-snapshots/") {
    mavenContent { snapshotsOnly() }
  }
  // PaperMC
  maven("https://repo.papermc.io/repository/maven-public/")
  // Sponge API
  maven("https://repo.spongepowered.org/repository/maven-public/")
  // PlaceholderAPI
  maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
    content { includeGroup("me.clip") }
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
  maven("https://nexus.scarsz.me/content/groups/public/") {
    mavenContent {
      includeGroup("com.discordsrv")
    }
  }
  // Glare's repo for Towny
  maven("https://repo.glaremasters.me/repository/towny/") {
    content { includeGroup("com.palmergames.bukkit.towny") }
  }
  // FactionsUUID
  maven("https://ci.ender.zone/plugin/repository/everything/") {
    content { includeGroup("com.massivecraft") }
  }
  // mcMMO
  maven("https://nexus.neetgames.com/repository/maven-releases/") {
    content {
      includeGroup("com.gmail.nossr50.mcMMO")
    }
  }
  // Parties
  maven("https://repo.alessiodp.com/releases/") {
    content {
      includeGroup("com.alessiodp.parties")
    }
  }
}

version = rootProject.version

indra {
  gpl3OnlyLicense()

  javaVersions {
    target(21)
  }

  github(GITHUB_ORGANIZATION, GITHUB_REPO)
}

spotless {
  java {
    targetExclude(
      "src/main/java/net/draycia/carbon/common/messages/PrefixedDelegateIterator.java",
      "src/main/java/net/draycia/carbon/common/messages/StandardPlaceholderResolverStrategyButDifferent.java",
      "src/main/java/com/google/inject/assistedinject/**"
    )
  }
}

indraSpotlessLicenser {
  licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
}

tasks {
  withType<JavaCompile> {
    // disable unclaimed annotation and missing annotation warnings
    options.compilerArgs.add("-Xlint:-processing,-classfile")
    options.compilerArgs.add("-parameters")
  }
}

dependencies {
  checkstyle(libs.stylecheck)
}
