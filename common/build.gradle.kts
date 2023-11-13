plugins {
  id("carbon.base-conventions")
}

dependencies {
  api(projects.carbonchatApi)
  api(libs.gremlin.runtime)
  compileOnlyApi(platform(libs.log4jBom))
  compileOnlyApi(libs.log4jApi)

  // Configs
  api(libs.configurateHocon)
  api(libs.adventureSerializerConfigurate4) {
    isTransitive = false
  }

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
  compileOnlyApi(libs.event)

  // Storage
  compileOnlyApi(libs.jdbiCore)
  compileOnlyApi(libs.jdbiObject)
  compileOnlyApi(libs.jdbiPostgres)
  api(libs.hikariCP)
  compileOnlyApi(libs.flyway) {
    exclude("com.google.code.gson", "gson")
  }
  compileOnlyApi(libs.flywayMysql) {
    isTransitive = false
  }

  // Messaging
  api(libs.messenger)
  api(libs.messengerNats)
  api(libs.messengerRabbitmq)
  api(libs.messengerRedis)
  compileOnlyApi(libs.netty)

  api(libs.event)
  api(libs.registry) {
    exclude("com.google.guava")
  }
  api(libs.kyoriMoonshine)
  api(libs.kyoriMoonshineCore)
  api(libs.kyoriMoonshineStandard)

  compileOnlyApi(libs.caffeine)

  // we shade and relocate a newer version than minecraft provides
  compileOnlyApi(libs.guava)

  // Plugins
  compileOnly(libs.miniplaceholders)
}
