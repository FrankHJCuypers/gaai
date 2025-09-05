# Nordic Semiconductor Kotlin-BLE-Library notes

Notes for my understanding of the library.

## Modules

The lib has several modules.

- advertiser
- app_client
- app_server
- client
- client-android
- client-api
- client-mock
- core
- mock
- profile
- scanner
- server
- server-android
- server-api
- server-mock
- test
- uiscanner

For Gaai, the advertiser and the server* modules seem not relevant.
The client* modules and the scanner modules are probably relevant.

## advertiser

## app_client

Example app showing how to implement a client with the library?
Depends on the modules advertiser, scanner, client, server, uiscanner.

Based on the Blinky app.
Why does it depend on advertiser and server, if it is a client app?

It also depends on other nordic libs:

- libs.nordic.ui
- libs.nordic.theme, so includes the Nordic logo etc...
- libs.nordic.navigation, encapsulates a lot of the navigation stuff with NavHost and NavController.
  Not clear what the differences are compared with the Cupcakes course.
- libs.nordic.permissions.ble for instance for requiring bluetooth permission etc...
  Includes the screens needed to ask for permission.
  Seems to use the composable
- libs.nordic.logger

So it uses nordic themes and navigation?

## app_server

## client

Depends on the modules core, client-api, mock, client-android, client-mock

## client-android

Depends on the modules core and client-api

## client-api

Depends on the modules core

## client-mock

Depends on the modules core, mock and client-api

## core

Seems to contain the main part of the library.

Has no dependencies to other modules in the lib.

## mock

Depends on the modules core, client-api and server-api.

## profile

Seems to contain message parsers for several standard BLE profiles.
Interesting as example for Gaai on how to parse Nexxtender Charger messages.

Depends on the module core.

## scanner

Depends on the modules core, mock.

## server

Depends on the modules core, server-api, mock, server-mock, server-android.

## server-android

Depends on the modules core, server-api.

## server-api

Depends on the module core.

## server-mock

Depends on the modules core, mock, server-api.

## test

Junit tests and android tests for he library.

Depends on the modules core, advertiser, scanner, profile, client, client-android, client-mock, client-api, client,
mock, server, server-android, server-mock, server-api.

Also uses hilt for dependency injection.

## uiscanner

Depends on the modules core and scanner