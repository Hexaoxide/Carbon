import org.gradle.api.file.RegularFileProperty

abstract class CarbonPlatformExtension {
  abstract val productionJar: RegularFileProperty
}
