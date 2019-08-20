---
title: "R package: rTubeDB"
---

rTubeDB is an R package to connect to TubeDB server.

Installation from [GitHub](https://github.com/environmentalinformatics-marburg/tubedb/tree/master/rTubeDB)
```R
install.packages("remotes")
install.packages("httr")
remotes::install_github("environmentalinformatics-marburg/tubedb/rTubeDB")
library(rTubeDB)
```

Usage example
```R
library(rTubeDB)
tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
rs <- regions(tubedb)
r <- region_metadata(tubedb, "REG1")
ts <- query_timeseries(tubedb, plot="PLOT1", sensor="Ta_200", datetimeFormat="POSIXlt")
str(ts)

#show rTubeDB package documentation
?TubeDB
```