plugins {
  id("carbon.shadow-platform")
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
