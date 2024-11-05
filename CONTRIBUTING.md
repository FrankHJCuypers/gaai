# Contributing

# Source code format

The Kotlin code is formatted according to the official
[Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html).
The only exceptions applied are:

+ A tab is 2 spaces instead of 4.
+ Line wrapping is applied after 120 characters in stead of 100.

The Android Studio project editor is set up to automatically apply this style.
Each time that a Kotlin file is saved or committed, it is also automatically reformatted.

# Gradlew commands

- `./gradlew.bat dokkaHtml` creates Html based documentation from the Kdoc documentation used for documenting the source
  code.
