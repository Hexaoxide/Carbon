package resourcegenerator

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class ResourceGeneratorExtension @Inject constructor(
  private val objects: ObjectFactory
) {
  abstract val generators: ListProperty<ResourceGenerator>

  fun paperPluginYml(op: PaperPluginYml.() -> Unit): PaperPluginYml {
    val config = PaperPluginYml(objects)
    config.op()
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
