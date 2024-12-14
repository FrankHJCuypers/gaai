# Development

## Gradlew commands

- `./gradlew.bat dokkaHtml` creates Html based documentation from the Kdoc documentation used for documenting the source
  code.
  The documentation is generated in the `app\build\documentation\html` directory
- `./gradlew testReleaseUnitTest` runs the Run unit tests for the release build.
  The Junit xml files are generated in the `app\build\test-results\testReleaseUnitTest` directory.
  Html test result files are generated in the `app\build\reports\tests\testReleaseUnitTest` directory.
- `./gradlew testDebugUnitTest` runs the Run unit tests for the debug build.
  The Junit xml files are generated in the `app\build\test-results\testDebugUnitTest` directory.
  Html test result files are generated in the `app\build\reports\tests\testDebugUnitTest` directory.
- `./gradlew test` runs both the above.
- `./gradlew build` assembles and tests the project.
  The release apk files are generated in the `app\build\outputs\apk\release` directory.

