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
  exclude("net/draycia/carbon/common/command/argument/OptionValueParser.java")
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
