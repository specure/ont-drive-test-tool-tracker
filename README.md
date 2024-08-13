# SignalTracker android app

This project meant to be for tracking signal

## Prerequisites:

this is meant to be build on macOS or linux-based systems.

- SWIG
- shell


## Build:

Create or add values to local.properties file:
HOSTNAME=<YOUR_IPERF_HOSTNAME>
FEATURE_SPEED_TEST_ENABLED=<true OR false>
MAPS_API_KEY=<GOOGLE MAPS API KEY>

example:
HOSTNAME=myiperfhostname.com
FEATURE_SPEED_TEST_ENABLED=true
MAPS_API_KEY=dfsfdsggfkg-fgsdgljsgn-gfsgs

in iperf-jni-upload folder

git submodule init
git submodule update

and then in iperf-jni-upload/iperf/src/main/jni/iperf3

git submodule init
git submodule update

in iperf-jni-download folder

git submodule init
git submodule update

and switch to master-neo branch (to get different package name of the lib)

and then in iperf-jni-download/iperf/src/main/jni/iperf3

git submodule init
git submodule update

to get all source files

## Manual:

1. Start the app
2. grant all permissions
3. turn off wifi adapter
4. turn on mobile data
5. turn on location services
6. check if you are able to see network info, location info, temperature on the overview screen
7. tap on the network icon to switch to track mode
8. check if you are able to see network info, position on map, temperature on the track screen
9. tap play button on the bottom to start logging values
10. tap stop and confirm it to stop logging values


## TODO:

For now we have doubled IPERF library just with different .so file to build and different package 
name to be able to create 2 different instances of the IPERF library which we need to perform 
simultaneously upload and download.

Iperf lib implementation provide only singleton so we are not able to perform upload and download 
simultaneously with one package of this library. 
- solved by 2 different package names for the same library and include 2 times 
- TODO: We need to find better solution for this with only one copy of library.

Make autorestart of Iperf test on error

- problems with server side as after error it is not able to connect again
- possible solutions:
  - have multiple instances of server and cycle between them + detect server issue and restart
    server if possible
- observation:
  - When there is a network type switch even between mobile network types (e.g. LTE to HSPDA or EDGE
    or...) and the test survives, the bandwidth is no more limited - maybe if there was technology
    with lower
    speeds, and max bandwidth is not reached then it has like more data buffered and therefore it
    put more data
    while it does not empty that buffer to a bandwidth value set originally (observable thru
    emulator network
    switching)

There is no check during the running test if mobile data are the one used for network traffic (
possible to add)

- added basic monitoring for WIFI network and other types - so in log user will see what type of
  network is used

Currently ending and starting phase are not parsed in detail, so we are missing some states of the
test
- like starting, connecting,


Currently we are not able to stop test in execution
- SOLVED

Do not stop time on pause
- SOLVED


## Potential limitation:
- during the test it seems like android OS make batches and it results in 0.0 bandwidth for some 
period (even few seconds), mostly seen with upload test
- when network switches from one type to another (e.g. LTE to Edge) test can be interrupted with
  error
- starting test can take at least 1s with great conditions, during the testing I experienced 
15s from test start to first bandwidth value acquired from the test
- unable to force user to not turn on wifi adapter

## Expected behavior

- battery updates can be not often, but as soon as temperature change, it is updated (sometime even
  half an hour if environment and execution is)

## Exported values explanation

Time format or local date and time is "yyyy-MM-dd HH:mm:ss"

- **durationMillis** - Test duration from the time user hit start button in milliseconds
- **timestamp** - Local Date and time of the entry
- **timestampRaw** - Unix timestamp of the entry (System.currentTimeMillis())
- **downloadSpeed** - download speed (for unit see **downloadSpeedUnit**)
- **downloadSpeedUnit** - unit of the speed [bits/sec|Mbits/sec|Kbits/sec|Gbits/sec]
- **downloadSpeedTestState** - **INITIALIZING**/**RUNNING**/**ERROR** - **INITIALIZING** when test
  is
  starting and establishing connection to server, **RUNNING** - when there is speed measured and
  data
  are transferred from client to server or vice-versa, **ERROR** - when some error happened,
  description of the error should be in **downloadSpeedTestError** field
