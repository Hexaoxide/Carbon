package resourcefactory

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.newInstance
import resourcefactory.bukkit.BukkitPluginYml
import resourcefactory.bukkit.bukkitPluginYml
import resourcefactory.paper.PaperPluginYml
import resourcefactory.paper.paperPluginYml
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class ResourceFactoryExtension @Inject constructor(
  private val objects: ObjectFactory,
  private val project: Project
) {
  abstract val factories: ListProperty<ResourceFactory>

  fun paperPluginYml(op: PaperPluginYml.() -> Unit): PaperPluginYml {
    val config = project.paperPluginYml(op)
    factory(config.generator())
    return config
  }

  fun bukkitPluginYml(op: BukkitPluginYml.() -> Unit): BukkitPluginYml {
    val config = project.bukkitPluginYml(op)
    factory(config.generator())
    return config
  }

  fun <T : ResourceFactory> factory(
    generatorType: KClass<T>,
    vararg params: Any,
    op: T.() -> Unit
  ): T {
    val o = objects.newInstance(generatorType, *params)
    o.op()
    factory(o)
    return o
  }

  fun factory(generator: ResourceFactory) {
    factories.add(generator)
  }

  fun factories(vararg generator: ResourceFactory) {
    factories.addAll(*generator)
  }
}
