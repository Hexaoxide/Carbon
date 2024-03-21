package resourcegenerator

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.newInstance
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path

fun Project.bukkitPluginYml(op: BukkitPluginYml.() -> Unit = {}): BukkitPluginYml {
  val yml = BukkitPluginYml(objects)
  yml.op()
  return yml
}

class BukkitPluginYml(
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
  @Optional
  var description: String? = null

  @Input
  @Optional
  var load: PluginLoadOrder? = null

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
  var depend: List<String>? = null

  @Input
  @Optional
  var softDepend: List<String>? = null

  @Input
  @Optional
  var loadBefore: List<String>? = null

  @Input
  @Optional
  var prefix: String? = null

  @Input
  @Optional
  var defaultPermission: Permission.Default? = null

  @Input
  @Optional
  var provides: List<String>? = null

  @Input
  @Optional
  var libraries: List<String>? = null

  @Nested
  val commands: NamedDomainObjectContainer<Command> = objects.domainObjectContainer(Command::class.java) { Command(it) }

  @Nested
  val permissions: NamedDomainObjectContainer<Permission> = objects.domainObjectContainer(Permission::class.java) { Permission(it) }

  enum class PluginLoadOrder {
    STARTUP,
    POSTWORLD
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
    var apiVersion = yml.apiVersion
    var name = yml.name
    var version = yml.version
    var main = yml.main
    var description = yml.description
    var load = yml.load
    var author = yml.author
    var authors = yml.authors?.toList()
    var website = yml.website
    var depend = yml.depend?.toList()
    var softDepend = yml.softDepend?.toList()
    var loadBefore = yml.loadBefore?.toList()
    var prefix = yml.prefix
    var defaultPermission = yml.defaultPermission
    var provides = yml.provides?.toList()
    var libraries = yml.libraries?.toList()
    val commands = yml.commands.asMap.toMap()
    val permissions = yml.permissions.asMap.toMap()
  }
}
