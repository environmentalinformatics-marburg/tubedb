---
title: "Install"
---

TubeDB can be run on Linux (Ubuntu) or Windows.

requirements:

- Java 11 or newer: Oracle JDK or OpenJDK

- (optional, for local desktop GUI) JavaFX 11 or newer

- TubeDB package: 
  - **prebuild package**: (recommended) includes [example data and example configuration](../../usage/example).
    - [download latest ('package.zip')](https://github.com/environmentalinformatics-marburg/tubedb/releases/latest/download/package.zip) 
    - [browse releases and download ('package.zip')](https://github.com/environmentalinformatics-marburg/tubedb/releases)
  - **self build package**: build TubeDB package from source
    - [build instructions](../build)

Linux (Ubuntu)
---

- install **OpenJDK**

`sudo apt-get install openjdk-11-jdk`

- (optional) install **OpenJFX** (needed for desktop GUI)

`sudo apt-get install openjfx`

- (optional) install **ms core fonts** for improved text in visualisation diagrams 

`sudo apt-get install ttf-mscorefonts-installer`

- (optional) install **screen** (needed for running TubeDB as background server)

`sudo apt-get install screen`

- extract package.zip

`unzip package.zip`

- you may need to mark shell scripts as executable in root folder of package

`chmod +X *.sh`

- proceed to [run](../run)

Windows
---

- install [Oracle Java 11](https://www.oracle.com/java/technologies/downloads/#java11-windows) or [other Java runtime](https://adoptopenjdk.net)

- extract package.zip

- proceed to [run](../run)