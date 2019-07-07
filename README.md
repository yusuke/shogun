# Shogun
Shogun is a GUI front end for [SDKMAN!](https://sdkman.io).
With Shogun, you can install JDKs, switch between JDKs at ease.
![Shogun](images/screenshot.png "Shogun at work")

## Supported platform
macOS(tested), Linux

## Launch Shogun
Download the latest binary from the [release page](https://github.com/yusuke/shogun/releases/), and double-click to launch the app.

![Shogun](images/appIcon.png "APP Icon")
 
## What does it offer?
Currently you can:
 - See the list of available Java distributions
 - Choose to make a specified distribution / version the default
 - Install or uninstall JDK
 - Reveal the specified JDK Home in Finder
  from OS Task tray.
  
## How to Build 
Java 11 is required to build Shogun. To build Shogun, have Java 11 installed on your mac, and run `$ mvn clean package`
Shogun.app will be located at target/.