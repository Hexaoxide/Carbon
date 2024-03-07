import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.kyori.indra.git.IndraGitExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.eclipse.jgit.lib.Repository
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.filter
import org.gradle.kotlin.dsl.the
import org.gradle.language.jvm.tasks.ProcessResources
import xyz.jpenilla.gremlin.gradle.ShadowGremlin

fun ProcessResources.replace(
  pattern: String,
  tokens: Map<String, Any?>
) {
  inputs.properties(tokens)
  filesMatching(pattern) {
    filter<ReplaceTokens>(
      "beginToken" to "\${",
      "endToken" to "}",
      "tokens" to tokens
    )
  }
}

val Project.releaseNotes: Provider<String>
  get() = providers.environmentVariable("RELEASE_NOTES")

/**
 * Relocate a package into the `carbonchat.libs.` namespace.
 */
fun Task.relocateDependency(pkg: String) {
  ShadowGremlin.relocate(this, pkg, "carbonchat.libs.$pkg")
}

fun Task.standardRuntimeRelocations() {
  relocateDependency("com.github.benmanes")
  // relocateDependency("com.github.luben.zstd") // natives don't like relocation - hopefully nothing breaks :)
  relocateDependency("com.google.protobuf")
  relocateDependency("com.mysql.cj")
  relocateDependency("com.mysql.jdbc")
  relocateDependency("com.rabbitmq")
  relocateDependency("io.nats")
  relocateDependency("net.i2p.crypto")
  relocateDependency("org.apache.commons.pool2")
  relocateDependency("org.jdbi")
  relocateDependency("org.mariadb.jdbc")
  relocateDependency("org.postgresql")
  relocateDependency("redis.clients.jedis")
  relocateDependency("org.flywaydb")
  relocateDependency("com.fasterxml")
  relocateDependency("org.h2")
}

/**
 * Relocates dependencies which we bundle and relocate on all platforms.
 */
fun Task.standardRelocations() {
  relocateDependency("org.bstats")
  relocateDependency("net.kyori.adventure.serializer.configurate4")
  relocateDependency("com.seiama.event")
  relocateDependency("net.kyori.moonshine")
  relocateDependency("com.seiama.registry")
  relocateDependency("org.spongepowered.configurate")
  relocateDependency("com.google.thirdparty.publicsuffix")
  relocateDependency("com.zaxxer.hikari")
  relocateDependency("ninja.egg82.messenger")
  relocateDependency("org.antlr")
  relocateDependency("com.electronwill")
  relocateDependency("xyz.jpenilla.gremlin")
}

fun Task.relocateCloud() {
  relocateDependency("cloud.commandframework")
}

fun Task.relocateGuice() {
  relocateDependency("com.google.inject")
  relocateDependency("org.aopalliance")
  relocateDependency("jakarta.inject")
}

fun ShadowJar.configureShadowJar() {
  //minimize()
  standardRelocations()
  dependencies {
    // not needed or provided by platform at runtime
    exclude(dependency("com.google.code.findbugs:jsr305"))
    exclude(dependency("com.google.errorprone:error_prone_annotations"))
    exclude { it.moduleGroup == "com.google.guava" }
    exclude(dependency("com.google.j2objc:j2objc-annotations"))
    exclude(dependency("io.netty:netty-all"))
    exclude(dependency("io.netty:netty-buffer"))
    exclude(dependency("it.unimi.dsi:fastutil"))
    exclude(dependency("org.checkerframework:checker-qual"))
    exclude(dependency("org.slf4j:slf4j-api"))
  }
}

fun Project.lastCommitHash(): String =
  the<IndraGitExtension>().commit()?.name?.substring(0, 7)
    ?: error("Could not determine commit hash")

fun Project.decorateVersion() {
  val versionString = version as String
  version = if (versionString.endsWith("-SNAPSHOT")) {
    "$versionString+${lastCommitHash()}"
  } else {
    versionString
  }
}

fun Project.currentBranch(): String {
  System.getenv("GITHUB_HEAD_REF")?.takeIf { it.isNotEmpty() }
    ?.let { return it }
  System.getenv("GITHUB_REF")?.takeIf { it.isNotEmpty() }
    ?.let { return it.replaceFirst("refs/heads/", "") }

  val indraGit = the<IndraGitExtension>().takeIf { it.isPresent }

  val ref = indraGit?.git()?.repository?.exactRef("HEAD")?.target
    ?: return "detached-head"

  return Repository.shortenRefName(ref.name)
}

val Project.libs: LibrariesForLibs
  get() = the()
