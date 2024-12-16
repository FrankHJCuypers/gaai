# Development

This app is developed in [Kotlin](https://developer.android.com/kotlin) with
[Android Studio](https://developer.android.com/studio) using

- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- [Kotlin coroutines](https://developer.android.com/kotlin/coroutines)
- [Kotlin BLE Library for Android](https://github.com/NordicSemiconductor/Kotlin-BLE-Library)

## Environment setup

If you want to compile *Gaai* you must install Git and Android Studio.
Follow these steps:

1. Install the 64-bit version of [Git](https://git-scm.com/downloads/win).
2. Install [Android Studio](https://developer.android.com/studio/install).
3. In Android Studio select the menu item *File* -> *Settings* -> *Version Control* -> *Git* and make sure that the
   *Path to Git executable* points to the git.exe that you just installed.
4. [Clone GitHub Gaai project in Android Studio](https://www.geeksforgeeks.org/how-to-clone-android-project-from-github-in-android-studio/).
   The required URL can be found by clicking on the ![Green Code>](docs/images/GreenCode.png)
   button at the top of this page.
   You might need a GitHub login to be able to clone a repo.
   If you plan on contributing to *Gaai*, you better first fork the repo in GitHub and use the URL of your fork in
   Android Studio.
   Under *Directory* choose a directory where you want the project to be stored.
   Press the *Clone* button.
5. In Android Studio [Connect to your device using Wi-Fi](https://developer.android.com/studio/run/device#wireless).
6. [Build and run Gaai](https://developer.android.com/studio/run)
   This might take some while the first time because Android Studio will also download all dependencies.

## Versioning

The *Gaai* project uses [Semantic Versioning]( https://semver.org/).
A version number has the format x.y.z, possibly with some extensions.
x, y and z are incremented as follows:

- x (MAJOR) version when incompatible API changes are made
- y (MINOR) version when functionality is added in a backward compatible manner
- z (PATCH) version when backward compatible bug fixes are made

Not every commit is given its own version number.
Only commits that correspond with released versions are given their own version number.
All other commits are candidates for the next release.
As soon as a commit is officially released for the x.y.z release, the following steps are taken:

1. A number x'.y'.z' for the next release is decided on, based on the [Semantic Versioning]( https://semver.org/) rules.
2. The commit chosen for the official release of x.y.z is marked with the tag *v*x.y.z, possibly with an extension.
   Some of the extensions that can be used are `-experimental`, `-alpha`, `-beta`, etc...
   See [Software release life cycle](https://en.wikipedia.org/wiki/Software_release_life_cycle).
   *Gaai* will use `-alpha` until the achieved testing level warrants an `-beta` state.
3. A release is created in GitHub with the APK files of the commit tagged with *v*x.y.z.
4. The next commit is marked with the tag x'.y'.z', marking it as the first candidate for release *v*x'.y'.z'.
5. The Gradle build generates a unique Android
   [versionName](https://developer.android.com/studio/publish/versioning#versioningsettings)
   for each commit in much the same way as [git describe](https://git-scm.com/docs/git-describe),
   but using the [gradle-android-git-version](https://github.com/gladed/gradle-android-git-version) plugin.
   The result for a commit equal or after the last x.y.z tag is:
   `<x.y.z>-<c>-g<1234567>[-<branch name>][-dirty]`
   with
    - `<x.y.z>`: The most recent tag of the form x.y.z.
    - `<c>`: the number of commits after the tag. Absent if 0.
    - `<1234567>`: the first 7 digits of the git commit hash.
    - `<branch name>`: name of the branch if different from *master*.
    - `-dirty`: is added if the workspace still contains local modifications (i.e. uncommitted files).

Android not only requires a versionName, but also a
[versionCode](https://developer.android.com/studio/publish/versioning#versioningsettings).
The versionCode is not meant to be shown to the user.
It is an internal number that Android uses to protect against downgrading.
Android does not allow to install an APK with a lower versionCode than the one already installed.
Using the [gradle-android-git-version](https://github.com/gladed/gradle-android-git-version) plugin, Gradle
generates a versionCode equal to 100.000 * z + 1.000 * y + z.

## Git commits

The *Gaai* project uses the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specifications
for writing commit messages.

## Testing

### Local tests

Local (JUnit) tests run directly on your development PC.
No emulator or mobile phone running Android is required.

Run the local tests in Android Studio as follows:

- In the *Android* view, select the *app -> kotlin+java -> be.cuypers_ghys.gaai (test)* node.
- In the pop-up menu, select *Run 'Tests in 'be...'*.
  The tests run.
- Verify that there are no errors.
  Fix errors if any.

Local tests can also be run using Gradle in a shell (e.g. PowerShell in Android Studio terminal, or git bash),
see [Test from the command line](https://developer.android.com/studio/test/command-line):
`./gradlew testReleaseUnitTest`

### Instrumented tests

**Note** that there are no instrumented tests yet.

Instrumented (JUnit) tests run on an Android emulator or mobile phone.

Run the Instrumented tests in Android Studio as follows:

- In the *Android* view, select the *app -> kotlin+java -> be.cuypers_ghys.gaai (androidTest)* node.
- In the pop-up menu, select *Run 'Tests in 'be...'".
  The tests run.
- Verify that there are no errors.
  Fix errors if any.

Instrumented tests can also be run using Gradle in a shell (e.g. PowerShell in Android Studio terminal, or git bash),
see [Test from the command line](https://developer.android.com/studio/test/command-line):
`./gradlew connectedAndroidTest`

#### Run an app on an emulator

Android Studio allows to run/debug an app on a built-in emulator.
For *Gaai*, this option is very limited because the emulator does not support BLE.

#### Run an app on a hardware device

Android Studio allows to run/debug an app on a hardware device.
See [Run apps on a hardware device](https://developer.android.com/studio/run/device) on how to do that.
Using WiFi worked very fine on a Google Pixel 6 Pro.
Basically you will need to enable *Developer options* and *Wireless debugging* on your phone and pair it with Android
Studio.

### Manual tests on a hardware device

Not all functionality is covered by local and instrumented tests (yet).
Some manual testing is required.
Install the app either using Android Studio or by manually installing the APK to test.

1. In Android Studio:
    - select the hardware device on which to install
    - select the default *app* configuration.
    - Run the *app*.
2. Manual installation:
    - Make the APK available on your hardware device (copy it using bluetooth or WiFi, use Google Drive,use Github link,
      ...)
    - Install/Run the APK by clicking on it.

Now manually test all functionality that is described in the [README](README.md#getting-started).

### Developer Documentation generation

The kotlin code is documented with [KDoc](https://kotlinlang.org/docs/kotlin-doc.html).
[Dokka](https://kotlinlang.org/docs/dokka-introduction.html) can generated HTML documentation from it.
In order to do that, run the following command in a shell (e.g. PowerShell in Android Studio terminal, or git bash):
`./gradlew dokkaHtml`. The resulting html files are generated in the `app\build\documentation\html` subdirectory.
Open the `index.html` file to start reading the documentation.

Note that this documentation targets developers, not the users of *Gaai*.

## Make a release

Once a commit on the master branch is selected to make a new release in GutHub, proceed as follows:

1. Perform the tests from [Testing](#testing) and verify that they are successful.
2. In Android Studio, select *Code -> Inspect Code* from the menu and inspect the results.
   Some of the reported issues can be improved, but several of them are not real issues.
   I tried to suppress these with `@Suppress` but that does not always work.
3. Perform the [Developer Documentation generation](#developer-documentation-generation) and verify the result.
4. Build the APK file by running `./gradlew build` in a shell (e.g. PowerShell in Android Studio terminal, or git bash).
   This will also run the tests from point 1 above.
   The resulting APK files are generated in the `app\build\outputs\apk` subdirectory.
   For the GitHub release, use the one in the `app\build\outputs\apk\release` subdirectory.  
   The name of the apk includes the version number of the release candidate,
   based on the most recent tag of the form x.y.z.
   See [Versioning](#versioning).
   The name of the version to release will then be vx.y.z.
5. Push the master branch to GitHub.
6. Create a new release in GitHub
    - In the *Code* screen, under *Releases*,
      select [Create a new release](https://github.com/FrankHJCuypers/Gaai/releases/new).
    - Choose a *Tag*: create a new tag to be applied on the most recent commit, starting with a 'v': vx.y.z.
      For a version 0.0.2 that becomes v0.0.2.
      Add an extension like `-alpha` if needed.
    - Choose a *Target*: master
    - Choose a *Previous Tag*: choose the tag of the previous release.
      This is used to generate a changelog.
    - Press *Generate release notes* and accept the full changelog that is proposed.
      Use it as the first line of the description of the release.
      Add some more detail for relevant commits;
      see [release v.0.0.2-0alpha](https://github.com/FrankHJCuypers/Gaai/releases/tag/v0.0.2-alpha) as an example.
    - Choose a *Release Title*: "Release \<tagname\>" with \<tagname\> the chosen tag.
    - Select *Set as latest release*
    - Select *Create a discussion for this release*.
    - Drag the APK file from the `app\build\outputs\apk\release` subdirectory to the new release.
    - Press *Publish release*.
    - Fetch the master branch from GitHub in order to import the new tags.
    - Decide on the next release number x'.y'.z'.
      Apply a tag with that name on the next commit on the master branch.

## APK signing

Release APK's must be signed.
See [Sign your app](https://developer.android.com/studio/publish/app-signing.html#kts).
Android refuses to install unsigned release APK's.
New versions of an already installed app must also be signed with the same key,
otherwise the new version is refused by Android.

Gradle automatically signs release builds when `./gradlew build` is ran.
That is defined in the `buildTypes` block of the app module's `build.gradle.kts` file.
The release block uses a `signingConfig` with the name "release",
which is itself defined in the `signingConfigs` of the app module's `build.gradle.kts` file.
The values `storeFile`, `storePassword`, `keyAlias` and `keyPassword` are not defined in the `build.gradle.kts` file.
`build.gradle.kts` must be pushed to the public git repo which would make the keys and passwords public.
Therefore, the 4 values are defined in a `keystore.properties` file,
located in the root of the project, but not included in Git.
`storeFile` defines the location of the
[keystore](https://cr.openjdk.org/~alanb/api/java.base/java/security/KeyStore.html) file.
The keystore file is of type "JKS".
It contains a 256 bits Elliptic Curve key on the NIST P-256 elliptic curve.
This key is used with the *SHA-256 with ECDSA* signature algorithm to sign the APK.
All of that is done automatically by gradle.

The keystore file can be manipulated using the command line tool
[keytool](https://cr.openjdk.org/~jjg/8261930/docs/specs/man/keytool.html)
or with the [KeyStore Explorer](https://keystore-explorer.org/).
The latter has a GUI.

The Keystore, signing key and passwords are not public.
They are currently managed by the project maintainer,
who is also responsible for backups.

## Gradlew commands

- `./gradlew dokkaHtml` creates Html based documentation from the Kdoc documentation used for documenting the source
  code.
  The documentation is generated in the `app\build\documentation\html` directory
- `./gradlew testReleaseUnitTest` runs the local (JUnit) tests for the release build.
  The Junit xml files are generated in the `app\build\test-results\testReleaseUnitTest` directory.
  Html test result files are generated in the `app\build\reports\tests\testReleaseUnitTest` directory.
- `./gradlew testDebugUnitTest` runs the local (JUnit) tests for the debug build.
  The Junit xml files are generated in the `app\build\test-results\testDebugUnitTest` directory.
  Html test result files are generated in the `app\build\reports\tests\testDebugUnitTest` directory.
- `./gradlew test` runs both the above.
- `./gradlew build` assembles and tests the project.
  The release apk files are generated in the `app\build\outputs\apk\release` directory.

