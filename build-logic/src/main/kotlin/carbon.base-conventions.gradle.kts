plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.publishing")
}

indra {
  gpl3OnlyLicense()
  javaVersions {
    target(16)
  }
  github(GITHUB_ORGANIZATION, GITHUB_REPO)
}
