description = "CarbonChat-Common"

dependencies {
  api(project(":CarbonChat-API"))

  implementation("io.lettuce", "lettuce-core", Versions.LETTUCE) {
    exclude(group = "io.netty")
  }

  implementation("net.kyori", "adventure-serializer-configurate4", Versions.ADVENTURE)

  implementation("org.spongepowered", "configurate-hocon", Versions.CONFIGURATE)
  implementation("org.spongepowered", "configurate-yaml", Versions.CONFIGURATE)

  implementation("co.aikar", "idb-core", Versions.IDB)
  implementation("com.zaxxer", "HikariCP", Versions.HIKARI)

  compileOnly("com.google.code.gson", "gson", Versions.GSON)

  compileOnly("org.slf4j", "slf4j-api", Versions.SLF4J)
}
