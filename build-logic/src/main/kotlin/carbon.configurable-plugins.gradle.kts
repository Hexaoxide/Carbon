import xyz.jpenilla.runtask.task.RunWithPlugins
import java.util.Properties

// todo use something better than properties for this (maybe configurate?)

val ext = extensions.create("configurablePlugins", ConfigurablePluginsExt::class.java)

val f = file("run-plugins.properties")
fun props(): Properties {
  val props = Properties()
  if (f.isFile) {
    f.bufferedReader().use { r -> props.load(r) }
  }
  return props
}

tasks.withType(RunWithPlugins::class).configureEach {
  doFirst {
    val props = props()
    val text = f.takeIf { it.isFile }?.readText() ?: "\n"
    val rest = text.substringAfter("# [taskName].[pluginName]=false")
    fun prop(name: String, def: Any): String = "$name=${props[name] ?: def}"

    val defProps = """
    # Enable or disable plugins in run tasks

    # applies to all run tasks in the project
    ${ext.gradleDependencyBased.get().joinToString("\n") { prop(it.name, false) }}

    # applies to only [taskName] (always has priority)
    # [taskName].[pluginName]=false""".trimIndent() + rest

    f.writeText(defProps)
  }
}

afterEvaluate {
  ext.gradleDependencyBased.get().forEach { entry ->
    val c = configurations.register(entry.name + "Plugin") {
      isTransitive = false
    }
    dependencies {
      c.name(entry.dep) { entry.op?.execute(this) }
    }
    tasks.withType(RunWithPlugins::class).configureEach {
      val props = props()
      val prop = props["$name.${entry.name}"]
        ?: props[entry.name]
      if (prop.toString().toBoolean()) {
        pluginJars.from(c)
      }
    }
  }
}
