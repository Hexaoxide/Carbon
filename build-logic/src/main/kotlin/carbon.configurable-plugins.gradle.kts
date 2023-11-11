import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.jpenilla.runtask.task.RunWithPlugins

val ext = extensions.create("configurablePlugins", ConfigurablePluginsExt::class.java)

afterEvaluate {
  ext.gradleDependencyBased.get().forEach { entry ->
    val c = configurations.register(entry.name + "Plugin") {
      isTransitive = false
    }
    dependencies {
      c.name(entry.dep) { entry.op?.execute(this) }
    }
    tasks.withType(RunWithPlugins::class).configureEach {
      val cfg = readConfig()
      val enabled = cfg.taskOverrides[name]?.get(entry.name)
        ?: cfg.defaults[entry.name]
        ?: false
      if (enabled) {
        pluginJars.from(c)
      }
    }
  }
}

@ConfigSerializable
class Config {
  var defaults: MutableMap<String, Boolean> = mutableMapOf()
  var taskOverrides: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf(
    "someTaskName" to mutableMapOf("somePlugin" to false)
  )
}

@Synchronized
fun readConfig(): Config {
  val loader = YamlConfigurationLoader.builder()
    .file(file("run-plugins.yml"))
    .nodeStyle(NodeStyle.BLOCK)
    .defaultOptions {
      it.header("""
        Enable and disable optional plugins for run tasks in this project
      """.trimIndent())
    }
    .build()
  val n = loader.load()
  val c = n.get(Config::class.java) as Config
  var write = false
  for (e in ext.gradleDependencyBased.get()) {
    if (!c.defaults.containsKey(e.name)) {
      write = true
      c.defaults[e.name] = e.defaultEnabled
    }
  }
  if (write) {
    n.set(c)
    loader.save(n)
  }
  return c
}
