// Top-level build file where you can add configuration options common to all subprojects/modules.

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.jetbrains.kotlin.android) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.room) apply false
  alias(libs.plugins.devtools.ksp) apply false
  // Generates HTML documentation
  id("org.jetbrains.dokka") version "2.2.0"
  id("com.google.gms.google-services") version "4.5.0" apply false
  id("com.google.firebase.crashlytics") version "3.0.7" apply false
}

subprojects {
  apply(plugin = "org.jetbrains.dokka")
}

