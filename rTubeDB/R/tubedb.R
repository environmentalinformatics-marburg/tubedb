#' An S4 class to represent a connection to TubeDB server
#'
#' Connecetion is checked at creation time of TubDB instance. So if url, user or password is not correct an error is thrown.
#'
#' @slot url URL to TubeDB server
#' @slot user user name of account at TubeDB server
#' @slot user user password of account at TubeDB server
#' @author woellauer
#' @seealso \link{regions} \link{region_metadata} \link{query_timeseries} \link{apiGet}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rs <- regions(tubedb)
#' r <- region_metadata(tubedb, "REG1")
#' ts <- query_timeseries(tubedb, plot="PLOT1", sensor="Ta_200", datetimeFormat="POSIXlt")
#' str(ts)
#' @export TubeDB
#' @exportClass TubeDB
TubeDB <- setClass(
  "TubeDB",

  slots = c(
    url = "character",
    user = "character",
    password = "character"
  ),
)

#' constructor for \link{TubeDB-class}
#'
#' This is the constructor.
#' @name TubeDB
#' @export TubeDB
setMethod(
  "initialize",

  "TubeDB",

  function(.Object, url, user, password) {
    if(length(url) == 0 || url == '') {
      stop("missing parameter: url")
    }
    if(!is.character(url) || length(url) != 1) {
      stop("parameter is not character: url")
    }
    if(length(user) == 0 || user == '') {
      stop("missing parameter: user")
    }
    if(!is.character(user) || length(user) != 1) {
      stop("parameter is not character: user")
    }
    if(length(password) == 0 || password == '') {
      stop("missing parameter: password")
    }
    if(!is.character(password) || length(password) != 1) {
      stop("parameter is not character: password")
    }
    .Object@url <- url
    .Object@user <- user
    .Object@password <- password
    apiGet(.Object, "region.json") # check connection
    .Object
  }
)

setMethod("show", "TubeDB", function(object) {
  cat("TubeDB", "\n",
      "  url: ", object@url, "\n",
      "  user:  ", object@user, "\n",
      sep = ""
  )
})

create_query_url <- function(api_url, method, param_list=NULL) {
  method_url <- paste(api_url,method,sep="/")
  if(is.null(param_list)) {
    return(method_url)
  } else {
    pl <- logicalToText(param_list)
    pl <- objectToText(pl)
    pl[lengths(pl) == 0] <- NULL
    pe <- lapply(pl, function(e) { return(URLencode(e, reserved=TRUE))})
    param_pair_list <- paste(names(pe), pe, sep="=")
    param_text <- paste(param_pair_list,collapse="&")
    query_url <- paste(method_url,param_text,sep="?")
    return(query_url)
  }
}

logicalToText <- function(q) {
  lapply(q, function(e) {
    if(is.logical(e)) {
      if(e) {
        return("true")
      }
      return("false")
    }
    return(e)
  })
}

objectToText <- function(q) {
  lapply(q, function(e) {
    return(paste(e))
  })
}

#' direct TubeDB API request
#'
#' Query time series from TubeDB server and create timestamps of specified type. If query could not be processed an error is throwen.
#'
#' @param tubedb instance of TubeDB class
#' @param method API function
#' @param param_list list of query parameters
#' @param processed response is conerted to appropriate objects or raw respone is returned
#' @return response
#' @author woellauer
#' @seealso \link{TubeDB} \link{regions} \link{region_metadata} \link{query_timeseries}
#' @export
apiGet <- function(tubedb, method, param_list=NULL, processed=TRUE) {
  stopifnot(isClass(tubedb, TubeDB))
  auth <- httr::authenticate(user=tubedb@user, password=tubedb@password, type="digest")
  url <- create_query_url(paste0(tubedb@url, "/tsdb"), method, param_list)
  r <- httr::GET(url, auth)
  sc <- httr::status_code(r)
  if(sc != 200) {
    if(sc == 401) {
      stop("Unauthorized: wrong user/password ?")
    }
    stop(sc, httr::content(r))
  }
  if(processed) {
    return(httr::content(r))
  } else {
    return(r)
  }
}

