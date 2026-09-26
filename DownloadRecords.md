# Download Records 

Still to do regarding the downloading of CDR, CCDT, Event and Metrics records.

## Todo

+ Upload CDR records to the https://hub.docker.com/r/geertmeersman/ev-charger-server?
+ Records are currently loaded to text files per download and type.
  + Use database tables instead?

## Where are record files stored?
 As chosen by the user. Example:
/sdcard/Documents/GaaiRecordsLog

These files can be accessed using Android Studio→Device Explorer, while Android Studio is Wi-Fi connected to the 
Android phone.

## Known problems
+ When GaaiDataRecordsCard button "Get remaining records", make sure that Gaai app does not get unactive
  i.e.; screen powers of or alike. Gaai stops downloading records.