plugins {
  id 'org.spongepowered.plugin' version '0.9.0'
}

description = "CarbonChat-Sponge"

dependencies {
  implementation project(":CarbonChat-API")
  compileOnly 'org.spongepowered:spongeapi:8.0.0-SNAPSHOT'
  annotationProcessor 'org.spongepowered:spongeapi:7.3.0'
  implementation "cloud.commandframework:cloud-sponge:${vers['cloud']}"
}

sponge.plugin.id = 'CarbonChat-Sponge8'
