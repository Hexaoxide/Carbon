plugins {
  id("carbon.base-conventions")
}

dependencies {
  api(projects.carbonchatApi)

  // Configs
  api(libs.configurateHocon)
  api(libs.configurateGuice)
  api(libs.adventureSerializerConfigurate4) {
    isTransitive = false
  }
  api(libs.adventureTextSerializerGson) {
    isTransitive = false
  }
  api(libs.typesafeConfig)

  // Other
  api(libs.guice)
}
