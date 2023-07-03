import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class WriteDependencies : DefaultTask() {
  @get:Input
  abstract val tree: Property<ResolvedComponentResult>

  @get:InputFiles
  abstract val files: ConfigurableFileCollection

  @get:Input
  abstract val outputFileName: Property<String>

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:Input
  abstract val repos: ListProperty<String>

  @get:Input
  abstract val transitive: Property<Boolean>

  @get:Input
  abstract val relocations: ListProperty<String>

  init {
    init()
  }

  private fun init() {
    transitive.convention(true)
  }

  fun relocate(from: String, to: String) {
    relocations.add("$from $to")
  }

  @TaskAction
  fun run() {
    val outputLines = StringBuilder()
    val outputFile = outputDir.get().file(outputFileName.get()).asFile
    val files = files.files
    for (repo in repos()) {
      outputLines.append(repo).append("\n")
    }
    outputLines.append("end_repos\n")
    val seen = mutableSetOf<String>()
    for (dependency in deps()) {
      val id = dependency.resolvedVariant.owner as ModuleComponentIdentifier
      if (id.displayName in seen) {
        continue
      }
      seen += id.displayName
      val file = files.single { it.name.equals("${id.module}-${id.version}.jar") }
      outputLines.append(id.displayName).append(" ").append(file.toPath().hashFile(HashingAlgorithm.SHA256).asHexString()).append("\n")
    }
    outputLines.append("end_deps\n")
    for (r in relocations.get()) {
      outputLines.append(r).append("\n")
    }
    outputFile.parentFile.mkdirs()
    outputFile.delete()
    outputFile.writeText(outputLines.toString())
  }

  private fun repos(): List<String> {
    if (repos.get().isNotEmpty()) {
      return repos.get()
    }
    if (project.repositories.isNotEmpty()) {
      return project.repositories.mapNotNull { (it as? MavenArtifactRepository)?.url.toString() }
    }
    return project.settings.dependencyResolutionManagement.repositories.mapNotNull { (it as? MavenArtifactRepository)?.url.toString() }
  }

  private val Project.settings: Settings
    get() = (gradle as GradleInternal).settings

  private fun deps(): MutableSet<ResolvedDependencyResult> {
    val ret = mutableSetOf<ResolvedDependencyResult>()
    ret.addFrom(tree.get().dependencies)
    return ret
  }

  private fun MutableSet<ResolvedDependencyResult>.addFrom(dependencies: Set<DependencyResult>) {
    for (dependency in dependencies) {
      dependency as ResolvedDependencyResult
      add(dependency)
      if (transitive.get()) {
        addFrom(dependency.selected.dependencies)
      }
    }
  }
}
