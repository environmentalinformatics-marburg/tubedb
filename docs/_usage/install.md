---
title: "Install"
---

TubeDB can be run on Linux (Ubuntu) or Windows.

requirements:

- TubeDB distribution package: self [build](../build) or prebuild at [example](../../documentation/example) subfolder in [git repository](https://github.com/environmentalinformatics-marburg/tubedb/tree/master/example)

- Java 8 or newer: Oracle JDK or OpenJDK

- JavaFX 8 or newer: included in (some) Oracle JDKs or OpenJFX

Linux (Ubuntu)
---

install **OpenJDK**

`sudo apt-get install openjdk-8-jdk`

install **OpenJFX**

`sudo apt-get install openjfx`

(optional) install **ms core fonts** for improved text in visualisation diagrams 

`sudo apt-get install ttf-mscorefonts-installer`

(optional) install **screen** for running TubeDB as background server

`sudo apt-get install screen`

you may need to mark shell scripts as executable in root folder of distribution package

`chmod +X *.sh`

proceed to [run](../run)

Windows
---

install [Oracle Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (JavaFX is included)

proceed to [run](../run)