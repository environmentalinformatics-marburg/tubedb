# rTubeDB: R connection package for TubeDB

---------------------------------------

### Install R Package
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

[documentation and examples at TubeDB homepage](https://environmentalinformatics-marburg.github.io/tubedb/usage/rpackage)