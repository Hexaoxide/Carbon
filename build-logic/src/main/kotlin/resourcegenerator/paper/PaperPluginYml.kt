package resourcegenerator.paper

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import resourcegenerator.ConfigurateSingleFileResourceGenerator
import resourcegenerator.ResourceGenerator
import resourcegenerator.bukkit.Permission
import resourcegenerator.nullIfEmpty
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

  @get:Input
  @get:Optional
  val apiVersion: Property<String> = objects.property()

  @get:Input
  val name: Property<String> = objects.property()

  @get:Input
  val version: Property<String> = objects.property()

  @get:Input
  val main: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val loader: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val bootstrapper: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val description: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val author: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val authors: ListProperty<String> = objects.listProperty()

  @get:Input
  @get:Optional
  val website: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val prefix: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val defaultPermission: Property<Permission.Default> = objects.property()

  @get:Input
  @get:Optional
  val foliaSupported: Property<Boolean> = objects.property()

  @get:Nested
  var dependencies: Dependencies = objects.newInstance(Dependencies::class)

  @get:Nested
  val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class) { Permission(it) }

  fun dependencies(op: Dependencies.() -> Unit) {
    dependencies.op()
  }

  /**
   * Copy the name, version, and description from the provided project.
   *
   * [project] project
   */
  fun copyProjectMeta(project: Project) {
    name.convention(project.name)
    version.convention(project.version as String?)
    description.convention(project.description)
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
    val apiVersion = yml.apiVersion.orNull
    val name = yml.name.get()
    val version = yml.version.get()
    val main = yml.main.get()
    val loader = yml.loader.orNull
    val bootstrapper = yml.bootstrapper.orNull
    val description = yml.description.orNull
    val author = yml.author.orNull
    val authors = yml.authors.nullIfEmpty()
    val website = yml.website.orNull
    val prefix = yml.prefix.orNull
    val defaultPermission = yml.defaultPermission.orNull
    val foliaSupported = yml.foliaSupported.orNull
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
