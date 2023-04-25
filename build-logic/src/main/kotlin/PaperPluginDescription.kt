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

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.lang.Closure
import net.minecrell.pluginyml.PluginDescription
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

class PaperPluginDescription(project: Project) : PluginDescription {

  @Input
  @Optional
  @JsonProperty("api-version")
  var apiVersion: String? = null
  @Input
  var name: String? = null
  @Input
  var version: String? = null
  @Input
  var main: String? = null
  @Input
  var loader: String? = null
  @Input
  @Optional
  var description: String? = null
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
  var prefix: String? = null
  @Input
  @Optional
  @JsonProperty("default-permission")
  var defaultPermission: BukkitPluginDescription.Permission.Default? = null
  @Input @Optional @JsonProperty("folia-supported") var foliaSupported: Boolean? = null

  @Nested
  val dependencies: MutableList<Dependency> = mutableListOf()
  @Nested
  @JsonProperty("load-after")
  val loadAfter: MutableList<LoadInfo> = mutableListOf()
  @Nested
  @JsonProperty("load-before")
  val loadBefore: MutableList<LoadInfo> = mutableListOf()

  data class Dependency(@Input val name: String, @Input val required: Boolean = true, @Input val bootstrap: Boolean = false)

  data class LoadInfo(@Input val name: String, @Input val bootstrap: Boolean = false)

  @Nested
  val permissions: NamedDomainObjectContainer<BukkitPluginDescription.Permission> = project.container(BukkitPluginDescription.Permission::class.java)

  // For Groovy DSL
  fun permissions(closure: Closure<Unit>) = permissions.configure(closure)

}
