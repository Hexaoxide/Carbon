plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.licenser.spotless")
}

version = rootProject.version

indra {
  gpl3OnlyLicense()

  javaVersions {
    target(17)
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
