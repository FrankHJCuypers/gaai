<meta name="google-site-verification" content="aIbjsFn9hlzewklZcNkHGOw9gGL_YMnqX1Ui1GuCDr4" />
<br/>
<br/> 

![Attention](https://img.shields.io/badge/Attention-The_Repository_was_renamed_from_Gaai_to_gaai!_Please_adapt_your_links-red?style=flat&logo=github&labelColor=red)

<br/>
<br/>



[![maintainer](https://img.shields.io/badge/maintainer-Frank%20HJ%20Cuypers-green?style=for-the-badge&logo=github)](https://github.com/frankhjcuypers)
[![GitHub Discussions](https://img.shields.io/github/discussions/FrankHJCuypers/gaai?style=for-the-badge&logo=github)](https://github.com/FrankHJCuypers/gaai/discussions)
[![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/FrankHJCuypers/gaai?style=for-the-badge&logo=github)](https://github.com/FrankHJCuypers/gaai/issues)

[![android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![android studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)](https://developer.android.com/studio)
[![kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)](https://developer.android.com/kotlin)
[![compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)

[![beta](https://img.shields.io/badge/version-beta-purple)](https://kotlinlang.org/docs/components-stability.html#stability-levels-explained)

[![GitHub Release](https://img.shields.io/github/v/release/FrankHJCuypers/gaai?include_prereleases&display_name=tag&logo=github)](https://github.com/FrankHJCuypers/gaai/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date-pre/FrankHJCuypers/gaai?logo=github)](https://github.com/FrankHJCuypers/gaai/releases)
[![GitHub License](https://img.shields.io/github/license/FrankHJCuypers/gaai?logo=github)](LICENSE)

[![GitHub last commit](https://img.shields.io/github/last-commit/FrankHJCuypers/gaai?logo=github)](https://github.com/FrankHJCuypers/gaai/commits)
[![GitHub contributors](https://img.shields.io/github/contributors/FrankHJCuypers/gaai?logo=github)](https://github.com/FrankHJCuypers/gaai/graphs/contributors)
[![GitHub commit activity (master)](https://img.shields.io/github/commit-activity/y/FrankHJCuypers/gaai/master?logo=github)](https://github.com/FrankHJCuypers/gaai/commits/master)

# Nexxtender Charger android app

The goal of this app is to control the *Nexxtender Home* and *Nexxtender Mobile* charger without requiring the
[Android Nexxtmove](https://play.google.com/store/apps/details?id=com.powerdale.nexxtender) app.
The Android Nexxtmove app is the official app for controlling the *Nexxtender* chargers and for syncing
billing information to the [Nexxtmove.me](https://www.nexxtmove.me/) website for refunding.
Both the Nexxtmove app and the Nexxtmove.me website require a username and password to login.
Without it, it is impossible to use the Nexxtmove app and therefore impossible to operate the *Nexxtender Home* charger,
which has no UI or input device on its own.
If you no longer have a valid username and password for the Nexxtmove app, using *Gaai* is an option.
Other alternatives are

- [ESPHome BLE Client for Powerdale Nexxtender EV Charger](https://github.com/geertmeersman/nexxtender)
- [Nexxtender Home Bluetooth Controller](https://github.com/toSvenson/nexxtender-ble)
- [NEXXTLOOK](https://www.lookwatt.be/drupal/web/nexxtlook)
- [NexxtenderBlackCable](https://github.com/Noudicus/NexxtenderBlackCable)

*Gaai*'s source code is hosted on [Github](https://github.com/FrankHJCuypers/gaai).

## Gaai?

Choosing a short distinctive name for a project is always difficult.
A bird name was chosen.
[Gaai](https://nl.wikipedia.org/wiki/Gaai) is Dutch for the
[Eurasian jay](https://en.wikipedia.org/wiki/Eurasian_jay).

# Features

- Only requires to install *Gaai*.
- No additional hardware required.
- No www.Nexxtmove.me account required.
- Connects over [Bluetooth Low Energy (BLE)](https://en.wikipedia.org/wiki/Bluetooth_Low_Energy) to the
  *Nexxtender Home* or *Nexxtender Mobile*.
- Finds and connects to the device based on its PN and SN. No MAC required.
- Shows real time data from the *Nexxtender Home* or *Nexxtender Mobile*: device information, charging status, current,
  power consumption, ...
- Allows to control the *Nexxtender Home*: start and stop the charger in ECO or MAX mode,
  sync the time with the mobile phone.
- Allows to configure the *Nexxtender Home*: default charging mode, max grid and max device current, off-peak hours.
- Management of badges (add, delete).
- Remembers the PN and SN of multiple devices.

The following features are **not** available:

- Synchronization with the www.Nexxtmove.me website from Diego.
- Download of Events, Metrics, charging information for billing, CDR, CCDT, ...
- Firmware upload.

# Installing Gaai

There are 3 ways to install *Gaai* on your Android phone:

1. [Install Gaai using APK file](#install-gaai-using-apk-file)
2. [Install Gaai using Android Studio on Windows](#install-gaai-using-android-studio-on-windows)
3. [Install Gaai using Google Play Store](#install-gaai-using-google-play-store)

## Install Gaai using APK file

For most people, this is the preferred way of installing.

An APK file generated from *Gaai* can be installed as described in
[How to install third-party apps without the Google Play Store](https://www.androidauthority.com/how-to-install-apks-31494/).

*Gaai* APK files are available for the official
[releases](https://github.com/FrankHJCuypers/gaai/releases) in the GitHub repo.
Select a release, open its *assets* and select the APK file.

If the selected APK is a `-debug` version, it will install without questions.
If it is a `-release` version, it is signed with the Gaai signature key and Android will perform some tests.
If *Google Play Protect* recommends to scan the app, select "scan app" and wait for the scanning to complete.
If the scanning is complete, select "Install app".

## Install Gaai using Android Studio on Windows

*Gaai* can be installed on your Android phone using Android Studio.
See [Environment setup](DEVELOPMENT.md#environment-setup).
Only used by developers during development.

## Install Gaai using Google Play Store

Once *Gaai* is sufficiently stable, distributing it via Google Play Store is an option to consider.
For the moment it is not available.

# Getting Started

## Bluetooth

*Gaai* uses BLE to connect to the *Nexxtender Home* or *Nexxtender Mobile*,
so make sure that Bluetooth is enabled on your mobile device.

In addition, *Gaai* requires some Bluetooth related permissions in order to function.
When *Gaai* is ran for the first time, it will ask for the required permissions.
In that case, please press the "Request permissions" button, and then press "Allow".
If you press "Don't Allow", *Gaai* will insist that it needs the permissions to continue,
but recent Android versions only allow an app to ask only once for permissions.
The only way out in that case is to go to Android Settings -> Apps -> Gaai -> Permissions.
In the "Not Allowed" section you will find the permission "Nearby devices":
click on it and press "Allow". You now have granted the permission.

Depending on the Android version, the following permissions are required:

+ Up to and including Android 11 (SDK <= 30): `android.permission.ACCESS_FINE_LOCATION`
+ Starting from Android 12 (SDK >= 31): `android.permission.BLUETOOTH_SCAN` and `android.permission.BLUETOOTH_CONNECT`

Note that permission naming is confusing in Android!
Although the *Gaai* application code asks for the permissions `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`
in Android 12 and up:

+ The Android pop-up where you have to allow the permissions, will ask
  "Allow *Gaai* to find, connect to, and determine the relative position of nearby devices."
+ In Android Settings -> Apps -> Gaai -> Permission the permission is named
  "Nearby Devices permission".

## Bluetooth pairing

The first time that *Gaai* connects to a *Nexxtender Home* or *Nexxtender Mobile* device,
*Gaai* will show a pop-up asking to enter the 6-digit PIN.
After entering the PIN, pairing completes.

See [PIN value](#pin-value) for the PIN value.

If the mobile phone user later removes the *Nexxtender Home* or *Nexxtender Mobile* device from the "Bluetooth" menu in
Android,
the pairing information is lost and *Gaai* will not automatically open the pairing dialog again.
The only options to restore from this situation are either of:

- pair the device from the Android "Bluetooth menu".
- delete the *Nexxtender Home* or *Nexxtender Mobile* from *Gaai* and then add it again.
  *Gaai* will now again ask to pair.

### PIN value

The Nexxtender charger installer that installed your charger and connected it with the Nexxtmove app on your
mobile phone, has paired it with the Nexxtender charger using the correct PIN value.
In general, the installer will not have provided you with that PIN value.
As long as you use the same mobile phone to connect to the Nexxtender charger, both devices should stay paired and
there is no issue.
But when you need to move to another phone, you need the PIN value.
If you don't have the PIN code, you can provide the *Gaai* GitHub repository owner with the PN and SN values
as mentioned in [Add a Nexxtender charger device](#add-a-nexxtender-device)
and he will calculate it for you.

## First start

When *Gaai* is started the first time, it has no *Nexxtender* devices in its database yet.
*Gaai* will display a home screen like the following:

![Empty DB](docs/images/EmptyDB.png)

At the bottom right is a button with a "+" that allows to add a *Nexxtender* device.

At the top right there is a  "&#8942;" icon. 
When you click on it, a drop down menu with 2 items appears.

![Home Drop Down Menu](docs/images/HomeDropDownMenu.png)

1. *About* opens the About screen with some information.
2. *Help* opens the https://frankhjcuypers.github.io/gaai/ page with this README in your browser.

## Example About screen

![Home Drop Down Menu](docs/images/About.png)

## Add a Nexxtender device

When tapping the "+" in the home screen, the following dialog box appears:

![Device Entry Empty](docs/images/DeviceEntryEmpty.png)

The *Scan device* button stays grayed out until a valid PN and SN are entered.
The PN and SN of your *Nexxtender * can be found at the bottom of your device,
as is shown in

![serial](docs/images/sn.webp).

When a valid PN and SN are entered, the *Scan Device* button become active.

![Device Entry Correct with SN and PN](docs/images/DeviceEntryCorrectSNPN.png)

Make sure that you are close to the *Nexxtender* device and that no other device is connected with it over BLE.
Tap the *Scan Device* button. Scanning will start and the button will now show *Cancel Scanning* allowing you to
cancel the scan if it takes to long.

![Device Entry Scanning](docs/images/DeviceEntryScanning.png)

If you entered the correct PN and SN,
*Gaai* should find the *Nexxtender* in less then a few seconds and show the following screen.

![Device Entry Found](docs/images/DeviceEntryFound.png)

Gaai shows a card with the details of the found device.

+ At the top is the type of the Nexxtender Charger: HOME or MOBILE. The BLE connection status is still *Not connected*.
+ At the middle left is the PN.
+ At the middle right is the SN.
+ At the bottom left is the MAC.
+ At the bottom right is the Service Data used to find the BLE device based on its advertisement packet.

Press *Save* to save this device in *Gaai's* database.
This will bring you back to the previous screen, now showing one device.

If the device that you scanned was already in *Gaai's* database, you will see

![Device Entry Duplicate](docs/images/DeviceEntryDuplicate.png)

*Gaai* will not let you create duplicates.

## List of Devices

As soon as the database contains at least one device, *Gaai* shows a list of all registered devices at startup as
shown in the next picture.

![Home Screen with 3 Devices](docs/images/HomeScreenThreeDevices.png)

In this screen you can

+ Add another device by clicking on the "+" button at the bottom right.
+ Remove a device by swiping its card to the right.
  A Dialog box will pop up asking you to confirm the delete:

![Device Delete Confirmation](docs/images/DeviceDeleteConfirm.png)

+ Connect to the device by clicking on its card.
  This will open the [Device details](#device-details) screen.
  Make sure that you are close to the *Nexxtender* charger and that no other device is connected with it over BLE.

## Device details

When clicking on a device card in [List of Devices](#list-of-devices),
*Gaai* tries to connect to the device and shows the following screen.

![Device Detail](docs/images/DeviceDetailScreen.png)

The complete screen is larger then the phone's screen.
You must scroll up to see the other parts.
When the screen opens, only the labels are shown without data values.
The values are populated as soon as *Gaai* has established the connection and read all the data.
That can take 5 seconds.
The BLE connection status at the top right of the first card can have the following values:

| Value         | Icon                                                                                         | Description                                                              |
|---------------|----------------------------------------------------------------------------------------------|--------------------------------------------------------------------------|
| Not connected | ![Not connected](docs/images/bluetooth_disabled_24dp_1F1F1F_FILL0_wght400_GRAD0_opsz24.svg)  | Gaai is not connected with the charger                                   |
| Connecting    | ![Not connected](docs/images/bluetooth_searching_24dp_1F1F1F_FILL0_wght400_GRAD0_opsz24.svg) | Gaai is trying to connect to the charger                                 |
| Discovering   | ![Discovering](docs/images/bluetooth_searching_24dp_1F1F1F_FILL0_wght400_GRAD0_opsz24.svg)   | Gaai is connected to the charger and is now discovering its BLE services |
| Connected     | ![Connected](docs/images/bluetooth_connected_24dp_1F1F1F_FILL0_wght400_GRAD0_opsz24.svg)     | Gaai is connected to the charger and has discovered all its BLE services | 
| Unknown       | ![Unknown](docs/images/bluetooth_disabled_24dp_1F1F1F_FILL0_wght400_GRAD0_opsz24.svg)        | Should not happen                                                        |

If the BLE status stays *Not connected*,
it might be that another BLE client is still connected to the device.
It could also be a problem in *Gaai*.
It is normally solved by going back to the previous screen using the left arrow at the top left of the screen,
and try again.

The card on the top is the same one as from the [List of Devices](#list-of-devices).

The next cards show the device name and the Device Information.
These contain general BLE information, not specific for a *Nexxtender* charger.

The next cards contain specific *Nexxtender* charger information as reported by the device.
The *Nexxtender Home* supports all cards.
The *Nexxtender Mobile* only supports the [Basic Data](#basic-data) card.

### Basic Data

The Basic Data card shows Basic *Nexxtender Home* and *Nexxtender Mobile* charging data.

![Basic Data Card](docs/images/BasicDataCard.png)

| Field         | Description                                               |
|---------------|-----------------------------------------------------------|
| Seconds       | Number of seconds since start of charging                 |
| Discriminator | Possible discriminator states: STARTED, CHARGING, STOPPED |
| Status        | Possible states: PLUGGED, CHARGING, FAULT                 |
| Energy        | Total energy in kWh charged during this session           |
| Phase count   | Charging Phase Count                                      |

### Grid Data

The Grid Data card shows Grid Data as measured by the *Nexxtender Home*.
It is not supported by the *Nexxtender Mobile*.

![Grid Data Card](docs/images/GridDataCard.png)

| Field      | Description                                                                                                                                                                                           |
|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Timestamp  | Current time                                                                                                                                                                                          |
| L1 Current | Grid phase L1 current                                                                                                                                                                                 |
| L2 Current | Grid phase L2 current                                                                                                                                                                                 |
| L3 Current | Grid phase L3 current                                                                                                                                                                                 |
| Consumed   | Total power injected in the Grid in cWh during this quarter? See [Nexxtender Charger Information](https://github.com/FrankHJCuypers/gaai/wiki/Nexxtender-Charger-Information#charging-grid-data-0xd0) |
| Interval   | Counts the seconds in the current quarter. Is set to 0 every multiple of a quarter (hh:00, hh:15, hh:30, hh:45)                                                                                       |

### Car Data

The Car Data card shows Car Data as measured by the *Nexxtender Home*.
It is not supported by the *Nexxtender Mobile*.

![Car Data Card](docs/images/CarDataCard.png)

| Field      | Description                    |
|------------|--------------------------------|
| Timestamp  | Current time                   |
| L1 Current | Car phase L1 current           |
| L2 Current | Car phase L2 current           |
| L3 Current | Car phase L3 current           |
| L1 Power   | Car phase L1 power consumption |
| L2 Power   | Car phase L2 power consumption |
| L3 Power   | Car phase L3 power consumption |

### Advanced Data

The Advanced Data card shows Advanced Data as measured by the *Nexxtender Home*.
It is not supported by the *Nexxtender Mobile*.

![Advanced Data Card](docs/images/AdvancedDataCard.png)

| Field                | Description                                                                                                             |
|----------------------|-------------------------------------------------------------------------------------------------------------------------|
| Timestamp            | Current time                                                                                                            |
| I Available          | Available current                                                                                                       |
| Grid Power           | Total power consumption from the grid                                                                                   |
| Car Power            | Total power consumption by the car                                                                                      |
| Authorization Status | Possible states: UNAUTHORIZED, AUTHORIZED DEFAULT, AUTHORIZED ECO, AUTHORIZED MAX, CHARGE STOPPED IN APP, CHARGE PAUSED |
| Error Code           | Error code returned by the Nexxtender Home                                                                              |

### Configuration

The Configuration card shows the data that can be configured in the *Nexxtender Home*.
It is not supported by the *Nexxtender Mobile*.
In theory all these fields are configurable and can be changed.
*Gaai* only allows to change the fields marked with ![edit outline](docs/images/mdi--edit-outline.png).
There are three different layouts, depending on the *Firmware Version*.

#### Configuration 1.0

This is the layout for *Firmware Version* below 1.1.0.

![Configuration Card1_0](docs/images/ConfigurationCard1_0.png)

| Field        | Description                                                                                                                                                   |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Default Mode | Possible default charging modes: ECO_PRIVATE, MAX_PRIVATE, ECO_OPEN, MAX_OPEN                                                                                 |
| Max Grid     | Maximum allowed grid consumption. Set this equal or smaller to the value of the main grid fuses. Set this to a lower value if you have a peak tariff contract |
| Safe         | Minimum charging current for the device. Certified chargers are required to provide a minimum of 6A                                                           |
| Weekdays     | Off peak period for week days                                                                                                                                 |
| Weekend      | Off peak period for weekend days                                                                                                                              |

#### Configuration 1.1

This is the layout for *Firmware Version* from 1.1.0 to 3.50.
The fields "Max Device" and "Network Type" are added compared to the 1.0 format.

![Configuration Card1_1](docs/images/ConfigurationCard1_1.png)

| Field        | Description                                                                           |
|--------------|---------------------------------------------------------------------------------------|
| Default Mode | See [Configuration 1.0](#configuration-10)                                            |
| Max Grid     | See [Configuration 1.0](#configuration-10)                                            |
| Safe         | See [Configuration 1.0](#configuration-10)                                            |
| Weekdays     | See [Configuration 1.0](#configuration-10)                                            |
| Weekend      | See [Configuration 1.0](#configuration-10)                                            |
| Max Device   | Maximum allowed charging speed in A for the EV device. Safe <= Max Device <= Max Grid |
| Network Type | Mono/Tri+N or Tri                                                                     |

For the field "Network Type" *Gaai* does not implement the option to change the value,
as it is probably not wise to change this value if you don't know what you are doing.

#### Configuration CBOR

This is the layout for *Firmware Version* above and including 3.50.
The fields "I EVSE Min" and "I Capacity" are added compared to the 1.1 format.
Most other fields from versions 1.0 and 1.1 have a different name in CBOR format.

![Configuration CardCBOR](docs/images/ConfigurationCardCBOR.png)

| Field                    | Description                                                                                                                                                                                     |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Charge Mode              | See *Default Mode* in [Configuration 1.1](#configuration-11)                                                                                                                                    |
| I Max                    | See *Max Grid* in [Configuration 1.1](#configuration-11)                                                                                                                                        |
| I Level 1                | See *Safe* in [Configuration 1.1](#configuration-11)                                                                                                                                            |
| Weekdays                 | See [Configuration 1.1](#configuration-11)                                                                                                                                                      |
| Weekend                  | See [Configuration 1.1](#configuration-11)                                                                                                                                                      |
| I EVSE Max               | See *Max Device* in [Configuration 1.1](#configuration-11)                                                                                                                                      |
| Phase Seq (Network Type) | See *Network Type* in [Configuration 1.1](#configuration-11)                                                                                                                                    |
| I EVSE Min               | Not used                                                                                                                                                                                        |
| I Capacity               | Peak grid current limit. I Level 1 <= I Capacity <= I Max. You can set this to a lower value if you have a peak tariff contract. Configurations 1.0 and 1.1 behave as if I Capacity == Max Grid |

For the field "I EVSE Min" *Gaai* does not implement the option to change the value,
as the value does not seem to be used by the *Nexxtender Home*.

#### Changing configuration

Clicking on ![edit outline](docs/images/mdi--edit-outline.png) next to "Default Mode"/"Charge Mode" gives the following
dialog screen:

![Default mode dialog](docs/images/DefaultModeDialog.png)

Select the required default charging mode and press OK to confirm.
OK is grayed out if the choice is still "Unknown".
Pressing Cancel does not change anything.

Note that setting the default mode to an ECO variant does not seem to have an effect.
The *Nexxtender* charger does accept it, but if you start charging in ECO mode, it will activate MAX mode anyhow.
The only way the start ECO mode is to explicitly start it with the *Start Charge ECO* button in the screen from
section [Loader](#loader).
This seems to be an issue with the charger; I have the same issue when using the *Nexxtmove* app.

Clicking on ![edit outline](docs/images/mdi--edit-outline.png) next to any of the fields with a value expressed in A
gives the following dialog screen:

![Ampere Slider Dialog](docs/images/AmpereSliderDialog.png)

Select the required current value in Amperes using the slider and press OK to confirm.
Pressing Cancel does not change anything.

Clicking on ![edit outline](docs/images/mdi--edit-outline.png) next to "Weekdays" or "Weekend"
gives the following dialog screen:

![Tou Period Dialog](docs/images/TouPeriodDialog.png)

Change both Start and End times by clicking ![edit outline](docs/images/mdi--edit-outline.png) next to it.
A time picker dialog opens to select a time:

![Tou Time Picker Dialog](docs/images/TouTimePickerDialog.png)

After changing the Start and End times, press OK to confirm.
Pressing Cancel does not change anything.

### Time

The Time card allows to sync the time of the *Nexxtender Home* with the time on the phone.
It is not supported by the *Nexxtender Mobile*.

![Time Card](docs/images/TimeCard.png)

Clicking "Get Time" reads the current time from the *Nexxtender Home*.

![Time Card Get Time](docs/images/TimeCardGetTime.png)

Clicking "Sync Time" writes the current phone time to the *Nexxtender Home*.

### Loader

The Loader card allows to start or switch the *Nexxtender Home* to charge in a specific mode or to stop it.
It is not supported by the *Nexxtender Mobile*.

![Loader Card](docs/images/LoaderCard.png)

Click on a button and the *Nexxtender Home* immediately switches to the corresponding state.

### Badges

The Badges card allows to go to the [Badge list](#badge-list) screen.

![Badges Card](docs/images/BadgesCard.png)

Click on the button to go to the [Badge list](#badge-list) screen.

## Badge list

This screen shows the list of RF badges registered by the charger.
Gaai does not keep a database of registered badges; it only shows the live list as known by the charger.
If there are no badges registered yet, the screen will look like:

![Empty Badge List](docs/images/BadgeListEmpty.png)

If there are already badges registered, the screen will show them:

![Example Badge List](docs/images/BadgeListThree.png)

Each row shows 1 registered badge.
Each badge has an [ISO/IEC 14443-3](https://en.wikipedia.org/wiki/ISO/IEC_14443) UID number that can either be 4,
7 or 10 bytes in length.
Each badge also has a *Charge Type* that indicates if this badge will start charging in MAX mode or DEFAULT mode.
The BLE protocol with the charger does unfortunately not report this *Charge Type*,
so Gaai will report them as UNKNOWN.

In this screen you can

- Register another badge by clicking either the "+ DEFAULT" or "+ MAX" button at the bottom right to register the badge
  with the corresponding *Charge Type*.
  A message will be shown to hold the new badge against the charger.
  The charger will register the new badge and Gaai automatically updates its list of registered badges.
- Remove a device by swiping the card entry that corresponds with the badge to the right.
  A Dialog box will pop up asking you to confirm the delete:

![Badgee Delete Confirmation](docs/images/BadgeDeleteConfirm.png)

Make sure that you are close to the *Nexxtender* charger and that no other device is connected with it over BLE.

# Links

Useful information can be found at

- [Nexxtender Charger Information, Frank HJ Cuypers](https://github.com/FrankHJCuypers/gaai/wiki/Nexxtender-Charger-Information)
- [Analyzing-Bluetooth-Low-Energy-Traffic](https://github.com/FrankHJCuypers/gaai/wiki/Analyzing-Bluetooth-Low-Energy-Traffic)
- [Wireshark Dissector in Lua for Nexxtender charger BLE](https://github.com/FrankHJCuypers/fuut)
- [ESPHome BLE Client for Powerdale Nexxtender EV Charger](https://github.com/geertmeersman/nexxtender)
- [Nexxtmove for Home Assistant](https://github.com/geertmeersman/nexxtmove)
- [Nexxtender Home Bluetooth Controller](https://github.com/toSvenson/nexxtender-ble)
- [Discord chat](https://discord.gg/PTpExQJsWA) related to
  [ESPHome BLE Client for Powerdale Nexxtender EV Charger](https://github.com/geertmeersman/nexxtender)
  and [Nexxtmove for Home Assistant](https://github.com/geertmeersman/nexxtmove)

# License

This project is licensed under the GNU AGPLv3 License. See the [LICENSE](LICENSE) file for details.

# Acknowledgements

- [ESPHome BLE Client for Powerdale Nexxtender EV Charger](https://github.com/geertmeersman/nexxtender)
  showing how to address the Nexxtender Home.
- [NordicSemiconductor Kotlin-BLE-Library uiscanner](https://github.com/NordicSemiconductor/Kotlin-BLE-Library/tree/main/uiscanner)
  for the BLE library.

# Supported Android versions and Nexxtender Home/Mobile firmware versions

In theory *Gaai* should work from API 26 (Android 8) and higher, but these are not all tested.
For the Nexxtender Home firmware versions,
there is insufficient documentation available to assess if it will work for older versions.

The following table shows the version combinations for which it was confirmed that it works.
Basic scenarios seems to work, but further testing is required for other scenarios.
The first row is the main combination used for testing and debugging during development.
The other rows are confirmed by other users.

| Phone Model        | Android API (version) | Nexxtender Home firmware version | Nexxtender Mobile firmware version |
|--------------------|-----------------------|----------------------------------|------------------------------------|
| Google Pixel 6 Pro | 34 (14)               | 2.53.2                           |                                    | 
| Google Pixel 8 Pro | 35 (15)               | 2.53.2                           |                                    | 
| Nokia X10          | 34 (14)               | 2.53.2                           |                                    |
| OnePlus Nord 2     | 33 (13)               | 3.65.0                           |                                    |
| Samsung Galaxy J7  | 27 (8.1)              | 3.65.0                           |                                    |

There are currently no combinations for which it is confirmed that it does not work.

# Disclaimer

The developer

- has no link with Powerdale nor Diego.
- developed this app without prior knowledge of most of the components and tools used
  developing it: Android app development, Android Studio, Gradle, Kotlin, Jetpack compose, BLE, ...
- did not see any perturbing interactions with the *Nexxtmove* app.
  However, if you still need the *Nexxtmove* app to upload charging information (for reimbursement or others),
  be very careful as there is no guarantee that it will not interfere.

*Gaai* is only tested on a the phone versus Nexxtender Home/Mobile versions as indicated in section
[Supported Android versions and Nexxtender Home firmware versions](#supported-android-versions-and-nexxtender-homemobile-firmware-versions)

At this point *Gaai* should be considered an beta app,.
It is currently not tested on sufficient combinations,
so please expect to spend some debugging time.
If despite the preceding warnings, you decide to use *Gaai*, that is on your own responsibility.
But we will do our best to help you. 


