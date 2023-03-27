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
  api(libs.guice) {
    exclude("com.google.guava")
  }
  api(libs.assistedInject)
  compileOnlyApi(libs.luckPermsApi)

  // Storage
  api(libs.jdbiCore)
  api(libs.jdbiObject)
  api(libs.jdbiPostgres)
  api(libs.hikariCP)
  api(libs.flyway)
  api(libs.flywayMysql)
  compileOnlyApi(libs.mysql) {
    exclude ("com.google.protobuf")
  }
  api(libs.postgresql)
  api(libs.caffeine)
  api(libs.mariadb)

  // Messaging
  api(libs.messengerNats)
  api(libs.messengerRabbitmq)
  api(libs.messengerRedis)
  api(libs.netty)
  compileOnlyApi(libs.zstdjni)
  api(libs.jedis)
  api(libs.rabbitmq)
  api(libs.nats)
}
