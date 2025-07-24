import com.google.gson.Gson
import com.google.gson.JsonElement
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.net.URI
import javax.inject.Inject

@CacheableTask
abstract class FetchLuckPermsJar : DefaultTask() {
  companion object {
    fun setup(
      project: Project,
      type: String,
    ): TaskProvider<FetchLuckPermsJar> {
      val getMeta = project.tasks.register<FetchLuckPermsDownloads>("fetchLuckPermsDownloads")
      return project.tasks.register<FetchLuckPermsJar>("fetchLuckPermsJar") {
        this.type.set(type)
        inputFile.set(getMeta.flatMap { it.outputFile })
      }
    }
  }

  @get:Inject
  abstract val layout: ProjectLayout

  @get:Input
  abstract val type: Property<String>

  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val inputFile: RegularFileProperty

  @get:OutputFile
  abstract val outputFile: RegularFileProperty

  init {
    init()
  }

  private fun init() {
    outputFile.convention(type.flatMap {
      layout.buildDirectory.file("luckperms/${it}.jar")
    })
  }

  @TaskAction
  fun run () {
    val json = inputFile.get().asFile.readText(Charsets.UTF_8)
    val map = Gson().fromJson(json, JsonElement::class.java).asJsonObject.get("downloads").asJsonObject
    val url = map.get(type.get()).asString
    val data = URI.create(url).toURL().readBytes()
    val outFile = outputFile.get().asFile.also {
      it.parentFile.mkdirs()
      if (it.exists()) {
        it.delete()
      }
    }
    outFile.writeBytes(data)
  }
}
