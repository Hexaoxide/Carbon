import net.draycia.carbon.*


plugins {
  id "java-library"
  id "maven-publish"
}

description "CarbonChat-API"

dependencies {
  api "org.checkerframework:checker-qual:${CHECKER_QUAL_VER}"

  api "net.kyori:adventure-api:$ADVENTURE_VER"
  api "net.kyori:adventure-nbt:$ADVENTURE_VER"
  api "net.kyori:adventure-platform-api:$ADVENTURE_PLATFORM_VER"
  api "net.kyori:adventure-text-serializer-plain:$ADVENTURE_VER"
  api("net.kyori:adventure-text-serializer-gson:$ADVENTURE_VER") {
    exclude group: "com.google.code.gson"
  }
  api "net.kyori:adventure-text-serializer-legacy:$ADVENTURE_VER"
  api "net.kyori:adventure-text-minimessage:$MINIMESSAGE_VER}"

  api("net.kyori:event-api:${vers["kyori-event-api"]}")
  api("net.kyori:registry:${vers["kyori-registry"]}")

  api "cloud.commandframework:cloud-core:${vers["cloud"]}"

  api "org.spongepowered:configurate-core:${vers["configurate"]}"

  api "com.google.guava:guava:${vers["guava"]}"

  compileOnlyApi "com.proximyst.moonshine:core:${vers["moonshine"]}"

  compileOnlyApi "org.slf4j:slf4j-api:${vers["slf4j-api"]}"

  compileOnlyApi "net.luckperms:api:${vers["luckperms-api"]}"
}


publishing {
  publications {
    maven(MavenPublication) {
      groupId = "net.draycia"
      artifactId = description
      version = projectVersion // No hash

      from components.java
    }
  }
}
