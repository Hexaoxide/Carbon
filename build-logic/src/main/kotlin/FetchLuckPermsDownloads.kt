import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.net.URI
import javax.inject.Inject

@UntrackedTask(because = "Always check for new metadata")
abstract class FetchLuckPermsDownloads : DefaultTask() {
  companion object {
    const val ENDPOINT: String = "https://metadata.luckperms.net/data/downloads"
  }

  @get:Inject
  abstract val layout: ProjectLayout

  @get:OutputFile
  abstract val outputFile: RegularFileProperty

  init {
    init()
  }

  private fun init() {
    outputFile.convention(layout.buildDirectory.file("luckperms/downloads.json"))
  }

  @TaskAction
  fun run () {
    val url = URI.create(ENDPOINT).toURL()
    val data = url.readText(Charsets.UTF_8)
    val outFile = outputFile.get().asFile.also {
      it.parentFile.mkdirs()
      if (it.exists()) {
        it.delete()
      }
    }
    outFile.writeText(data, Charsets.UTF_8)
  }
}
