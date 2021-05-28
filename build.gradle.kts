plugins {
  id("carbon.build-logic")
}

group = "net.draycia"
description = "CarbonChat - A modern chat plugin"
val projectVersion: String by project // get from gradle.properties
version = projectVersion

subprojects {
  plugins.apply("carbon.base-conventions")

  dependencies {
    "checkstyle"(rootProject.libs.stylecheck)
  }
}
