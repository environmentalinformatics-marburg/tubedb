# Example projects

This folder contains several example project configurations and time series source data.

# Global configuration files

sensors defined for all projects `config/sensors.yaml`

user access to one or several projects `realm.properties`

network IPs assigned to users without login prompt `realm_ip.csv`

general (server) configuration `tsdb_paths.ini`

time series data import specification by type and by folder location `import.ini`


*If you activate password protected login in `tsdb_paths.ini`, be sure to change the accounts and passwords in `realm.properties`.*


## Projects

---
## proj1

*Project with four plots in two groups, CSV data files and sensor name renaming.*

**proj1** defined in `config/proj1/region.ini`

plot groups **groupOne**, **groupTwo** defined in `config/proj1/general_stations.ini`

plots **plotRed**, **plotGreen**, **plotYellow**, **plotBlue** defined in `config/proj1/plot_inventory.csv`

logger type **logger1** defined in `config/proj1/logger_type_schema.ini`

CSV time series data files (with file name = plot name) located at **source/example1_csv**, specified in `import.ini`


---
## proj2

*Project with four plots in one group, binary data files.*

**proj2** defined in `config/proj2/region.ini`

one plot group **somePlots** defined in `config/proj2/general_stations.ini`

plots **plot1**, **plot2**, **plot3**, **plot4** defined in `config/proj2/plot_inventory.csv`

renaming of data file sensor names defined in **sensor_translation.ini**

logger types **loggerA**, **loggerB** defined in `config/proj2/logger_type_schema.ini`

UDBF time series data files  (with first folder name = plot name) located at **source/example2_udbf**, specified in `import.ini`


---
## proj3sub1

*Project with two plots in one group, CSV data files.*

**proj3sub1** defined in `config/proj3sub1/region.ini`

one plot group **sub1main** defined in `config/proj3sub1/general_stations.ini`

plots **stationA**, **stationB** defined in `config/proj3sub1/plot_inventory.csv`

logger type **logger3A** defined in `config/proj3sub1/logger_type_schema.ini`

CSV time series data files (with file name = plot name) located at **source/example3a_csv**, specified in `import.ini`

---
## proj3sub2

*Project with two plots in one group, CSV data files with indirection layer between plots and stations.*

*In the other projects plot (geographic location) and station (recording device) are identical. In this project is defined which stations provide time series data to which plots. Multiples stations may be located at one plot, possibly at different intervals in time.*

**proj3sub2** defined in `config/proj3sub2/region.ini`

one plot group **sub2main** defined in `config/proj3sub2/general_stations.ini`

plots **stationC**, **stationD** defined in `config/proj3sub2/plot_inventory.csv`

stations **rec123** (assigned to plot stationC), **rec124** (assigned to plot stationD) defined in `config/proj3sub2/station_inventory.csv`

logger type **logger3B** defined in `config/proj3sub2/logger_type_schema.ini`

CSV time series data file (with column 'plotID'= plot name) located at **source/example3b_csv**, specified in `import.ini`

---
## proj3all

*Project without own plots, but with assigned plots from other projects. The plots are assigned to one group.*

*Groups are associated to exactly one project. Plots are associated to exactly one primary group. Optionally groups can point to assigned plots of other groups (secondary groups for that plot).*

**proj3all** defined in `config/proj3all/region.ini`

one plot group **proj3allmain** defined in `config/proj3all/general_stations.ini`, within that group plots **stationA**, **stationB**, **stationC**, **stationD** (from projects **proj3sub1**, **proj3sub2**) are assigned to that group.

No additional plots are defined for this project.