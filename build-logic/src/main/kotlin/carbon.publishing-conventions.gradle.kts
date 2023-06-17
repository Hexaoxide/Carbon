plugins {
  id("carbon.base-conventions")
  id("net.kyori.indra.publishing")
}


signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
}

indra {
  configurePublications {
    pom {
      developers {
        developer {
          id.set("Vicarious")
          name.set("Josua Parks")
        }
      }
    }
  }
}
