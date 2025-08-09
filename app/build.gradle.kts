import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.io.FileInputStream
import java.nio.file.Paths
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

// Read keystore properties from gaai-release-keystore.properties file, in the $user.home\.android folder
// For security reasons gaai-release-keystore.properties and gaai-keystore.properties are NOT in git.
val keystoreProperties = Properties()
val keystorePropertiesFile = file(Paths.get(System.getProperty("user.home")).resolve(".android\\gaai-release-keystore.properties"))
if (keystorePropertiesFile.exists()) {
  keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
  signingConfigs {
    create("release") {
      // If the gaai-release-keystore.properties file exists, we use it (we are running locally)
      if (keystorePropertiesFile.exists()) {
        storeFile = file(keystoreProperties["storeFile"] as String)
        storePassword = keystoreProperties["storePassword"] as String
        keyAlias = keystoreProperties["keyAlias"] as String
        keyPassword = keystoreProperties["keyPassword"] as String
      }
      else {
        // If the gaai-release-keystore.properties file does not exist, we use Github secrets.
        storeFile = rootProject.file(System.getenv("SIGNING_STORE_FILE") as String)
        storePassword = System.getenv("SIGNING_STORE_PASSWORD") as String
        keyAlias = System.getenv("SIGNING_KEY_ALIAS") as String
        keyPassword = System.getenv("SIGNING_KEY_PASSWORD") as String
      }
    }
  }

  namespace = "be.cuypers_ghys.gaai"
  compileSdk = 36

  defaultConfig {
    versionCode = androidGitVersion.code()
    versionName = androidGitVersion.name()
    base.archivesName = "Gaai-v$versionCode-$versionName"

    applicationId = "be.cuypers_ghys.gaai"
    minSdk = 26
    targetSdk = 36

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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  kotlin {
    jvmToolchain(21)
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
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.nordic.kotlin.ble.profile)
  implementation(libs.nordic.kotlin.ble.scanner)
  implementation(libs.nordic.kotlin.ble.client)
  implementation(libs.cbor)
  implementation(libs.accompanist.permissions)
  implementation(libs.accompanist.drawablepainter)
  implementation(libs.androidx.core.ktx)

  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.lifecycle.runtime.compose.android)
  implementation(libs.androidx.junit.ktx)
  implementation(libs.androidx.monitor)
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

dokka {
  moduleName.set("Gaai")

  dokkaPublications.html {
    outputDirectory.set(layout.buildDirectory.dir("documentation/html"))
    suppressInheritedMembers.set(true)
    failOnWarning.set(false)
  }

  dokkaSourceSets.main {
    documentedVisibilities.set(
      setOf(
        VisibilityModifier.Public,
        VisibilityModifier.Protected,
        VisibilityModifier.Package,
        VisibilityModifier.Private,
        VisibilityModifier.Internal
      )
    )
    sourceLink {
      localDirectory.set(file("src/main/java"))
      remoteUrl("https://example.com/src")
      remoteLineSuffix.set("#L")
    }
  }

  pluginsConfiguration.html {
    footerMessage.set("(c) Frank HJ Cuypers")
  }
}
