plugins {
  id("carbon.publishing-conventions")
}

description = "API for interfacing with the CarbonChat Minecraft mod/plugin"

val docs: Configuration by configurations.creating {
  attributes {
    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
    attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
  }
}
configurations.compileOnlyApi {
  extendsFrom(docs)
}

dependencies {
  // Doesn't add any dependencies, only version constraints
  api(platform(libs.adventureBom))
  docs(platform(libs.adventureBom))

  // Provided by platform
  docs(libs.adventureApi)
  compileOnlyApi(libs.adventureTextSerializerPlain)
  compileOnlyApi(libs.adventureTextSerializerLegacy)
  compileOnlyApi(libs.adventureTextSerializerGson) {
    exclude("com.google.code.gson")
  }
  compileOnlyApi(libs.minimessage)

  docs(libs.checkerQual)

  // Provided by Minecraft
  compileOnlyApi(libs.gson)
}

tasks.withType<Javadoc> {
  val options = options as StandardJavadocDocletOptions
  options.links(
    "https://jd.advntr.dev/api/${libs.versions.adventure.get()}/",
    "https://checkerframework.org/api/",
  )
  inputs.files(docs).ignoreEmptyDirectories().withPropertyName(docs.name + "-configuration")
  doFirst {
    options.addStringOption(
      "sourcepath",
      docs.resolvedConfiguration.files.joinToString(separator = File.pathSeparator, transform = File::getPath)
    )
  }
}
