// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.jetbrains.kotlin.android) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.room) apply false
  alias(libs.plugins.devtools.ksp) apply false
  // Generates HTML documentation
  id("org.jetbrains.dokka") version "2.0.0"
}

subprojects {
  apply(plugin = "org.jetbrains.dokka")
}

