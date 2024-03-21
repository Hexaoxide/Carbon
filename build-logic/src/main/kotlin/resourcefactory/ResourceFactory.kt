package resourcefactory

import java.nio.file.Path

interface ResourceFactory {
  fun generate(outputDir: Path)
}
