import net.draycia.carbon.*

description="CarbonChat-API"

dependencies {
  api("org.checkerframework:checker-qual:${CHECKER_QUAL_VER}")

  api("net.kyori:adventure-api:$ADVENTURE_VER")
  api("net.kyori:adventure-nbt:$ADVENTURE_VER")
  api("net.kyori:adventure-platform-api:$ADVENTURE_PLATFORM_VER")
  api("net.kyori:adventure-text-serializer-plain:$ADVENTURE_VER")
  api("net.kyori:adventure-text-serializer-gson:$ADVENTURE_VER") {
    //excludeGroup("com.google.code.gson")
  }
  api("net.kyori:adventure-text-serializer-legacy:$ADVENTURE_VER")
  api("net.kyori:adventure-text-minimessage:$MINIMESSAGE_VER")

  api("net.kyori:event-api:$KYORI_EVENT_API_VER")
  api("net.kyori:registry:$KYORI_REGISTRY_VER")

  api("cloud.commandframework:cloud-core:$CLOUD_VER")

  api("org.spongepowered:configurate-core:$CONFIGURATE_VER")

  api("com.google.guava:guava:$GUAVA_VER")

  compileOnlyApi("com.proximyst.moonshine:core:$MOONSHINE_VER")

  // Provided by Minecraft
  compileOnlyApi("org.apache.logging.log4j:log4j-api:2.11.2")

  compileOnlyApi("net.luckperms:api:$LUCKPERMS_API_VER")
}


publishing {
  /*
  publications {
    maven(MavenPublication) {
      groupId = "net.draycia"
      artifactId = description
      version = projectVersion // No hash

      from components.java
    }
  }
   */
}
