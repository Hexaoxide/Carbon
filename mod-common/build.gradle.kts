plugins {
  id("carbon.base-conventions")
  id("net.neoforged.moddev")
}

neoForge {
  enable {
    neoFormVersion = libs.versions.neoform.get()
  }
}

dependencies {
  api(projects.carbonchatCommon) {
    exclude("org.slf4j")
  }
}
