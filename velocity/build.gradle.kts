plugins {
  id("carbon-shadow-subproject")

}

repositories {
  maven("https://nexus.velocitypowered.com/repository/maven-public/") {
    name = "velocity"
  }
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
