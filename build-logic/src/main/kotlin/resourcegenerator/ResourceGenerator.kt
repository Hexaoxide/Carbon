package resourcegenerator

import java.nio.file.Path

interface ResourceGenerator {
  fun generate(outputDir: Path)
}
