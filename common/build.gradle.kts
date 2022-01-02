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
  api(libs.typesafeConfig)

  // Cloud
  api(platform(libs.cloudBom))
  api(libs.cloudCore)
  api(libs.cloudMinecraftExtras) {
    isTransitive = false
  }

  // Other
  api(libs.guice)
  compileOnlyApi(libs.luckPermsApi)

  // Storage
  api(libs.jdbiCore)
  api(libs.jdbiObject)
  api(libs.jdbiPostgres)
  api(libs.hikariCP)
  api(libs.flyway)
  api(libs.flywayMysql)
  api(libs.mysql)
  api(libs.caffeine)
  api(libs.mariadb)
}
