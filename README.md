
[![maintainer](https://img.shields.io/badge/maintainer-Frank%20HJ%20Cuypers-green?style=for-the-badge&logo=github)](https://github.com/frankhjcuypers)
[![GitHub Discussions](https://img.shields.io/github/discussions/FrankHJCuypers/Gaai?style=for-the-badge&logo=github)](https://github.com/FrankHJCuypers/Gaai/discussions)

[![android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![android studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)](https://developer.android.com/studio)
[![kotlin](	https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)](https://developer.android.com/kotlin)
[![compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)

[![alpha](https://img.shields.io/badge/version-experimantal-red)](https://kotlinlang.org/docs/components-stability.html#stability-levels-explained)


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
// TODO


### Bluetooth 

Gaai uses Bluetooth Low Energy (BLE) to connect to the Nexxtender Home, 
so make sure that bluetooth is enabled on your mobile device.

In addition, Gaai requires some Bluetooth related permissions in order to function.
When Gaai is ran for the first time, it will ask for the required permissions.
In that case, please press the "Request permissions" button, and then press "Allow".
If you press "Don't Allow", Gaai will insist that it needs the permissions to continue,
but recent Android versions only allow an app to ask only once for permissions.
The only way out in that case is to go to Android Settings, then Apps, select Gaai and then Permissions. 
in the "Not Allowed" section you will find the permission "Nearby devices":
click on it and press "Allow". You now have granted the permission.

Depending on the Android version, the following permissions are required:
+ Up to and including Android 11 (SDK <= 30): `android.permission.ACCESS_FINE_LOCATION`
+ Starting from Android 12 (SDK >= 31): `android.permission.BLUETOOTH_SCAN` and `android.permission.BLUETOOTH_CONNECT`

Note that permission naming is confusing in Android! 
Although the Gaai application code asks for the permissions `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`
in Android 12 and up:
+ The Android pop-up where you have to allow the permissions, will ask
  "Allow Gaai to find, connect to, and determine the relative position of nearby devices."
+ In Android Settings -> Apps -> Gaai -> Permission the permission is named 
  "Nearby Devices permission".

### Bluetooth pairing

The first time that a Nexxtender Home is connected to in Gaai, Gaai will show a pop-up asking to 
enter the 6-digit PIN.
After entering the PIN, pairing completes.

If the mobile phone user later removes the Nexxtender *Home* device from the "Bluetooth" menu
in Android, the pairing information is lost and Gaai will not automatically open the pairing 
dialog again.
The only options to restore from this situation are either of:
- pair the device from the Android Bluetooth menu.
- delete the Nexxtender Home from Gaai and then add it again.
  Gaai will now again ask to pair.


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

## Acknowledgements

Part of the code is based on existing code.
The following code was heavily borrowed from:
- [NordicSemiconductor Kotlin-BLE-Library uiscanner](https://github.com/NordicSemiconductor/Kotlin-BLE-Library/tree/main/uiscanner)

## Disclaimer

This app was developed by someone without prior knowledge of most of the components and tools used
developing it: Android app development, Android Studio, Gradle, Kotlin, Jetpack compose, BLE, ...
Treat it as such and use at your own risk.

## Support

For support, questions, or feedback, please open an issue on the [GitHub repository](https://github.com/FrankHJCuypers/Gaai/issues/new).
