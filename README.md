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
and therefore impossible to to operate the Nexxtender Home charger, which has no UI or inputdevice
on its own.
If you no longer have a login, using Gaai is an option.

## Development

This app is developed in [Kotlin](https://developer.android.com/kotlin) with
[Android Studio](https://developer.android.com/studio) using
- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- [Kotlin coroutines](https://developer.android.com/kotlin/coroutines)
- [Kotlin BLE Library for Android](https://github.com/NordicSemiconductor/Kotlin-BLE-Library)


## Features

- **BLE Connectivity:** establish a BLE connection between your Android device and the  
  Powerdale Nexxtender Home EV charger for data exchange and control

## Getting Started

//TODO: explain how to run the app

## Links

Useful information can be found at
- [Nexxtender Information, Frank HJ Cuypers, Google Docs] (TBD)
- [https://github.com/geertmeersman/nexxtender](ESPHome BLE Client for Powerdale Nexxtender EV Charger)
- [https://github.com/geertmeersman/nexxtmove](Nexxtmove for Home Assistant)
- [https://github.com/toSvenson/nexxtender-ble](Nexxtender Home Bluetooth Controller)
- [Android Nexxtmove app](https://play.google.com/store/apps/details?id=com.powerdale.nexxtender)
- [Android Nexxtender Installer app](https://play.google.com/store/apps/details?id=com.powerdale.homeinstaller)

## License

This project is licensed under the GNU AGPLv3 License. See the [LICENSE](LICENSE) file for details.

## Support

For support, questions, or feedback, please open an issue on the [GitHub repository](https://github.com/FrankHJCuypers/Gaai/issues/new).
