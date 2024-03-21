package resourcegenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import javax.inject.Inject

abstract class ResourceGeneratorPlugin : Plugin<Project> {
  @get:Inject
  abstract val objects: ObjectFactory

  override fun apply(target: Project) {
    target.plugins.withType(JavaBasePlugin::class) {
      val sourceSets = target.extensions.getByType(SourceSetContainer::class)
      sourceSets.all {
        val setName = name

        val genExt = objects.newInstance(ResourceGeneratorExtension::class, target)
        extensions.add("resourceGenerator", genExt)

        val task = target.tasks.register("${setName}GenerateResources", GenerateResources::class) {
          outputDir.set(target.layout.buildDirectory.dir("generated/generateResources/$setName"))
          generators.set(genExt.generators)
        }

        resources.srcDir(task.flatMap { it.outputDir })
      }
    }
  }
}
