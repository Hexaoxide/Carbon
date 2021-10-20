import java.util.*

plugins {
  id("carbon.shadow-platform")
  id("net.kyori.blossom")
}

val velocityRun: Configuration by configurations.creating {
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, Usage.JAVA_RUNTIME))
    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class, Category.LIBRARY))
    attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class, LibraryElements.JAR))
  }
}

dependencies {
  implementation(projects.carbonchatCommon)

  compileOnly(libs.velocityApi)
  annotationProcessor(libs.velocityApi)

  implementation(libs.cloudVelocity)

  velocityRun("com.velocitypowered", "velocity-proxy", libs.versions.velocityApi.get())
}

tasks {
  shadowJar {
    dependencies {
      relocateDependency("io.leangen.geantyref")

      // included in velocity
      exclude(dependency("com.google.inject:guice"))
      exclude(dependency("aopalliance:aopalliance"))
      exclude(dependency("javax.inject:javax.inject"))
    }
  }
  register<JavaExec>("runProxy") {
    group = "carbon"
    standardInput = System.`in`
    classpath(velocityRun.asFileTree)
    workingDir = layout.projectDirectory.dir("run").asFile

    val pluginJar = shadowJar.flatMap { it.archiveFile }
    inputs.file(pluginJar)

    doFirst {
      if (!workingDir.exists()) {
        workingDir.mkdirs()
      }
      val plugins = workingDir.resolve("plugins")
      if (!plugins.exists()) {
        plugins.mkdirs()
      }

      pluginJar.get().asFile.copyTo(plugins.resolve("carbon.jar"), overwrite = true)
    }
  }
}

blossom {
  mapOf(
    "ID" to rootProject.name.toLowerCase(Locale.ROOT),
    "NAME" to rootProject.name,
    "VERSION" to version,
    "DESCRIPTION" to description,
    "URL" to GITHUB_REPO_URL
  ).forEach { (k, v) ->
    replaceToken("$[$k]", v)
  }
}
