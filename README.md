### SignalTracker android app

This project meant to be for tracking signal

## Prerequisites:

this is meant to be build on macOS or linux-based systems.

- SWIG
- shell


## Build:

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

Currently we are not able to stop test in execution

- SOLVED

Currently ending and starting phase are not parsed in detail, so we are missing some states of the test
- like starting, connecting, 

Do not stop time on pause

Make autorestart of Iperf test on error

## Potential limitation:
- during the test it seems like android OS make batches and it results in 0.0 bandwidth for some 
period (even few seconds), mostly seen with upload test
- when network switches from one type to another (e.g. LTE to Edge) test is interrupted with error
- starting test can take at least 1s with great conditions, during the testing I experienced 
15s from test start to first bandwidth value acquired from the test
- unable to force user to not turn on wifi adapter