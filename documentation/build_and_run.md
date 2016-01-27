Instructions to build and run TubeDB
===

Java 8 and Gradle is required to build and run TubeDB.

Linux
---

Run Gradle with task "\_build\_package". Then in the folder "package" is the compiled TubeDB. There are some shell scripts to manage TubeDB. To execute them you first need to mark scipt-files as executable: chmod +x *.sh

* **clear_import.sh** clears all time series data in TubeDB and imports time series data from data files.

* **server.sh starts** the TubeDB webserver and waits for requests. Terminate it with key "*ctrl-c*". Open a browser and type *http://localhost:8080*

* **explore.sh** opens a desktop gui for time series and meta data inspection.

* **start_server_background.sh** runs the TubeDB webserver in background. For this the application *screen* is required (e.g. run "*sudo apt-get install screen*")

* **stop_server_background.sh** stops a running background server of TubeDB.

* **check_server_background.sh** checks if a TubeDB server is running in background.


Windows
---

Run "*gradle_gui.cmd*" to get a gui window for Gradle. Then run the Gralde task "*_build_package_windows*". Then in the folder "package" is the compiled TubeDB. There are some script files to manage TubeDB.

**server.cmd** starts the TubeDB webserver and waits for requests. Terminate it with key "*ctrl-c*". Open a browser and type *http://localhost:8080*

There are some more windows scripts (*.cmd) which duplicate the functionality described in the Linux section.