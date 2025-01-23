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

tasks.jar {
  manifest.attributes(
    "FMLModType" to "GAMELIBRARY",
    "Automatic-Module-Name" to "de.hexaoxi.carbonchat.mod.common",
  )
}
