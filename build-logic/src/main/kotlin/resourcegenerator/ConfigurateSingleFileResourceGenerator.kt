package resourcegenerator

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.loader.ConfigurationLoader
import java.nio.file.Path
import javax.inject.Inject

abstract class ConfigurateSingleFileResourceGenerator(
  private val loaderFactory: (Path) -> ConfigurationLoader<*>
) : SingleFileResourceGenerator() {
  override fun generateSingleFile(outputFile: Path) {
    val loader = loaderFactory(outputFile)
    loader.save(generateRootNode(loader))
  }

  abstract fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N

  abstract class ObjectMapper @Inject constructor(
    loaderFactory: (Path) -> ConfigurationLoader<*>
  ) : ConfigurateSingleFileResourceGenerator(loaderFactory) {
    @get:Nested
    abstract val value: Property<ValueProvider>

    fun value(value: Any) {
      this.value.set { value }
    }

    override fun <N : ConfigurationNode> generateRootNode(loader: ConfigurationLoader<N>): N {
      val node = loader.createNode()
      node.set(value.get().asConfigSerializable())
      return node
    }

    fun interface ValueProvider {
      fun asConfigSerializable(): Any
    }
  }
}
