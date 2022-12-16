plugins {
  `java-library`
  id("io.papermc.paperweight.userdev")
}

dependencies {
  api(projects.carbonchatPaper.internalsAccessorApi)
  paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.19.3-R0.1-SNAPSHOT")
  api("xyz.jpenilla:reflection-remapper:0.1.0-SNAPSHOT")
}

repositories {
  mavenCentral()
  maven("https://repo.jpenilla.xyz/snapshots/")
  maven("https://maven.fabricmc.net/")
}
