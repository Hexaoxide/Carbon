import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class CarbonShadowPlatformExtension @Inject constructor(objects: ObjectFactory) {
  val relocateGuice: Property<Boolean> = objects.property<Boolean>().convention(false)
  val relocateCloud: Property<Boolean> = objects.property<Boolean>().convention(true)
}
