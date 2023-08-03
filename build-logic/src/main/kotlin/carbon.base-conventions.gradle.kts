plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.license-header")
}

version = rootProject.version

indra {
  gpl3OnlyLicense()

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
  exclude("com/google/inject/assistedinject")
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