#' regions
#'
#' Get regions (projects) from TubeDB.
#'
#' @param tubedb instance of TubeDB class
#' @return list of regions
#' @author woellauer
#' @seealso \link{TubeDB} \link{region_metadata} \link{query_timeseries}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rs <- regions(tubedb)
#' r <- region_metadata(tubedb, "REG1")
#' ts <- query_timeseries(tubedb, plot="PLOT1", sensor="Ta_200", datetimeFormat="POSIXlt")
#' str(ts)
#' @export
regions <- function(tubedb) {
  stopifnot(isClass(tubedb, TubeDB))
  r <- apiGet(tubedb, "region.json")
  return(r)
}

#' region metadata
#'
#' Get metadata of one region from TubeDB.
#'
#' @param tubedb instance of TubeDB class
#' @param regionID ID of region
#' @return metadata as list
#' @author woellauer
#' @seealso \link{TubeDB} \link{regions} \link{query_timeseries}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rs <- regions(tubedb)
#' r <- region_metadata(tubedb, "REG1")
#' ts <- query_timeseries(tubedb, plot="PLOT1", sensor="Ta_200", datetimeFormat="POSIXlt")
#' str(ts)
#' @export
region_metadata <- function(tubedb, regionID) {
  stopifnot(isClass(tubedb, TubeDB))
  r <- apiGet(tubedb, "metadata.json", list(region=regionID))
  return(r)
}

#' query time series
#'
#' Query time series from TubeDB server and create timestamps of specified type.
#'
#' @param tubedb instance of TubeDB class
#' @param plot character, plot/station
#' @param sensor character or list of character, sensor or list of sensors
#' @param aggregation time steps, one of: raw hour day week month year
#' @param quality quality checks, one of: no, physical, step, empirical
#' @param interpolated apply interpolation, FALSE or TRUE
#' @param year get data from one year only
#' @param month get data of one month of one year only, year neads to be specified
#' @param datetimeFormat character, requested type of timestamps. one of: "character", "POSIXct", "POSIXlt"
#' @param colYear add numeric year column in resulting data.frame (calendar year)
#' @param colMonth add numeric month column in resulting data.frame (1 to 12)
#' @param colDay add numeric day column in resulting data.frame (1 to 31)
#' @return data.frame time series with datetime column + sensor columns
#' @author woellauer
#' @seealso \link{TubeDB} \link{regions} \link{region_metadata} \link{POSIXct} \link{POSIXlt} \link{read_timeseries}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rs <- regions(tubedb)
#' r <- region_metadata(tubedb, "REG1")
#' ts <- query_timeseries(tubedb, plot="PLOT1", sensor="Ta_200", datetimeFormat="POSIXlt")
#' str(ts)
#' @export
query_timeseries <- function(tubedb, plot, sensor, aggregation = "hour", quality = "physical", interpolated = FALSE, year = NULL, month = NULL, datetimeFormat = "character", colYear = FALSE, colMonth = FALSE, colDay = FALSE) {
  stopifnot(isClass(tubedb, TubeDB))
  args <- list(
    plot = plot,
    aggregation = aggregation,
    quality = quality,
    interpolated = interpolated,
    year = year,
    month = month
  )
  names(sensor) <- rep("sensor", length(sensor))
  args <- c(args, sensor)
  r <- apiGet(tubedb, "query_csv", args, FALSE)
  c <- textConnection(rawToChar(r$content))
  t <- read_timeseries(c, datetimeFormat, colYear, colMonth, colDay)
  close(c)
  return(t)
}

