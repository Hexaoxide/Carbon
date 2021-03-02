description = "CarbonChat-API"

dependencies {
  api("org.checkerframework", "checker-qual", Versions.CHECKER_QUAL)

  api("net.kyori", "adventure-api", Versions.ADVENTURE)
  api("net.kyori", "adventure-nbt", Versions.ADVENTURE)
  api("net.kyori", "adventure-serializer-configurate4", Versions.ADVENTURE)
  api("net.kyori", "adventure-text-serializer-plain", Versions.ADVENTURE)
  api("net.kyori", "adventure-text-serializer-gson", Versions.ADVENTURE) {
    exclude(group = "com.google.code.gson")
  }
  api("net.kyori", "adventure-text-serializer-legacy", Versions.ADVENTURE)
  api("net.kyori", "adventure-text-minimessage", Versions.MINIMESSAGE)

  api("de.themoep", "minedown-adventure", Versions.MINEDOWN)

  api("net.kyori", "event-api", Versions.KYORI_EVENT)
  api("net.kyori", "registry", Versions.KYORI_REGISTRY)

  api("cloud.commandframework", "cloud-core", Versions.CLOUD)

  api("org.spongepowered", "configurate-core", Versions.CONFIGURATE)

  api("com.google.guava", "guava", Versions.GUAVA)

  compileOnlyApi("org.slf4j", "slf4j-api", Versions.SLF4J)

  compileOnlyApi("net.luckperms", "api", Versions.LUCKPERMS)
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      groupId = rootProject.group as String
      artifactId = description
      version = rootProject.version as String

      from(components["java"])
    }
  }
}
