package resourcegenerator

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.nio.file.Path

abstract class SingleFileResourceGenerator : ResourceGenerator {
  @get:Input
  abstract val path: Property<String>

  abstract fun generateSingleFile(outputFile: Path)

  override fun generate(outputDir: Path) {
    generateSingleFile(outputDir.resolve(path.get()))
  }
}
