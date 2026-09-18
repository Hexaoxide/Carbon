plugins {
  id("carbon.base-conventions")
  id("net.kyori.indra.publishing")
  id("org.incendo.cloud-build-logic.publishing")
}

signing {
  val signingKey = project.findProperty("signingKey") as String?
  val signingPassword = project.findProperty("signingPassword") as String?
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
        developer {
          id.set("jmp")
          name.set("Jason Penilla")
        }
      }
    }
  }
}

javadocLinks {
  defaultJavadocProvider = "https://www.javadocs.dev/{group}/{name}/{version}"
}
