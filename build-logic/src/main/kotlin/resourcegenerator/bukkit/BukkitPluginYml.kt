package resourcegenerator.bukkit

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import resourcegenerator.ConfigurateSingleFileResourceGenerator
import resourcegenerator.ResourceGenerator
import resourcegenerator.nullIfEmpty
import java.nio.file.Path

fun Project.bukkitPluginYml(op: BukkitPluginYml.() -> Unit = {}): BukkitPluginYml {
  val yml = BukkitPluginYml(objects)
  yml.copyProjectMeta(this)
  yml.op()
  return yml
}

class BukkitPluginYml(
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
  val description: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val load: Property<PluginLoadOrder> = objects.property()

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
  val depend: ListProperty<String> = objects.listProperty()

  @get:Input
  @get:Optional
  val softDepend: ListProperty<String> = objects.listProperty()

  @get:Input
  @get:Optional
  val loadBefore: ListProperty<String> = objects.listProperty()

  @get:Input
  @get:Optional
  val prefix: Property<String> = objects.property()

  @get:Input
  @get:Optional
  val defaultPermission: Property<Permission.Default> = objects.property()

  @get:Input
  @get:Optional
  val provides: ListProperty<String> = objects.listProperty()

  @get:Input
  @get:Optional
  val libraries: ListProperty<String> = objects.listProperty()

  @get:Nested
  val commands: NamedDomainObjectContainer<Command> = objects.domainObjectContainer(Command::class.java) { Command(it) }

  @get:Nested
  val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class.java) { Permission(it) }

  @get:Input
  @get:Optional
  val foliaSupported: Property<Boolean> = objects.property()

  enum class PluginLoadOrder {
    STARTUP,
    POSTWORLD
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

  @ConfigSerializable
  data class Command(
    @Transient
    @Input val name: String
  ) {
    @Input
    @Optional
    var description: String? = null

    @Input
    @Optional
    var aliases: List<String>? = null

    @Input
    @Optional
    var permission: String? = null

    @Input
    @Optional
    var permissionMessage: String? = null

    @Input
    @Optional
    var usage: String? = null
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
    gen.path.set("plugin.yml")
    gen.value.set(this)
    return gen
  }

  override fun asConfigSerializable(): Any {
    return Serializable(this)
  }

  @ConfigSerializable
  class Serializable(yml: BukkitPluginYml) {
    var apiVersion = yml.apiVersion.orNull
    var name = yml.name.get()
    var version = yml.version.get()
    var main = yml.main.get()
    var description = yml.description.orNull
    var load = yml.load.orNull
    var author = yml.author.orNull
    var authors = yml.authors.nullIfEmpty()
    var website = yml.website.orNull
    var depend = yml.depend.nullIfEmpty()
    var softDepend = yml.softDepend.nullIfEmpty()
    var loadBefore = yml.loadBefore.nullIfEmpty()
    var prefix = yml.prefix.orNull
    var defaultPermission = yml.defaultPermission.orNull
    var provides = yml.provides.nullIfEmpty()
    var libraries = yml.libraries.nullIfEmpty()
    val commands = yml.commands.asMap.toMap()
    val permissions = yml.permissions.asMap.toMap()
    val foliaSupported = yml.foliaSupported.orNull
  }
}
