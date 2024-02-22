import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

val ktfmtVersion = "0.47"

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.spotless)
  alias(libs.plugins.versions)
  alias(libs.plugins.version.catalog.update)
  alias(libs.plugins.dokka)
}

repositories { mavenCentral() }

kotlin { jvmToolchain(21) }

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "com.diffplug.spotless")
  apply(plugin = "org.jetbrains.dokka")

  repositories { mavenCentral() }

  tasks.withType<Test> { useJUnitPlatform() }

  configure<SpotlessExtension> {
    kotlin { ktfmt(ktfmtVersion) }
    kotlinGradle { ktfmt(ktfmtVersion) }
  }

  kotlin { jvmToolchain(21) }
}

configure<SpotlessExtension> {
  kotlin { ktfmt(ktfmtVersion) }
  kotlinGradle { ktfmt(ktfmtVersion) }
}

fun isUnstableRelease(version: String): Boolean {
  return version.contains("beta|rc|alpha".toRegex(RegexOption.IGNORE_CASE))
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf { isUnstableRelease(candidate.version) && !isUnstableRelease(currentVersion) }
}