- **downloadSpeedTestError** - String of the error, null if no error occurred or there are no
  details
  for error
- **downloadSpeedTestTimestamp** - Local Date and time of the download speed part update time
- **downloadSpeedTestTimestampRaw** - Unix timestamp of the download speed part (
  System.currentTimeMillis())
- **uploadSpeed** - upload speed (for unit see **uploadSpeedUnit**)
- **uploadSpeedUnit** - unit of the speed [bits/sec|Mbits/sec|Kbits/sec|Gbits/sec]
- **uploadSpeedTestState** - **INITIALIZING**/**RUNNING**/**ERROR** - **INITIALIZING** when test is
  starting and establishing connection to server, **RUNNING** - when there is speed measured and
  data
  are transferred from client to server or vice-versa, **ERROR** - when some error happened,
  description of the error should be in **uploadSpeedTestError** field
- **uploadSpeedTestError** - String of the error, null if no error occurred or there are no details
  for error
- **uploadSpeedTestTimestamp** - Local Date and time of the upload speed part update time
- **uploadSpeedTestTimestampRaw** - Unix timestamp of the upload speed part (
  System.currentTimeMillis())
- **latitude** - GPS latitude coordinate
- **longitude** - GPS longitude coordinate
- **locationTimestamp** - Local Date and time of the location part update time
- **locationTimestampRaw** - Unix timestamp of the location part (System.currentTimeMillis())
- **networkType** - Main network type (CELLULAR, WIFI, BLUETOOTH, VPN, ETHERNET, UNKNOWN)
- **mobileNetworkOperator** - Network operator obtained from network
- **mobileNetworkType** - String representation of mobile network type e.g. (LTE, EDGE, HSPDA, ...)
- **signalStrength** - in dBm, primary connected cell signal
- **networkInfoTimestamp** - Local Date and time of the network info part update time
- **networkInfoTimestampRaw** - Unix timestamp of the network info part (System.currentTimeMillis())
- **connectionStatus** - **CONNECTED**/**DISCONNECTED** - **CONNECTED** when network is available
  and
  address is able to be resolved by DNS, **DISCONNECTED** otherwise
- **temperatureCelsius** - Device temperature in Celsius degrees
- **temperatureTimestamp** - Local Date and time of the temperature part update time
- **temperatureTimestampRaw** - Unix timestamp of the temperature part (
  System.currentTimeMillis()) -
do not need to be very fresh if temperature does not change (it should update only if temperature
changes)

## Testing scenarios

## Testing scenarios with manager app

### Testing resetting values

device C setup (signal tracker client):

- installed signal tracker app with default values, all permissions granted
- keep iperf settings same as on C2 to make them collide
- go to active track screen

device M setup (signal tracker manager):

- installed signal tracker manager app with defaults, all permissions granted
- connect to C
- start C with manager app - wait to get running state
- stop with app on C - finish it
- go back to tracking screen on C
- countdown should not start again
- countdown should be on 00:00:00
- should be able to start by M and C

### Showing test error

device C1 setup (signal tracker client 1):

- installed signal tracker app with default values, all permissions granted
- keep iperf settings same as on C2 to make them collide
- go to active track screen

device C2 setup (signal tracker client 2):

- installed signal tracker app with default values, all permissions granted
- keep iperf settings same as on C1 to make them collide
- go to active track screen

device M setup (signal tracker manager):

- installed signal tracker manager app with defaults, all permissions granted
- connect to C1 and to C2
- start C1 with manager app - wait to get running state
- start C2 with manager app - should get error state on C2 