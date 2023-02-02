import io.leangen.geantyref.TypeToken
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.listProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import javax.inject.Inject

abstract class CarbonPermissionsExtension @Inject constructor(private val objects: ObjectFactory) {
  abstract val yaml: RegularFileProperty

  val permissions: Provider<List<Permission>> = create()

  private fun create(): ListProperty<Permission> = objects.listProperty<Permission>().also {
    it.set(yaml.map { file ->
      val loader = YamlConfigurationLoader.builder()
        .path(file.asFile.toPath())
        .nodeStyle(NodeStyle.BLOCK)
        .build()
      loader.load().childrenMap().map { (name, child) ->
        loadPermission(name as String, child)
      }
    })
    it.disallowChanges()
    it.finalizeValueOnRead()
  }

  private fun loadPermission(name: String, node: ConfigurationNode): Permission {
    return if (node.isMap) {
      Permission(
        name,
        node.node("description").string,
        node.node("children").takeIf { c -> !c.virtual() }
          ?.get(object : TypeToken<HashMap<String, Boolean>>() {})
      )
    } else {
      Permission(name, node.string, null)
    }
  }

  data class Permission(
    val string: String,
    val description: String?,
    val children: Map<String, Boolean>?
  )
}
