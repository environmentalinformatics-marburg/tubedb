---
title: "Build"
---

**Note**: You don't need to build TubeDB. Prebuild packages are available. (see [install](../install))

TubeDB can be build on Linux (Ubuntu) or Windows. 

requirements:

- (optional) git

- Gradle

- Java 8 or newer: Oracle JDK or OpenJDK

- JavaFX 8 or newer: included in (some) Oracle JDKs or OpenJFX

---
## Linux (Ubuntu)

update Ubuntu package list

`sudo apt-get update`

update all installed packages

`sudo apt-get upgrade`

(optional) install **git**

`sudo apt-get install git`

install **Gradle**

`sudo apt install gradle`

install **OpenJDK**

`sudo apt-get install openjdk-8-jdk`

check correct installation of JDK

`javac --version`

install **OpenJFX**

`sudo apt-get install openjfx`

**(option 1)** clone TubeDB source with git

`git clone --depth 1  https://github.com/environmentalinformatics-marburg/tubedb`

**(option 2)** download [latest source archive](https://github.com/environmentalinformatics-marburg/tubedb/archive/master.zip) and extract it

**(option 3)** download [specific source archive](https://github.com/environmentalinformatics-marburg/tubedb/releases) version  and extract it

change to TubeDB source root (**there needs to be the file `build.gradle`**)

`cd tubedb`

install current Gradle wrapper (you may need to mark shell scripts as executable `chmod +X *.sh`)

`./gradlew`

**Note**: To ensure correct Gralde version you need to use `./gradlew` and not '*gradle*' for your Gradle commands.

**Troubleshooting**: If Gradle prints the error "*unable to create native thread: possibly out of memory or process/resource limits reached*" try to run with `--no-daemon`  e.g.  `./gradlew --no-daemon _build_package_linux_windows`

run one of following Gradle commands for desired target platform: Linux or Windows or both

- `./gradlew _build_package`

- `./gradlew _build_package_windows`

- `./gradlew _build_package_linux_windows`

the build is then created in subfolder `package`

`cd package`

for Linux target you may need to mark shell scripts as executable in folder `package`:

`chmod +X *.sh`

proceed to [install](../install)

---
## Windows


install [gradle](https://gradle.org/install/)

install [Oracle Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (JavaFX is included)

download [latest source archive](https://github.com/environmentalinformatics-marburg/tubedb/archive/master.zip) and extract it

on command line change to TubeDB source root (there needs to be the file `build.gradle`)

run one of following gradle commands for desired target platform: Linux or Windows or both

- `gradle _build_package`

- `gradle _build_package_windows`

- `gradle _build_package_linux_windows`

the build is distributed in subfolder `package`

`cd package`

proceed to [install](../install)
