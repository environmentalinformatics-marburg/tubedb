---
title: "Example package"
---

The folder *example* contains a package of TubeDB with some time series source data files. It is runnable with Linux or Windows.

Linux
---

To execute the script files in the example folder you first need to mark script-files as executable. Type: `chmod +x *.sh`

With `./clear_import.sh` time series source data files will be imported into TsDB.

Then type `./server.sh` to run the webserver. Now you can open a browser and type `http://localhost:8080` to view the web interface of TubeDB. To terminate the TubeDB server press ctrl-c.

Windows
---

Files in the example folder that end with `.cmd` are windows script files.

Double-click at `clear_import.cmd` and time series source data files will be imported into TsDB. Wait until its done.

Double-click at `server.cmd` to run the webserver. Now you can open a browser and type `http://localhost:8080` to view the web interface of TubeDB. To terminate the TubeDB server press ctrl-c or close the console-window.

