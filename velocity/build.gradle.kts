import java.util.Locale

plugins {
  id("carbon.shadow-platform")
  id("net.kyori.blossom")
}

dependencies {
  implementation(projects.carbonchatCommon)
  compileOnly(libs.velocityApi)
  annotationProcessor(libs.velocityApi)
}

tasks {
  shadowJar {
    dependencies {
      // included in velocity
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
