import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider

abstract class ConfigurablePluginsExt {
  data class DepPlugin(
    val dep: Provider<MinimalExternalModuleDependency>,
    val op: Action<in ExternalModuleDependency>?,
    val defaultEnabled: Boolean = false,
    val name: String = dep.get().name
  )

  abstract val gradleDependencyBased: ListProperty<DepPlugin>

  fun dependency(lib: Provider<MinimalExternalModuleDependency>, op: Action<in ExternalModuleDependency>? = null) {
    gradleDependencyBased.add(DepPlugin(lib, op))
  }

  fun dependency(lib: Provider<MinimalExternalModuleDependency>, defaultEnabled: Boolean, op: Action<in ExternalModuleDependency>? = null) {
    gradleDependencyBased.add(DepPlugin(lib, op, defaultEnabled))
  }
}
