plugins {
  id("carbon.base-conventions")
}

dependencies {
  api(projects.carbonchatApi)

  // Configs
  api(libs.configurateHocon)
  api(libs.adventureSerializerConfigurate4) {
    isTransitive = false
  }
  //api(libs.typesafeConfig)

  // Cloud
  api(platform(libs.cloudBom))
  api(libs.cloudCore)
  api(libs.cloudMinecraftExtras) {
    isTransitive = false
  }

  // Other
  compileOnlyApi(libs.guice) {
    exclude("com.google.guava")
  }
  compileOnlyApi(libs.assistedInject) {
    isTransitive = false
  }
  compileOnlyApi(libs.luckPermsApi)

  // Storage
  compileOnlyApi(libs.jdbiCore)
  compileOnlyApi(libs.jdbiObject)
  compileOnlyApi(libs.jdbiPostgres)
  api(libs.hikariCP)
  api(libs.flyway)
  api(libs.flywayMysql)

  // Messaging
  api(libs.messenger)
  api(libs.messengerNats)
  api(libs.messengerRabbitmq)
  api(libs.messengerRedis)
  compileOnlyApi(libs.netty)

  compileOnlyApi(libs.jarRelocator)
  compileOnlyApi(libs.caffeine)

  // we shade and relocate a newer version than minecraft provides
  compileOnlyApi(libs.guava)

  // Plugins
  compileOnly(libs.miniplaceholders)
}
