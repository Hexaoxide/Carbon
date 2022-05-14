plugins {
  id("carbon.base-conventions")
}

dependencies {
  // Doesn't add any dependencies, only version constraints
  api(platform(libs.adventureBom))
  api(platform(libs.log4jBom))

  // Provided by platform
  compileOnlyApi(libs.adventureApi)
  compileOnlyApi(libs.adventureTextSerializerPlain)
  compileOnlyApi(libs.adventureTextSerializerLegacy)
  compileOnlyApi(libs.adventureTextSerializerGson) {
    exclude("com.google.code.gson")
  }
  compileOnlyApi(libs.minimessage)

  compileOnlyApi(libs.checkerQual)

  api(libs.kyoriEventApi)
  api(libs.kyoriRegistry)
  api(libs.kyoriMoonshine)
  api(libs.kyoriMoonshineCore)
  api(libs.kyoriMoonshineStandard)

  // we shade and relocate a newer version than minecraft provides
  api(libs.guava)

  // Provided by Minecraft
  compileOnlyApi(libs.gson)
  compileOnlyApi(libs.log4jApi)
}
