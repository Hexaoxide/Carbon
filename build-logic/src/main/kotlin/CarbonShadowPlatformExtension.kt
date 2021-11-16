import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class CarbonShadowPlatformExtension(project: Project) {
  val relocateGuice: Property<Boolean> = project.objects.property<Boolean>().convention(false)
  val relocateCloud: Property<Boolean> = project.objects.property<Boolean>().convention(true)
}
