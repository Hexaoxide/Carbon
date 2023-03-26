/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import net.minecrell.pluginyml.InvalidPluginDescriptionException
import net.minecrell.pluginyml.PlatformPlugin
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

class PaperPlugin : PlatformPlugin<BukkitPluginDescription>("Paper", "paper-plugin.yml") {

  companion object {
    @JvmStatic private val VALID_NAME = Regex("^[A-Za-z0-9 _.-]+$")
  }

  override fun createExtension(project: Project) = BukkitPluginDescription(project)

  override fun setDefaults(project: Project, description: BukkitPluginDescription) {
    description.name = description.name ?: project.name
    description.version = description.version ?: project.version.toString()
    description.description = description.description ?: project.description
    description.website = description.website ?: project.findProperty("url")?.toString()
    description.author = description.author ?: project.findProperty("author")?.toString()
  }

  override fun setLibraries(libraries: ResolvedComponentResult?, description: BukkitPluginDescription) {
    val resolved = libraries?.let {
      it.dependencies.map { d -> (d as? ResolvedDependencyResult)?.selected?.moduleVersion?.toString() ?: error("No moduleVersion for $d") }
    }
    description.libraries = ((description.libraries ?: listOf()) + (resolved ?: listOf())).distinct()
  }

  override fun validate(description: BukkitPluginDescription) {
    val name = description.name ?: throw InvalidPluginDescriptionException("Plugin name is not set")
    if (!VALID_NAME.matches(name)) throw InvalidPluginDescriptionException("Invalid plugin name: should match $VALID_NAME")

    if (description.version.isNullOrEmpty()) throw InvalidPluginDescriptionException("Plugin version is not set")

    val main = description.main ?: throw InvalidPluginDescriptionException("Main class is not defined")
    if (main.isEmpty()) throw InvalidPluginDescriptionException("Main class cannot be empty")
    if (main.startsWith("org.bukkit.")) throw InvalidPluginDescriptionException("Main may not be within the org.bukkit namespace")

    for (command in description.commands) {
      if (command.name.contains(':')) throw InvalidPluginDescriptionException("Command '${command.name}' cannot contain ':'")
      command.aliases?.forEach { alias ->
        if (alias.contains(':')) throw InvalidPluginDescriptionException("Alias '$alias' of '${command.name}' cannot contain ':'")
      }
    }

    if (description.provides?.all(VALID_NAME::matches) == false) {
      throw InvalidPluginDescriptionException("Invalid plugin provides name: all should match $VALID_NAME")
    }
  }

}
