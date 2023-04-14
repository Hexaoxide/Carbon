import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.kyori.indra.git.IndraGitExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the

val Project.releaseNotes: Provider<String>
  get() = providers.environmentVariable("RELEASE_NOTES")

/**
 * Relocate a package into the `net.draycia.carbon.libs` namespace.
 */
fun ShadowJar.relocateDependency(pkg: String) {
  relocate(pkg, "net.draycia.carbon.libs.$pkg")
}

/**
 * Relocates dependencies which we bundle and relocate on all platforms.
 */
fun ShadowJar.standardRelocations() {
  relocateDependency("org.bstats")
  relocateDependency("net.kyori.adventure.serializer.configurate4")
  relocateDependency("net.kyori.event")
  relocateDependency("net.kyori.moonshine")
  relocateDependency("net.kyori.registry")
  relocateDependency("org.spongepowered.configurate")
  relocateDependency("com.typesafe.config")
  relocateDependency("com.google.thirdparty.publicsuffix")
  relocateDependency("org.jdbi")
  relocateDependency("com.github.benmanes")
  relocateDependency("org.mariadb.jdbc")
  relocateDependency("com.zaxxer.hikari")
  relocateDependency("org.postgresql")
  relocateDependency("redis.clients.jedis")
  relocateDependency("ninja.egg82.messenger")
  relocateDependency("org.antlr")
  relocateDependency("io.nats")
  relocateDependency("com.rabbitmq")
  relocateDependency("com.electronwill")
  relocateDependency("net.i2p.crypto")
  relocateDependency("org.apache.commons.pool2")
  relocateDependency("org.flywaydb")
}

fun ShadowJar.relocateCloud() {
  relocateDependency("cloud.commandframework")
}

fun ShadowJar.relocateGuice() {
  relocateDependency("com.google.inject")
  relocateDependency("org.aopalliance")
  relocateDependency("javax.inject")
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

fun Project.latestGitHash(): String? =
  the<IndraGitExtension>().commit()?.name?.substring(0, 7)

val Project.libs: LibrariesForLibs
  get() = the()
