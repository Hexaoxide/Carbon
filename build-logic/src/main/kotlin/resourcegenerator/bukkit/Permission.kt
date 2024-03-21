package resourcegenerator.bukkit

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

@ConfigSerializable
data class Permission(@Input @Transient val name: String) {
  @Input
  @Optional
  var description: String? = null

  @Input
  @Optional
  var default: Default? = null
  var children: List<String>?
    @Internal get() = childrenMap?.filterValues { it }?.keys?.toList()
    set(value) {
      childrenMap = value?.associateWith { true }
    }

  @Input
  @Optional
  @Setting("children")
  var childrenMap: Map<String, Boolean>? = null

  enum class Default(val serialized: String) {
    TRUE("true"),
    FALSE("false"),
    OP("op"),
    NOT_OP("not op");

    object Serializer : TypeSerializer<Default> {
      override fun deserialize(type: Type?, node: ConfigurationNode?): Default {
        throw UnsupportedOperationException()
      }

      override fun serialize(type: Type, obj: Default?, node: ConfigurationNode) {
        node.set(obj?.serialized)
      }
    }
  }
}
