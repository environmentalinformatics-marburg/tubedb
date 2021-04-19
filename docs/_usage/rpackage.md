---
title: "R package: rTubeDB"
---

rTubeDB is an R package to connect to TubeDB server.

Installation from [GitHub](https://github.com/environmentalinformatics-marburg/tubedb/tree/master/rTubeDB)
```R
# Install rTubeDB package and automatically install updated versions.
# In some cases a restart of R is needed to work with a updated version of rTubeDB package (in RStudio - Session - Terminate R).
if(!require('remotes')) install.packages("remotes")
if(!require('httr')) install.packages('httr')
remotes::install_github('environmentalinformatics-marburg/tubedb/rTubeDB')
library(rTubeDB)
# get documentation
#?TubeDB
```

Usage example
```R
# load package
library(rTubeDB)

#show rTubeDB package documentation
?TubeDB

# open TubeDB server connection
tubedb <- rTubeDB::TubeDB(url="http://127.0.0.1:8080", user="user", password="password")

# get regions/projects as data.frame
regionDF <- rTubeDB::query_regions(tubedb)

# get plots of one region as data.frame
plotDF <- rTubeDB::query_region_plots(tubedb, "BALE")

# get all sensors of all plots of one region as data.frame
sensorDF <- rTubeDB::query_region_sensors(tubedb, "BALE")

# get climate time series of two sensors over two plots with default processing at full time span as data.frame
tsDF <- rTubeDB::query_timeseries(tubedb, plot=c("BALE001", "BALE002"), sensor=c("Ta_200", "rH_200"), datetimeFormat="POSIXlt")

# show time series
plot(tsDF$datetime, tsDF$Ta_200)
```