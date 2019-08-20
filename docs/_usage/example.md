---
title: "Example package"
---

TubeDB repository ([download](https://github.com/environmentalinformatics-marburg/tubedb/archive/master.zip)) includes a prebuild example package in subfolder [`example`](https://github.com/environmentalinformatics-marburg/tubedb/tree/master/example). It is runnable on Linux or Windows.

Make sure to perform prerequisites mentioned in [install](../../usage/install) before running TubeDB!

Example package may serve as starting point for your own climate data projects. You may modify step-by-step configuration files to fit you use case. 

---
### Running on Linux

To execute the script files in the example folder you first need to mark script-files as executable. Type: `chmod +x *.sh`

With `./clear_import.sh` time series source data files will be imported into TubeDB. Wait until its done.

Then type `./server.sh` to run the webserver. Now you can open a browser and type `http://localhost:8080` to view the web interface of TubeDB. To terminate the TubeDB server press ctrl-c.

(for details see [run](../../usage/run))

---
### Running on Windows

Files in the example folder that end with `.cmd` are windows script files.

Double-click at `clear_import.cmd` and time series source data files will be imported into TsDB. Wait until its done.

Double-click at `server.cmd` to run the webserver (and leave created console-window open). Now you can open a browser and type `http://localhost:8080` to view the web interface of TubeDB. To terminate the TubeDB server press ctrl-c or close the console-window.

(for details see [run](../../usage/run))

---
### Relevant configuration files

- general TubeDB configuration: `tsdb_paths.ini` ([docu](https://github.com/environmentalinformatics-marburg/tubedb/blob/master/tsdb_paths.ini))

- TubeDB access control: `realm.properties` and `realm_ip.csv` ([docu](https://github.com/environmentalinformatics-marburg/tubedb/blob/master/realm.properties))

- Data source configuration: `import.ini` ([docu](../../configuration/import))

- Sensor configuration: `config/sensors.yaml` ([docu](../../configuration/sensor))

- Project main configuration: `config/proj1/region.ini` ([docu](../../configuration/project))

- Project station/plot group configuration: `config/proj1/general_stations.ini` ([docu](../../configuration/project))

- Project station/plot group configuration: `config/proj1/plot_inventory.csv`  ([docu](../../configuration/project))

- Project logger configuration: `config/proj1/logger_type_schema.ini`  ([docu](../../configuration/project))