#' read time series from file or textConnection
#'
#' Read time series of TubeDB CSV-format from file or textConnection and create timestamps of specified type.
#'
#' @param file file or textConnection
#' @param datetimeFormat requested type of timestamps. one of: "character", "POSIXct", "POSIXlt"
#' @param colYear add numeric year column in resulting data.frame (calendar year)
#' @param colMonth add numeric month column in resulting data.frame (1 to 12)
#' @param colDay add numeric day column in resulting data.frame (1 to 31)
#' @return data.frame time series with datetime column + sensor columns
#' @author woellauer
#' @seealso \link{POSIXct} \link{POSIXlt} \link{query_timeseries}
#' @export
read_timeseries <- function(file, datetimeFormat = "character", colYear = FALSE, colMonth = FALSE, colDay = FALSE) {
  t <- read.table(file, sep = ",", header = TRUE, stringsAsFactors = FALSE, colClasses = c(datetime="character"))
  if(colYear) {
    t$year <- getYear(t$datetime)
  }
  if(colMonth) {
    t$month <- getMonth(t$datetime)
  }
  if(colDay) {
    t$day <- getDay(t$datetime)
  }
  if(datetimeFormat == "character") {
    return(t)
  } else if(datetimeFormat == "POSIXct" || datetimeFormat == "POSIXlt") {
    datetime <- t$datetime
    if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
      datetime <- fillTimestamp(datetime)
    }
    format <- getTimestampFormat(datetime[1])
    if(datetimeFormat == "POSIXct") {
      t$datetime <- as.POSIXct(datetime, format = format)
    } else {
      t$datetime <- as.POSIXlt(datetime, format = format)
    }
    return(t)
  } else {
    stop("unknown datetimeFormat output format: ", datetimeFormat, "   possible values are: character POSIXct POSIXlt")
  }
}

getYear <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  t <- as.POSIXlt(datetime, format = format)
  year <- 1900 + t$year
  return(year)
}

getMonth <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  t <- as.POSIXlt(datetime, format = format)
  month <- t$mon + 1
  return(month)
}

getDay <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  t <- as.POSIXlt(datetime, format = format)
  day <- t$mday
  return(day)
}

fillTimestamp <- function(timestamp) {
  n <- nchar(timestamp[1])
  if(n == 4) {
    return(paste0(timestamp, "-01-01"))
  }
  if(n == 7) {
    return(paste0(timestamp, "-01"))
  }
  return(timestamp)
}

getTimestampFormat <- function(timestamp) {
  n <- nchar(timestamp[1])
  if(n == 10) {
    return("%Y-%m-%d")
  }
  if(n == 13) {
    return("%Y-%m-%dT%H")
  }
  if(n == 16) {
    return("%Y-%m-%dT%H:%M")
  }
  if(n == 19) {
    return("%Y-%m-%dT%H:%M:%S")
  }
  stop("unknown timestamp format of ", timestamp[1])
}

#' write time series  to file or textConnection
#'
#' Write time series data.frame to file or textConnection in TubeDB CSV-format.
#'
#' datetime cloumn need to exist. Resulting CSV-file contains as first row datetime and following columns with sensors.
#' If sensors parameter is missing all columns are included, exept 'year', 'month', 'day' (maybe created by read_timeseries).
#'
#'
#' @param x time series as data.frame
#' @param file file or textConnection
#' @param sensors sensor columns
#' @author woellauer
#' @seealso \link{read_timeseries} \link{query_timeseries}
#' @export
write_timeseries <- function(x, file, sensors = NULL) {
  cn <- colnames(x)
  if(!("datetime" %in% cn)) {
    stop("missing datetime column")
  }
  if(is.null(sensors)) {
    sensors <- cn[!(cn == "datetime" | cn == "year" | cn == "month" | cn == "day")]
  }
  if("datetime" %in% colnames(sensors)) {
    stop("datetime is no sensor")
  }
  if(!(all(sensors %in% cn))) {
    stop("not all sensor columns are in data.frame ", paste(sensors, ""), " ==> ", paste(cn, ""))
  }
  cols <- c("datetime", sensors)
  data <- x[cols]
  write.csv(data, file, quote = FALSE, row.names = FALSE)
}
