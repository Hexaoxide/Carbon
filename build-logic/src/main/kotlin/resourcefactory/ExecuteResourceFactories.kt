package resourcefactory

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import kotlin.io.path.createDirectories

abstract class ExecuteResourceFactories : DefaultTask() {
  @get:Nested
  abstract val factories: ListProperty<ResourceFactory>

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:Inject
  abstract val fsOps: FileSystemOperations

  @TaskAction
  fun run() {
    val dir = outputDir.get().asFile.toPath()
    fsOps.delete {
      delete(dir)
    }

    dir.createDirectories()

    for (generator in factories.get()) {
      generator.generate(dir)
    }
  }
}
