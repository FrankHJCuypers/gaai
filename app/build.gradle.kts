@file:Suppress("LongLine", "LongLine")

import org.jetbrains.dokka.DokkaConfiguration.Visibility
import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.room)
  alias(libs.plugins.devtools.ksp)
  id("com.gladed.androidgitversion") version "0.4.14"
  base
}

androidGitVersion {
  codeFormat = "MNNPPP"
  format = "%tag%%-count%%-gcommit%%-branch%%-dirty%"
  untrackedIsDirty = true
}

// Read keystore properties from keystore.properties file, in the rootProject folder
// For security reasons keystore.properties is NOT in git.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
  signingConfigs {
    create("release") {
      storeFile = file(keystoreProperties["storeFile"] as String)
      keyPassword = keystoreProperties["keyPassword"] as String
      storePassword = keystoreProperties["storePassword"] as String
      keyAlias = keystoreProperties["keyAlias"] as String
    }
  }
  
  namespace = "be.cuypers_ghys.gaai"
  compileSdk = 35

  defaultConfig {
    base {
      versionCode = androidGitVersion.code()
      versionName = androidGitVersion.name()
      archivesName = "Gaai-v$versionCode-$versionName"
    }

    applicationId = "be.cuypers_ghys.gaai"
    minSdk = 26
    targetSdk = 35

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.1"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  // Needed for JUnit5 to work
  @Suppress("UnstableApiUsage")
  testOptions {
    unitTests.all { it.useJUnitPlatform() }
  }

  room {
    schemaDirectory("$projectDir/schemas")
  }
}

dependencies {

  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.nordic.kotlin.ble.profile)
  implementation(libs.nordic.kotlin.ble.scanner)
  implementation(libs.nordic.kotlin.ble.client)
  implementation(libs.cbor)
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.core.ktx)

  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.lifecycle.runtime.compose.android)
  annotationProcessor(libs.androidx.room.compiler)
  // To use Kotlin annotation processing tool (kapt)
  //kapt(libs.androidx.room.compiler)
  // To use Kotlin Symbol Processing (KSP)
  ksp(libs.androidx.room.compiler)
  implementation(libs.androidx.room.ktx)

  // Include a slf4j implementation. libs.nordic.kotlin.ble uses it and without an implementation, no logging.
  // Alternative is to include
  // nordic-logger = { group = "no.nordicsemi.android.common", name = "logger", version.ref = "nordic-common" }?
  // See Kotlin-BLE-Library, app_client packages, gradle script.
  implementation(libs.slf4j.api)
  implementation(libs.slf4j.simple)

//    implementation(libs.androidx.room.common)

  implementation(libs.androidx.navigation.runtime.ktx)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.versioncompare)

  testImplementation(libs.junit)
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  implementation(kotlin("script-runtime"))
  testImplementation(libs.mockk)

  runtimeOnly(libs.android.documentation.plugin)
}


tasks.dokkaHtml {
  outputDirectory.set(layout.buildDirectory.dir("documentation/html"))
  dokkaSourceSets {
    configureEach {
      documentedVisibilities.set(
        setOf(
          Visibility.PUBLIC,
          Visibility.PROTECTED,
          Visibility.PACKAGE,
          Visibility.PRIVATE,
          Visibility.INTERNAL,
        )
      )
    }
  }
}