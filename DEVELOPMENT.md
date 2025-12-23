[![Firebase](https://readmebadge.vercel.app/badges/firebase.svg)](https://console.firebase.google.com/project/gaai-3e164/overview)


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
   *Gaai* will use `-beta` until the achieved testing level warrants an `stable state.
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
generates a versionCode equal to 100.000 * x + 1.000 * y + z.

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
`./gradlew testReleaseUnitTest`, `./gradlew testUnitTest` or `./gradlew testDebugUnitTest`

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

## Code coverage

### Code coverage of Local tests

The [Local (JUnit) tests](#local-tests) can also produce code coverage results.
Run the local tests in Android Studio as follows:

- In the *Android* view, select the *app -> kotlin+java -> be.cuypers_ghys.gaai (test)* node.
- In the pop-up menu, select *Run 'Tests in 'be...' with Coverage*.
  The tests run.
- The Coverage screen shows a summary of the coverage results. 
  From there you can drill down to source code level to see which code is covered and which not. 
  Add Junit tests to increase coverage to 100% is possible.

Code coverage of local tests can also be generated using Gradle in a shell 
(e.g. PowerShell in Android Studio terminal, or git bash),
see [Test from the command line](https://developer.android.com/studio/test/command-line):
`./gradlew testDebugUnitTest` followed by `./gradlew createDebugCoverageReport`.
Note that `./gradlew testReleaseUnitTest` does not create Code coverage information.

## Debugging

### Logging

Gaai uses android.util.Log for logging.
The logging results are available in real time in [Logcat](https://developer.android.com/studio/debug/logcat).
The use of the logging levels is loosely based on 
[When to use the different log levels](https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels.)

None if the code in the be.cuypers_ghys.gaai.util package has logging statements.
This code is fairly small and simple and fully tested with JUnit.
Adding logging statements seems overkill.

## Developer Documentation generation

The kotlin code is documented with [KDoc](https://kotlinlang.org/docs/kotlin-doc.html).
[Dokka](https://kotlinlang.org/docs/dokka-introduction.html) can generate HTML documentation from it.
In order to do that, run the following command in a shell (e.g. PowerShell in Android Studio terminal, or git bash):
`./gradlew dokkaGenerate`. The resulting html files are generated in the `app\build\documentation\html` subdirectory.
Open the `index.html` file to start reading the documentation.

Note that this documentation targets developers, not the users of *Gaai*.

## Make a release
This procedure only works if you have the APK signing keys setup, so that `./gradlew build` can run.
See [Gradlew commands](#gradlew-commands).

Once a commit on the master branch is selected to make a new release in GutHub, proceed as follows:

1. Perform the tests from [Testing](#testing) and verify that they are successful.
2. In Android Studio, select *Code -> Inspect Code* from the menu and inspect the results.
   Some of the reported issues can be improved, but several of them are not real issues.
   I tried to suppress these with `@Suppress` but that does not always work.
3. Perform the [Developer Documentation generation](#developer-documentation-generation) and verify the result.
4. Build the APK file by running `./gradlew build` in a shell (e.g. PowerShell in Android Studio terminal, or git bash).
   This will also run the tests from point 1 above.
   The resulting APK files are generated in the `app\build\outputs\apk` subdirectory.
   For the GitHub release, use the one in the `app\build\outputs\apk\release` and `app\build\outputs\apk\firebase`
   subdirectories.
   Alternatively use the one from the latest build artifact generated by the [Android CI](#android-ci) action.  
   The name of the APK file includes the version number of the release candidate,
   based on the most recent tag of the form x.y.z.
   See [Versioning](#versioning).
   The name of the version to release will then be vx.y.z.
5. Build the AAB file by running `./gradlew bundle` in a shell (e.g. PowerShell in Android Studio terminal, or git bash).
   The resulting AAB files are generated in the `app\build\outputs\bundle` subdirectory.
   For the GitHub release, use the one in the `app\build\outputs\bundle\release` and `app\build\outputs\bundle\firebase`
   subdirectories.
   Alternatively use the one from the latest build artifact generated by the [Android CI](#android-ci) action.  
   The name of the AAB file also includes the version number of the release candidate.
6. Push the master branch to GitHub.
7. Create a new release in GitHub
    - In the *Code* screen, under *Releases*,
      select [Create a new release](https://github.com/FrankHJCuypers/gaai/releases/new).
    - Choose a *Tag*: create a new tag to be applied on the most recent commit, starting with a 'v': vx.y.z.
      For a version 0.0.2 that becomes v0.0.2.
      Add an extension like `-alpha` if needed.
    - Choose a *Target*: master
    - Choose a *Previous Tag*: choose the tag of the previous release.
      This is used to generate a changelog.
    - Press *Generate release notes* and accept the full changelog that is proposed.
      Use it as the first line of the description of the release.
      Add some more detail for relevant commits;
      see [release v0.1.0-beta](https://github.com/FrankHJCuypers/gaai/releases/tag/v0.1.0-beta) as an example.
    - Choose a *Release Title*: "Release \<tagname\>" with \<tagname\> the chosen tag.
    - Select *Set as latest release*
    - Select *Create a discussion for this release*.
    - Drag the APK file from the `app\build\outputs\apk\release` subdirectory to the new release.
    - Drag the APK file from the `app\build\outputs\apk\firebase` subdirectory to the new release.
    - Drag the AAB file from the `app\build\outputs\bundle\release` subdirectory to the new release.
    - Drag the AAB file from the `app\build\outputs\bundle\firebase` subdirectory to the new release.
    - Press *Publish release*.
    - Fetch the master branch from GitHub in order to import the new tags.
    - Decide on the next release number x'.y'.z'.
      Apply a tag with that name on the next commit on the master branch.

## APK signing

Non-debug APK's and AAB's must be signed.
See [Sign your app](https://developer.android.com/studio/publish/app-signing.html#kts).
Android refuses to install unsigned APK's.
Debug APKs are automatically signed with a special debug key.
But non-debug APKs must be signed by a developer key.
New versions of an already installed app must also be signed with the same key,
otherwise the new version is refused by Android.

Gradle automatically signs releas and firebase builds when `./gradlew build` is ran.
That behavior is defined in the `buildTypes` block of the app module's `build.gradle.kts` file.
The release block uses a `signingConfig` with the name "release",
which is itself defined in the `signingConfigs` of the app module's `build.gradle.kts` file.
The values `storeFile`, `storePassword`, `keyAlias` and `keyPassword` are not defined in the `build.gradle.kts` file.
`build.gradle.kts` must be pushed to the public git repo which would make the keys and passwords public.
The `gaai-release.keystore` file which contains the keys is for the same reason not included in Git.
Depending on the build being a local gradle build or a *Github Action* build, the key handling is different.

For a local build, the 4 values must be defined in a `gaai-release-keystore.properties` file,
located in the ANDROID_USER_HOME directory (`C:\Users\<user>>\.android` on Windows).
`storeFile` defines the location of the
[keystore](https://cr.openjdk.org/~alanb/api/java.base/java/security/KeyStore.html) file.
The keystore file is of type "JKS".
It contains a 256 bits Elliptic Curve key on the NIST P-256 elliptic curve.
This key is used with the *SHA-256 with ECDSA* signature algorithm to sign the APK.
All of that is done automatically by gradle.

For a *Github Action* build, the 4 values are stored in Github's
[repository secrets](https://docs.github.com/en/actions/how-tos/writing-workflows/choosing-what-your-workflow-does/using-secrets-in-github-actions).
The following 4 secrets are used:

- SIGNING_STORE_FILE: the Base64 encoded contents of the `storeFile` (`gaai-release.keystore`)
- SIGNING_STORE_PASSWORD: the value of `storePassword`
- SIGNING_KEY_ALIAS: the value of `keyAlias`
- SIGNING_KEY_PASSWORD: the value of `keyPassword`

The keystore file can be manipulated using the command line tool
[keytool](https://cr.openjdk.org/~jjg/8261930/docs/specs/man/keytool.html)
or with the [KeyStore Explorer](https://keystore-explorer.org/).
The latter has a GUI.

In order to get the Base64 encoded contents of `gaai-release.keystore`, execute the following in Git bash:

```
  base64 gaai-release.keystore | tr -d '\n' > gaai-release-keystore.txt
```

The contents of `gaai-release-keystore.txt` is to be put in the SIGNING_STORE_FILE Github secret.

The Keystore, signing key and passwords are not public.
They are currently managed by the project maintainer,
who is also responsible for backups.

### Verify if an apk file is signed

With the following command one can verify if apk files in the current directory are signed.

```
apksigner verify --print-certs *.apk
```

This command does not verify the signature, but just prints the certificate information.
`apksigner` is part of the Android SDK build tools, which should be automatically installed when 
Android Studio is installed.
Note that the location of `apksigner` is not automatically included in the PATH.
I manually had to add `C:\Users\frank\AppData\Local\Android\Sdk\build-tools\36.0.0` to the PATH.
`apksigner` is a `bat` file, so will only work from a DOS shell.
See [apksigner](https://docs.digicert.com/zf/software-trust-manager/client-tools/signing-tools/third-party-signing-tool-integrations/apksigner.html).

### Verify if an aab file is signed

With the following command one can verify if aab files in the current directory are signed.

```
 keytool -printcert -jarfile *.aab
```

This command does not verify the signature, but just prints the certificate information.
`keytool` is part of the Java SDK distribution, which should be automatically installed when
Android Studio is installed.

## Gradlew commands

- `./gradlew dokkaGenerate` creates Html based documentation from the Kdoc documentation used for documenting the source
  code.
  The documentation is generated in the `app\build\documentation\html` directory
- `./gradlew testReleaseUnitTest` runs the local (JUnit) tests for the release build.
  The Junit xml files are generated in the `app\build\test-results\testReleaseUnitTest` directory.
  Html test result files are generated in the `app\build\reports\tests\testReleaseUnitTest` directory.
- `./gradlew testDebugUnitTest` runs the local (JUnit) tests for the debug build.
  The Junit xml files are generated in the `app\build\test-results\testDebugUnitTest` directory.
  Html test result files are generated in the `app\build\reports\tests\testDebugUnitTest` directory.
  In addition it performs code coverage analysis using jacoco.
  The jacoco result file is written to `app\build\outputs\unit_test_code_coverage\debugUnitTest\testDebugUnitTest.exec`.
- `./gradlew test` runs both the above.
- `./gradlew build` assembles and tests all the build types in the project.
  The release apk files are generated in the `app\build\outputs\apk\release` directory.
  Note that the *release* build type requires that the signingConfig *release* is correctly setup.
  See [APK Signing](#apk-signing).
- `./gradlew createDebugCoverageReport` creates an html coverage report in `app\build\reports\coverage\test\debug`
  based on the jacoco results in `app\build\outputs\unit_test_code_coverage\debugUnitTest\testDebugUnitTest.exec`.
  This task is **not** included in `./gradlew build`.
- `./gradlew bundle` creates an aab bundle. This bundle can be used to upload to Google Play.
  Bundles are not signed.
- `./gradlew assembleDebug` only assembles the debug build type
- `./gradlew assembleRelease` only assembles the release build type
- `./gradlew assembleFirebase` only assembles the release build type

## GitHub actions

The following GitHub actions are defined:

- Android CI
- pages-build-deployment

### Android CI

On every push to the GitHub repository, this action execute's `./gradlew build` and uploads the generated
signed APK to the build artifacts.
It also publishes the code coverage on [Codecov](https://app.codecov.io/github/FrankHJCuypers/gaai).

### pages-build-deployment

Action generated by GitHub to build the project's documentation and deploy it to GitHub-pages.
It was setup as follows:

- In the project's repository in GitHub, select *Settings*, then under *Code and automation* select *Pages*.
- Under *Source*, select *Deploy from a branch* and choose the master branch.

## Firebase

The Gaai **debug** and **firebase** build types use [Firebase](https://firebase.google.com/) integration, see
[Gaai on Firebase](https://console.firebase.google.com/project/gaai-3e164/overview).
As explained in [PRIVACY.md](PRIVACY.md#google-analytics-and-crashlytics), 
this has an impact on the user's privacy.

Its configuration is in app\google-services.json.
According to the video at the top of 
[Add Firebase to your Android project](https://firebase.google.com/docs/android/setup) at around 7:50, 
none of this info is secret.
But for open source projects it is best that everyone that works on the project, 
uses its own Firebase project and configuration settings.
Therefore it is best to not include the json file in git.
The video describes how everyone can setup their own Firebase project.
But not including it in git, will generate failures during Github Actionns build.
Solutions are provided in 
[Configure Firebase project for Continuous Integration builds](https://proandroiddev.com/configure-firebase-project-for-continuous-integration-builds-833f08561a73),
but are a bit overkill for a project like Gaai.
Therefore the current choice was to keep the google-services.json file in git.
This might need to be changed in the future.

### Firebase analytics

The aforementioned video also shows how to trigger a Firebase.analytics.logEvent().
Events are only send to Firebase once every hour or so.
With the following command, your device does this more often.

`adb shell setprop debug.firebase.analytics.app be.cuypers_ghys.gaai`

Firebase analytics is enabled.
Currently there is only 1 call to Firebase.analytics.logEvent() present in the code.
A press to the Get Time button writes an event.
It is used for testing the Firebase analytics integration.

### Firebase Crashlytics

Crashlytics is enabled.
Gaai instances that crash report this to the Crashlytics server.
On the Crashlytics server, the crashes are visible with stack dumps.
That way, the developers are made aware of crashes in the filed and are provided with information that aids in 
debugging the crash.
 
## Distributing via Google Play

It is not yet possible to distribute Gaai via Google Play.
Efforts to make this happen have been analyzed and seem to be a lot of work and problems to solve.
Not sure if it is worthwhile for such a small project with a limited number of users.

Blocking problems

- For new personal Google Play accounts, applications can only go into production after successful testing
  by at least 12 testers. See 
  [Everything about the 12 testers requirement](https://support.google.com/googleplay/android-developer/community-guide/255621488/everything-about-the-12-testers-requirement?hl=en).
  This does not seem very realistic for Gaai, seen the limited number of users.

### Current status

Try to build a signed aab file, which is what Google Play requires.
Building the aab works with `./gradlew bundle`.
Verification with `keytool` shows it is signed.