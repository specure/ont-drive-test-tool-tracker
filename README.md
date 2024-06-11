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

## TODO:

For now we have doubled IPERF library just with different .so file to build and different package 
name to be able to create 2 different instances of the IPERF library which we need to perform 
simultaneously upload and download.

Iperf lib implementation provide only singleton so we are not able to perform upload and download 
simultaneously with one package of this library.

We need to find better solution for this with only one copy of library.