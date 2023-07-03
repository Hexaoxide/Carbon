import java.util.Locale

plugins {
  id("carbon.shadow-platform")
  id("net.kyori.blossom")
  id("xyz.jpenilla.run-velocity")
}

dependencies {
  implementation(projects.carbonchatCommon)

  compileOnly(libs.velocityApi)
  annotationProcessor(libs.velocityApi)

  implementation(libs.cloudVelocity)
  compileOnly(libs.miniplaceholders)

  runtimeOnly(libs.jarRelocator)

  runtimeDownload(libs.mysql)
}

tasks {
  shadowJar {
    dependencies {
      relocateDependency("io.leangen.geantyref")
      relocateDependency("com.google.inject.assistedinject")

      relocateDependency("com.github.luben.zstd")
      relocateDependency("me.lucko.jarrelocator")
      relocateDependency("org.objectweb.asm")
      relocateDependency("org.jdbi")
      relocateDependency("com.github.benmanes")

      relocateDependency("io.nats")
      relocateDependency("org.apache.commons.pool2")
      relocateDependency("redis.clients.jedis")
      relocateDependency("com.rabbitmq")

      // included in velocity
      exclude(dependency("com.google.inject:guice"))
      exclude(dependency("aopalliance:aopalliance"))
      exclude(dependency("javax.inject:javax.inject"))
    }
  }
  writeDependencies {
    relocateDependency("org.postgresql")
    relocateDependency("com.google.protobuf")
    relocateDependency("com.mysql.cj")
    relocateDependency("com.mysql.jdbc")
    relocateDependency("org.mariadb.jdbc")

    relocateDependency("org.jdbi")
    relocateDependency("com.github.benmanes")

    relocateDependency("io.nats")
    relocateDependency("net.i2p.crypto")
    relocateDependency("org.apache.commons.pool2")
    relocateDependency("redis.clients.jedis")
    relocateDependency("com.rabbitmq")

    relocateDependency("com.google.inject.assistedinject")
  }
  runVelocity {
      velocityVersion(libs.versions.velocityApi.get())
  }
}

blossom {
  mapOf(
    "ID" to rootProject.name.lowercase(Locale.ROOT),
    "NAME" to rootProject.name,
    "VERSION" to version,
    "DESCRIPTION" to description,
    "URL" to GITHUB_REPO_URL
  ).forEach { (k, v) ->
    replaceToken("$[$k]", v)
  }
}

modrinth {
  optional.project("unsignedvelocity")
}
