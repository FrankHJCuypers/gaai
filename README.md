
[![maintainer](https://img.shields.io/badge/maintainer-Frank%20HJ%20Cuypers-green?style=for-the-badge&logo=github)](https://github.com/frankhjcuypers)
[![GitHub Discussions](https://img.shields.io/github/discussions/FrankHJCuypers/Gaai?style=for-the-badge&logo=github)](https://github.com/FrankHJCuypers/Gaai/discussions)

[![android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![android studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)](https://developer.android.com/studio)
[![kotlin](	https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)](https://developer.android.com/kotlin)

[![github release](https://img.shields.io/github/v/release/FrankHJCuypers/Gaai?logo=github)](https://github.com/FrankHJCuypers/Gaai/releases)
[![github release date](https://img.shields.io/github/release-date/FrankHJCuypers/Gaai)](https://github.com/FrankHJCuypers/Gaai/releases)
[![GitHub License](https://img.shields.io/github/license/FrankHJCuypers/Gaai)](LICENSE)
[![Gitea Last Commit](https://img.shields.io/gitea/last-commit/FrankHJCuypers/Gaai)](https://github.com/FrankHJCuypers/Gaai/commits)
[![github contributors](https://img.shields.io/github/contributors/FrankHJCuypers/Gaai)](https://github.com/FrankHJCuypers/Gaai/graphs/contributors)
[![github commit activity](https://img.shields.io/github/commit-activity/y/FrankHJCuypers/Gaai?logo=github)](https://github.com/FrankHJCuypers/Gaai/commits/main)


# Simple Nexxtender Home android app

The goal of this app is to be able to control the Nexxtender Home charger without requiring the 
[Android Nexxtmove app](https://play.google.com/store/apps/details?id=com.powerdale.nexxtender).
The [Android Nexxtmove app](https://play.google.com/store/apps/details?id=com.powerdale.nexxtender)
is the official app for controlling the Nexxtender Home charger and for syncing billing information
to the [Nexxtmove.me website](https://www.nexxtmove.me/) for refunding.
Both the [Nexxtmove app](https://play.google.com/store/apps/details?id=com.powerdale.nexxtender)
and the [Nexxtmove.me website](https://www.nexxtmove.me/) require a username and password to login.
Without it, it is impossible to use the
[Nexxtmove app](https://play.google.com/store/apps/details?id=com.powerdale.nexxtender)
and therefore impossible to to operate the Nexxtender Home charger, which has no UI or input device
on its own.
If you no longer have a login, using Gaai is an option.

## Development

This app is developed in [Kotlin](https://developer.android.com/kotlin) with
[Android Studio](https://developer.android.com/studio) using
- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- [Kotlin coroutines](https://developer.android.com/kotlin/coroutines)
- [Kotlin BLE Library for Android](https://github.com/NordicSemiconductor/Kotlin-BLE-Library)

## Gaai?

Choosing a short distinctive name for a project is always difficult.
A bird name was chosen.
[Gaai](https://nl.wikipedia.org/wiki/Gaai) is Dutch for the
[Eurasian jay](https://en.wikipedia.org/wiki/Eurasian_jay).

## Features

- **BLE Connectivity:** establish a BLE connection between your Android device and the  
  Powerdale Nexxtender Home EV charger for data exchange and control

## Getting Started

//TODO: explain how to run the app

## Links

Useful information can be found at
- [Nexxtender Information, Frank HJ Cuypers, Google Docs] (TBD)
- [ESPHome BLE Client for Powerdale Nexxtender EV Charger](https://github.com/geertmeersman/nexxtender) 
- [Nexxtmove for Home Assistant](https://github.com/geertmeersman/nexxtmove)
- [Nexxtender Home Bluetooth Controller](https://github.com/toSvenson/nexxtender-ble)
- [Discord chat](https://discord.gg/PTpExQJsWA) related to 
  [ESPHome BLE Client for Powerdale Nexxtender EV Charger](https://github.com/geertmeersman/nexxtender)
  and Nexxtmove for Home Assistant](https://github.com/geertmeersman/nexxtmove)
- [Android Nexxtmove app](https://play.google.com/store/apps/details?id=com.powerdale.nexxtender)
- [Android Nexxtender Installer app](https://play.google.com/store/apps/details?id=com.powerdale.homeinstaller)

## License

This project is licensed under the GNU AGPLv3 License. See the [LICENSE](LICENSE) file for details.

## Support

For support, questions, or feedback, please open an issue on the [GitHub repository](https://github.com/FrankHJCuypers/Gaai/issues/new).
