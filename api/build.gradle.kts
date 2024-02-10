plugins {
  id("carbon.publishing-conventions")
  alias(libs.plugins.javadoc.links)
}

description = "API for interfacing with the CarbonChat Minecraft mod/plugin"

dependencies {
  // Doesn't add any dependencies, only version constraints
  api(platform(libs.adventureBom))

  // Provided by platform
  compileOnlyApi(libs.adventureApi)
  compileOnlyApi(libs.adventureTextSerializerPlain)
  compileOnlyApi(libs.adventureTextSerializerLegacy)
  compileOnlyApi(libs.adventureTextSerializerGson) {
    exclude("com.google.code.gson")
  }
  compileOnlyApi(libs.minimessage)

  compileOnlyApi(libs.checkerQual)

  // Provided by Minecraft
  compileOnlyApi(libs.gson)
}
