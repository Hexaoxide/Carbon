package resourcegenerator

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path

fun Project.paperPluginYml(op: PaperPluginYml.() -> Unit = {}): PaperPluginYml {
  val yml = PaperPluginYml(objects)
  yml.op()
  return yml
}

class PaperPluginYml constructor(
  @Transient
  private val objects: ObjectFactory
) : ConfigurateSingleFileResourceGenerator.ObjectMapper.ValueProvider {

  @Input
  @Optional
  var apiVersion: String? = null

  @Input
  var name: String? = null

  @Input
  var version: String? = null

  @Input
  var main: String? = null

  @Input
  var loader: String? = null

  @Input
  @Optional
  var description: String? = null

  @Input
  @Optional
  var author: String? = null

  @Input
  @Optional
  var authors: List<String>? = null

  @Input
  @Optional
  var website: String? = null

  @Input
  @Optional
  var prefix: String? = null

  @Input
  @Optional
  var defaultPermission: Permission.Default? = null

  @Input
  @Optional
  var foliaSupported: Boolean? = null

  @Nested
  var dependencies: Dependencies = Dependencies()

  @Nested
  val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class) { Permission(it) }

  fun bootstrapDependency(name: String, load: Load = Load.OMIT, required: Boolean = true, joinClasspath: Boolean = true) {
    dependencies.bootstrap[name] = Dependency(load, required, joinClasspath)
  }

  fun dependency(name: String, load: Load = Load.OMIT, required: Boolean = true, joinClasspath: Boolean = true) {
    dependencies.server[name] = Dependency(load, required, joinClasspath)
  }

  enum class Load {
    BEFORE,
    AFTER,
    OMIT
  }

  @ConfigSerializable
  data class Dependencies(
    @Nested
    val bootstrap: MutableMap<String, Dependency> = mutableMapOf(),
    @Nested
    val server: MutableMap<String, Dependency> = mutableMapOf()
  )

  @ConfigSerializable
  data class Dependency(
    @Input val load: Load = Load.OMIT,
    @Input val required: Boolean = true,
    @Input val joinClasspath: Boolean = true
  )

  // For Groovy DSL
  fun permissions(closure: Closure<Unit>) = permissions.configure(closure)

  fun generator(): ResourceGenerator {
    val gen = objects.newInstance(
      ConfigurateSingleFileResourceGenerator.ObjectMapper::class,
      { path: Path ->
        YamlConfigurationLoader.builder()
          .defaultOptions {
            it.serializers { s ->
              s.registerExact(Permission.Default::class.java, Permission.Default.Serializer)
            }
          }
          .path(path)
          .build()
      }
    )
    gen.path.set("paper-plugin.yml")
    gen.value.set(this)
    return gen
  }

  override fun asConfigSerializable(): Any {
    return Serializable(this)
  }

  @ConfigSerializable
  class Serializable(yml: PaperPluginYml) {
    val apiVersion = yml.apiVersion
    val name = yml.name
    val version = yml.version
    val main = yml.main
    val loader = yml.loader
    val description = yml.description
    val author = yml.author
    val authors = yml.authors?.toList()
    val website = yml.website
    val prefix = yml.prefix
    val defaultPermission = yml.defaultPermission
    val foliaSupported = yml.foliaSupported
    val dependencies = yml.dependencies.copy(
      bootstrap = yml.dependencies.bootstrap.toMutableMap(),
      server = yml.dependencies.server.toMutableMap()
    )
    val permissions = yml.permissions.asMap.toMap()
  }
}
