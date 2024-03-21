package resourcegenerator

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.newInstance
import resourcegenerator.bukkit.BukkitPluginYml
import resourcegenerator.bukkit.bukkitPluginYml
import resourcegenerator.paper.PaperPluginYml
import resourcegenerator.paper.paperPluginYml
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class ResourceGeneratorExtension @Inject constructor(
  private val objects: ObjectFactory,
  private val project: Project
) {
  abstract val generators: ListProperty<ResourceGenerator>

  fun paperPluginYml(op: PaperPluginYml.() -> Unit): PaperPluginYml {
    val config = project.paperPluginYml(op)
    generator(config.generator())
    return config
  }

  fun bukkitPluginYml(op: BukkitPluginYml.() -> Unit): BukkitPluginYml {
    val config = project.bukkitPluginYml(op)
    generator(config.generator())
    return config
  }

  fun <T : ResourceGenerator> generator(
    generatorType: KClass<T>,
    vararg params: Any,
    op: T.() -> Unit
  ): T {
    val o = objects.newInstance(generatorType, *params)
    o.op()
    generator(o)
    return o
  }

  fun generator(generator: ResourceGenerator) {
    generators.add(generator)
  }

  fun generators(vararg generator: ResourceGenerator) {
    generators.addAll(*generator)
  }
}
