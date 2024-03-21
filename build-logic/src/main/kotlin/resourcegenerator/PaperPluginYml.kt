package resourcegenerator

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path
import javax.inject.Inject

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
  var dependencies: Dependencies = objects.newInstance(Dependencies::class)

  @Nested
  val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class) { Permission(it) }

  fun dependencies(op: Dependencies.() -> Unit) {
    dependencies.op()
  }

  enum class Load {
    BEFORE,
    AFTER,
    OMIT
  }

  abstract class Dependencies @Inject constructor(objects: ObjectFactory) {
    @get:Nested
    val bootstrap: NamedDomainObjectContainer<Dependency> = objects.domainObjectContainer(Dependency::class) { Dependency(objects, it) }

    @get:Nested
    val server: NamedDomainObjectContainer<Dependency> = objects.domainObjectContainer(Dependency::class) { Dependency(objects, it) }

    fun bootstrap(
      name: String,
      load: Load = Load.OMIT,
      required: Boolean = true,
      joinClasspath: Boolean = true
    ): NamedDomainObjectProvider<Dependency> = bootstrap.register(name) {
      this.load.set(load)
      this.required.set(required)
      this.joinClasspath.set(joinClasspath)
    }

    fun server(
      name: String,
      load: Load = Load.OMIT,
      required: Boolean = true,
      joinClasspath: Boolean = true
    ): NamedDomainObjectProvider<Dependency> = server.register(name) {
      this.load.set(load)
      this.required.set(required)
      this.joinClasspath.set(joinClasspath)
    }
  }

  class Dependency(
    objects: ObjectFactory,
    @get:Internal
    val name: String
  ) {
    @get:Input
    val load: Property<Load> = objects.property<Load>().convention(Load.OMIT)

    @get:Input
    val required: Property<Boolean> = objects.property<Boolean>().convention(true)

    @get:Input
    val joinClasspath: Property<Boolean> = objects.property<Boolean>().convention(true)
  }

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
    val dependencies = SerializableDependencies.from(yml.dependencies)
    val permissions = yml.permissions.asMap.toMap()
  }

  @ConfigSerializable
  data class SerializableDependency(val load: Load, val required: Boolean, val joinClasspath: Boolean) {
    companion object {
      fun from(dep: Dependency) = SerializableDependency(dep.load.get(), dep.required.get(), dep.joinClasspath.get())
    }
  }

  @ConfigSerializable
  data class SerializableDependencies(
    val bootstrap: Map<String, SerializableDependency>,
    val server: Map<String, SerializableDependency>
  ) {
    companion object {
      fun from(deps: Dependencies) = SerializableDependencies(
        deps.bootstrap.asMap.mapValues { SerializableDependency.from(it.value) },
        deps.server.asMap.mapValues { SerializableDependency.from(it.value) }
      )
    }
  }
}
