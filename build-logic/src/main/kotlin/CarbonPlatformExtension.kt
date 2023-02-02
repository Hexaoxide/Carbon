import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class CarbonPlatformExtension @Inject constructor(objects: ObjectFactory) {
  val jarTask: Property<AbstractArchiveTask> = objects.property()
}
