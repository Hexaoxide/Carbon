dependencies {
  // Doesn't add any dependencies, only version constraints
  api(platform(libs.adventureBom))
  api(platform(libs.log4jBom))

  // These are provided by Paper and Sponge
  compileOnlyApi(libs.adventureApi)
  compileOnlyApi(libs.adventureTextSerializerPlain)
  compileOnlyApi(libs.adventureTextSerializerLegacy)
  compileOnlyApi(libs.adventureTextSerializerGson) {
    exclude("com.google.code.gson")
  }

  api(libs.minimessage) {
    isTransitive = false
  }

  compileOnlyApi(libs.checkerQual)

  api(libs.kyoriEventApi)
  api(libs.kyoriRegistry)

  api(libs.cloudCore)
  api(libs.cloudMinecraftExtras) {
    isTransitive = false
  }

  api(libs.moonshine)

  // we shade and relocate a newer version than minecraft provides
  api(libs.guava)

  // Provided by Minecraft
  compileOnlyApi(libs.gson)
  compileOnlyApi(libs.log4jApi)
}
