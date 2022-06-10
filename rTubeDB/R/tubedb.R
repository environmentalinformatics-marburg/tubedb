#' An S4 class to represent a connection to TubeDB server
#'
#' Connecetion is checked at creation time of TubDB instance. So if url, user or password is not correct an error is thrown.
#'
#' @slot url URL to TubeDB server
#' @slot user user name of account at TubeDB server
#' @slot user user password of account at TubeDB server
#' @author woellauer
#' @seealso \link{query_timeseries} \link{query_regions} \link{query_region_plots} \link{query_region_sensors}
#' @examples
#' # load package
#' library(rTubeDB)
#'
#' # open TubeDB server connection
#' tubedb <- rTubeDB::TubeDB(url="http://127.0.0.1:8080", user="user", password="password")
#'
#' # get regions/projects as data.frame
#' regionDF <- rTubeDB::query_regions(tubedb)
#'
#' # get plots of one region as data.frame
#' plotDF <- rTubeDB::query_region_plots(tubedb, "BALE")
#'
#' # get all sensors of all plots of one region as data.frame
#' sensorDF <- rTubeDB::query_region_sensors(tubedb, "BALE")
#'
#' # get climate time series of two sensors over two plots with default processing at full time span as data.frame
#' tsDF <- rTubeDB::query_timeseries(tubedb, plot=c("BALE001", "BALE002"), sensor=c("Ta_200", "rH_200"), datetimeFormat="POSIXlt")
#'
#' # show time series
#' plot(tsDF$datetime, tsDF$Ta_200)
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

#' direct TubeDB API request (internal)
#'
#' Query time series from TubeDB server and create timestamps of specified type. If query could not be processed an error is throwen.
#'
#' @param tubedb instance of TubeDB class
#' @param method API function
#' @param param_list list of query parameters
#' @param processed response is converted to appropriate objects or raw respone is returned
#' @return response
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions} \link{query_region_plots} \link{query_region_sensors}
#' @export
apiGet <- function(tubedb, method, param_list=NULL, processed=TRUE) {
  stopifnot(isClass(tubedb, TubeDB))
  auth <- httr::authenticate(user=tubedb@user, password=tubedb@password, type="digest")
  url <- create_query_url(paste0(tubedb@url, "/tsdb"), method, param_list)
  r <- httr::GET(url, auth)
  sc <- httr::status_code(r)
  #print(paste("sc", sc))
  if(sc != 200) {
    if(sc == 401) {
      stop("Unauthorized: wrong user/password ?")
    }
    stop(sc, r)
  }
  if(processed) {
    return(httr::content(r))
  } else {
    return(r)
  }
}

#' convert NULL value to NA value (internal)
#'
#' @export
NULLtoNA <- function(v) {
  if(is.null(v)) return(NA) else return(v)
}

#' query regions list
#'
#' Get regions (projects) from TubeDB as list of regions
#'
#' @param tubedb instance of TubeDB class
#' @return list of regions
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions} \link{query_region_metadata}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_regions_list <- function(tubedb) {
  stopifnot(isClass(tubedb, TubeDB))
  r <- apiGet(tubedb, "region.json")
  return(r)
}

#' query regions
#'
#' Get regions (projects) from TubeDB as data.frame of regions
#'
#' @param tubedb instance of TubeDB class
#' @return data.frame of regions
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_region_metadata}
#' @examples
#' library(rTubeDB)
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_regions <- function(tubedb) {
  rs <- query_regions_list(tubedb)
  id <- sapply(rs, function(r) {NULLtoNA(r$id)})
  name <- sapply(rs, function(r) {NULLtoNA(r$name)})
  view_year_range_start <- sapply(rs, function(r) {if(is.null(r$view_year_range)) NA else NULLtoNA(r$view_year_range$start)})
  view_year_range_end <- sapply(rs, function(r) {if(is.null(r$view_year_range)) NA else NULLtoNA(r$view_year_range$end)})
  df <- data.frame(id=id, name=name, view_year_range_start=view_year_range_start, view_year_range_end=view_year_range_end, stringsAsFactors = FALSE)
  return(df)
}

#' query region metadata
#'
#' Get metadata of one region (projects) from TubeDB as list
#'
#' @param tubedb instance of TubeDB class
#' @return list of region metadata
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_metadata <- function(tubedb, regionID) {
  stopifnot(isClass(tubedb, TubeDB))
  r <- apiGet(tubedb, "metadata.json", list(region=regionID))

  if(!is.null(r$general_stations)) {
    names(r$general_stations) <- sapply(r$general_stations, function(general_station) {return(general_station$id)})
  }
  if(!is.null(r$plots)) {
    names(r$plots) <- sapply(r$plots, function(plot) {return(plot$id)})
  }
  if(!is.null(r$sensors)) {
    names(r$sensors) <- sapply(r$sensors, function(sensor) {return(sensor$id)})
  }
  if(!is.null(r$stations)) {
    names(r$stations) <- sapply(r$stations, function(station) {return(station$id)})
  }

  return(r)
}

#' query region basic information
#'
#' Get basic information of one region (projects) from TubeDB as list
#'
#' @param tubedb instance of TubeDB class
#' @return list of region basic information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_info_list <- function(tubedb, regionID) {
  rm <- query_region_metadata(tubedb, regionID)
  r <- rm$region
  return(r)
}

#' query region basic information
#'
#' Get basic information of one region (projects) from TubeDB as data.frame
#'
#' @param tubedb instance of TubeDB class
#' @return data.frame of region basic information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_info <- function(tubedb, regionID) {
  r <- query_region_info_list(tubedb, regionID)
  vyrs <- if(is.null(r$view_year_range)) NA else NULLtoNA(r$view_year_range$start)
  vyre <- if(is.null(r$view_year_range)) NA else NULLtoNA(r$view_year_range$end)
  dgs <- if(is.null(r$default_general_station)) NA else NULLtoNA(r$default_general_station)
  df <- data.frame(id = r$id, name = r$name, view_year_range_start = vyrs, view_year_range_end = vyre, default_general_station = dgs, stringsAsFactors = FALSE)
  return(df)
}

#' query "general station" (group of plots) information
#'
#' Get "general station" (group of plots) information of one region (projects) from TubeDB as list
#'
#' @param tubedb instance of TubeDB class
#' @return list of "general station" information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_general_stations_list <- function(tubedb, regionID) {
  rm <- query_region_metadata(tubedb, regionID)
  gsl <- rm$general_stations
  return(gsl)
}

#' query "general station" (group of plots) information
#'
#' Get "general station" (group of plots) information of one region (projects) from TubeDB as data.frame
#'
#' @param tubedb instance of TubeDB class
#' @return data.frame of "general station" information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_general_stations <- function(tubedb, regionID) {
  gsl <- query_region_general_stations_list(tubedb, regionID)
  id <- sapply(gsl, function(r) {NULLtoNA(r$id)})
  name <- sapply(gsl, function(r) {NULLtoNA(r$name)})
  view_year_range_start <- sapply(gsl, function(r) {if(is.null(r$view_year_range)) NA else NULLtoNA(r$view_year_range$start)})
  view_year_range_end <- sapply(gsl, function(r) {if(is.null(r$view_year_range)) NA else NULLtoNA(r$view_year_range$end)})
  df <- data.frame(id=id, name=name, view_year_range_start=view_year_range_start, view_year_range_end=view_year_range_end, stringsAsFactors = FALSE)
  return(df)
}

#' query plots information
#'
#' Get plots information of one region (projects) from TubeDB as list
#'
#' @param tubedb instance of TubeDB class
#' @return list of plots information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_plots_list <- function(tubedb, regionID) {
  rm <- query_region_metadata(tubedb, regionID)
  gsl <- rm$plots
  return(gsl)
}

#' query plots information
#'
#' Get plots information of one region (projects) from TubeDB as data.frame
#'
#' @param tubedb instance of TubeDB class
#' @return data.frame of plots information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_plots <- function(tubedb, regionID) {
  pl <- query_region_plots_list(tubedb, regionID)
  id <- sapply(pl, function(p) {NULLtoNA(p$id)})
  general_station <- sapply(pl, function(p) {NULLtoNA(p$general_station)})
  logger_type <- sapply(pl, function(p) {NULLtoNA(p$logger_type)})
  vip <- sapply(pl, function(p) {NULLtoNA(p$vip)})
  latitude <- sapply(pl, function(p) {NULLtoNA(p$latitude)})
  longitude <- sapply(pl, function(p) {NULLtoNA(p$longitude)})
  elevation <- sapply(pl, function(p) {NULLtoNA(p$elevation)})
  df <- data.frame(id=id, general_station=general_station, logger_type=logger_type, vip=vip, latitude=latitude, longitude=longitude, elevation=elevation, stringsAsFactors = FALSE)
  return(df)
}

#' query metadata of one plot
#'
#' Get metadata of one plot of one region (project) from TubeDB as list
#'
#' @param tubedb instance of TubeDB class
#' @return list
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsdf <- query_regions(tubedb)
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rpmdf <- query_region_plot_metadata(tubedb, "BALE", "BALE001")
#' rpsdf <- query_region_plot_sensors(tubedb, "BALE", "BALE001")
#' @export
query_region_plot_metadata <- function(tubedb, regionID, plotID) {
  rm <- query_region_metadata(tubedb, regionID)
  pm <- rm$plots[[plotID]]
  return(pm)
}

#' query sensor names of one plot
#'
#' Get sensor names of one plot of one region (project) from TubeDB as vector
#'
#' @param tubedb instance of TubeDB class
#' @return vector
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsdf <- query_regions(tubedb)
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rpmdf <- query_region_plot_metadata(tubedb, "BALE", "BALE001")
#' rpsdf <- query_region_plot_sensors(tubedb, "BALE", "BALE001")
#' @export
query_region_plot_sensors <- function(tubedb, regionID, plotID) {
  rm <- query_region_metadata(tubedb, regionID)
  pm <- rm$plots[[plotID]]
  s <- unlist(pm$sensor_names)
  return(s)
}

#' query sensors information
#'
#' Get sensor information of one region (projects) from TubeDB as list
#'
#' @param tubedb instance of TubeDB class
#' @return list of plots information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_sensors_list <- function(tubedb, regionID) {
  rm <- query_region_metadata(tubedb, regionID)
  sl <- rm$sensors
  return(sl)
}

#' query sensors information
#'
#' Get sensor information of one region (projects) from TubeDB as data.frame
#'
#' @param tubedb instance of TubeDB class
#' @return data.frame of plots information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_sensors <- function(tubedb, regionID) {
  sl <- rTubeDB::query_region_sensors_list(tubedb, regionID)
  id <- sapply(sl, function(s) {NULLtoNA(s$id)})
  description <- sapply(sl, function(s) {NULLtoNA(s$description)})
  unit_description <- sapply(sl, function(s) {NULLtoNA(s$unit_description)})
  raw <- sapply(sl, function(s) {NULLtoNA(s$raw)})
  derived <- sapply(sl, function(s) {NULLtoNA(s$derived)})
  internal <- sapply(sl, function(s) {NULLtoNA(s$internal)})
  df <- data.frame(id=id, description=description, unit_description=unit_description, raw=raw, derived=derived, internal=internal, stringsAsFactors = FALSE)
  return(df)
}

#' query station information
#'
#' Get station information of one region (projects) from TubeDB as list. One plot (a location) may contain several stations.
#'
#' @param tubedb instance of TubeDB class
#' @return list of station information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_stations_list <- function(tubedb, regionID) {
  rm <- query_region_metadata(tubedb, regionID)
  sl <- rm$stations
  return(sl)
}

#' query station information
#'
#' Get station information of one region (projects) from TubeDB as data.frame. One plot (a location) may contain several stations.
#'
#' @param tubedb instance of TubeDB class
#' @return data.frame of station information
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions_list} \link{query_regions}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rsl <- query_regions_list(tubedb)
#' rsdf <- query_regions(tubedb)
#' rml <- query_region_metadata(tubedb, "BALE")
#' ril <- query_region_info_list(tubedb, "BALE")
#' ridf <- query_region_info(tubedb, "BALE")
#' rgsl <- query_region_general_stations_list(tubedb, "BALE")
#' rgsdf <- query_region_general_stations(tubedb, "BALE")
#' rpl <- query_region_plots_list(tubedb, "BALE")
#' rpdf <- query_region_plots(tubedb, "BALE")
#' rsrl <- query_region_sensors_list(tubedb, "BALE")
#' rsrdf <- query_region_sensors(tubedb, "BALE")
#' rstl <- query_region_stations_list(tubedb, "BALE")
#' rstdf <- query_region_stations(tubedb, "BALE")
#' @export
query_region_stations <- function(tubedb, regionID) {
  sl <- rTubeDB::query_region_stations_list(tubedb, regionID)
  id <- sapply(sl, function(s) {NULLtoNA(s$id)})
  logger_type <- sapply(sl, function(s) {NULLtoNA(s$logger_type)})
  df <- data.frame(id=id, logger_type=logger_type, stringsAsFactors = FALSE)
  return(df)
}



#' query time series
#'
#' Query time series as data.frame at one plot of several sensors from TubeDB server and create timestamps of specified type.
#'
#' Mandatory parameters: \strong{tubedb}, \strong{plot}, \strong{sensor}
#'
#' @section Time span of time series:
#'
#' no time parameter -> full avaiable timespan
#'
#' \code{year} parameter -> one full year
#'
#' \code{year} and \code{month} parameter -> one full month
#'
#' \code{start} and \code{end} parameter -> exact time span
#'
#' Format of \code{start} and \code{end} parameter: (character) yyyy-MM-ddTHH:mm, abbreviations allowed. e.g.  2018-12-31T23:59, 2018-12-31T23, 2018-12-31, 2018-12, 2018. '\code{end}' abbreviations are filled, e.g. 2018-01 -> 2018-01-31T23:59
#'
#'
#'
#'
#' @param tubedb instance of TubeDB class
#' @param plot character or list of character, one plot or list of plots
#' @param sensor character or list of character, one sensor or list of sensors
#' @param aggregation time steps, one of: raw hour day week month year
#' @param quality quality checks, one of: no, physical, step, empirical
#' @param interpolated apply interpolation, FALSE or TRUE
#' @param start start time of timeseries
#' @param end end time of timeseries
#' @param year get data from one full year only (You may use this 'year' parameter OR 'start', 'end' parameters)
#' @param month get data of one full month of one year only, parameter year neads to be specified (You may use this 'month' parameter OR 'start'/'end' parameters)
#' @param day get data of one full day of one month only, parameter month neads to be specified (You may use this 'day' parameter OR 'start'/'end' parameters)
#' @param casted all plot-sensor pairs in one row per timestamp, e.g. columns plot1.sensor1, plot2.sensor1, plot1.sensor2
#' @param spatial_aggregated combine all values of one sensor over all plots to one value per timestamp by mean
#' @param datetimeFormat character, requested type of timestamps. one of: "character", "POSIXct", "POSIXlt"
#' @param colPlot add column with plot name
#' @param colYear add numeric year column in resulting data.frame (calendar year)
#' @param colMonth add numeric month column in resulting data.frame (1 to 12)
#' @param colDay add numeric day column in resulting data.frame (1 to 31)
#' @param colHour add numeric hour column in resulting data.frame (0 to 23)
#' @param colWeek add numeric day of week column in resulting data.frame (1 to 53) (ISO week based)
#' @param colDayOfWeek add numeric day of week column in resulting data.frame (1 to 7) (ISO week based)
#' @return data.frame time series with datetime column + sensor columns
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_regions} \link{query_region_plots} \link{query_region_sensors} \link{POSIXct} \link{POSIXlt} \link{read_timeseries} \link{query_diagram} \link{query_heatmap}
#' @examples
#' # load package
#' library(rTubeDB)
#'
#' # open TubeDB server connection
#' tubedb <- rTubeDB::TubeDB(url="http://127.0.0.1:8080", user="user", password="password")
#'
#' # get regions/projects as data.frame
#' regionDF <- rTubeDB::query_regions(tubedb)
#'
#' # get plots of one region as data.frame
#' plotDF <- rTubeDB::query_region_plots(tubedb, "BALE")
#'
#' # get all sensors of all plots of one region as data.frame
#' sensorDF <- rTubeDB::query_region_sensors(tubedb, "BALE")
#'
#' # get climate time series of two sensors over two plots with default processing at full time span as data.frame
#' tsDF <- rTubeDB::query_timeseries(tubedb, plot=c("BALE001", "BALE002"), sensor=c("Ta_200", "rH_200"), datetimeFormat="POSIXlt")
#'
#' # show time series
#' plot(tsDF$datetime, tsDF$Ta_200)
#' @export
query_timeseries <- function(tubedb, plot, sensor, aggregation = "hour", quality = "physical", interpolated = FALSE, start = NULL, end = NULL, year = NULL, month = NULL, day = NULL, casted = FALSE, spatial_aggregated = FALSE, datetimeFormat = "character", colYear = FALSE, colPlot = TRUE, colMonth = FALSE, colDay = FALSE, colHour = FALSE, colWeek = FALSE, colDayOfWeek = FALSE) {
  stopifnot(isClass(tubedb, TubeDB))
  args <- list(
    aggregation = aggregation,
    quality = quality,
    interpolated = interpolated,
    start = start,
    end = end,
    year = year,
    month = month,
    day = day,
    casted = casted,
    spatial_aggregated = spatial_aggregated,
    col_plot = colPlot
  )
  names(plot) <- rep("plot", length(plot))
  names(sensor) <- rep("sensor", length(sensor))
  args <- c(plot, sensor, args)
  r <- apiGet(tubedb, "query_csv", args, FALSE)
  c <- textConnection(rawToChar(r$content))
  t <- read_timeseries(c, datetimeFormat, colYear, colMonth, colDay, colHour, colWeek, colDayOfWeek)
  close(c)
  return(t)
}

#' query diagram of time series
#'
#' Query time series as pixel-image ('nativeRaster') at one plot of several sensors from TubeDB server and create timestamps of specified type.
#'
#' Mandatory parameters: \strong{tubedb}, \strong{plot}, \strong{sensor}
#'
#' @section Time span of time series:
#'
#' no time parameter -> full avaiable timespan
#'
#' \code{year} parameter -> one full year
#'
#' \code{year} and \code{month} parameter -> one full month
#'
#' \code{start} and \code{end} parameter -> exact time span
#'
#' Format of \code{start} and \code{end} parameter: (character) yyyy-MM-ddTHH:mm, abbreviations allowed. e.g.  2018-12-31T23:59, 2018-12-31T23, 2018-12-31, 2018-12, 2018. '\code{end}' abbreviations are filled, e.g. 2018-01 -> 2018-01-31T23:59
#'
#'
#'
#'
#' @param tubedb instance of TubeDB class
#' @param plot character, plot/station
#' @param sensor character sensor name
#' @param aggregation time steps, one of: raw hour day week month year
#' @param quality quality checks, one of: no, physical, step, empirical
#' @param interpolated apply interpolation, FALSE or TRUE
#' @param start start time of timeseries
#' @param end end time of timeseries
#' @param year get data from one full year only (You may use this 'year' parameter OR 'start', 'end' parameters)
#' @param month get data of one full month of one year only, parameter year neads to be specified (You may use this 'month' parameter OR 'start'/'end' parameters)
#' @param day get data of one full day of one month only, parameter month neads to be specified (You may use this 'day' parameter OR 'start'/'end' parameters)
#' @param boxplot draw xy-diagram or boxplot
#' @param width pixel width of resulting image
#' @param height pixel height of resulting image
#' @return nativeRaster
#' @author woellauer
#' @seealso \link{TubeDB} \link{query_timeseries} \link{query_regions} \link{query_region_plots} \link{query_region_sensors} \link{query_heatmap}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rs <- regions(tubedb)
#' r <- region_metadata(tubedb, "REG1")
#' img <- query_diagram(tubedb, plot="PLOT1", sensor="Ta_200", start="2017", end="2019")
#' grid::grid.raster(img)
#' @export
query_diagram <- function(tubedb, plot, sensor, aggregation = "hour", quality = "physical", interpolated = FALSE, start = NULL, end = NULL, year = NULL, month = NULL, day = NULL, boxplot = FALSE, width = 1000, height = 200) {
  stopifnot(isClass(tubedb, TubeDB))
  args <- list(
    plot = plot,
    aggregation = aggregation,
    quality = quality,
    interpolated = interpolated,
    start = start,
    end = end,
    year = year,
    month = month,
    day = day,
    boxplot = boxplot,
    width = width,
    height = height
  )
  names(plot) <- rep("plot", length(plot))
  names(sensor) <- rep("sensor", length(sensor))
  args <- c(plot, sensor, args)
  r <- apiGet(tubedb, "query_image", args, FALSE)
  img <- png::readPNG(r$content, native = TRUE, info = TRUE)
  return(img)
}




#' query heatmap of time series
#'
#' Query time series as heatmap pixel-image ('nativeRaster') at one plot of several sensors from TubeDB server and create timestamps of specified type.
#'
#' Mandatory parameters: \strong{tubedb}, \strong{plot}, \strong{sensor}
#'
#' @section Time span of time series:
#'
#' no time parameter -> full avaiable timespan
#'
#' \code{year} parameter -> one full year
#'
#' \code{year} and \code{month} parameter -> one full month
#'
#' \code{start} and \code{end} parameter -> exact time span
#'
#' Format of \code{start} and \code{end} parameter: (character) yyyy-MM-ddTHH:mm, abbreviations allowed. e.g.  2018-12-31T23:59, 2018-12-31T23, 2018-12-31, 2018-12, 2018. '\code{end}' abbreviations are filled, e.g. 2018-01 -> 2018-01-31T23:59
#'
#'
#'
#'
#' @param tubedb instance of TubeDB class
#' @param plot character, plot/station
#' @param sensor character sensor name
#' @param quality quality checks, one of: no, physical, step, empirical
#' @param interpolated apply interpolation, FALSE or TRUE
#' @param start start time of timeseries
#' @param end end time of timeseries
#' @param year get data from one full year only (You may use this 'year' parameter OR 'start', 'end' parameters)
#' @param month get data of one full month of one year only, parameter year neads to be specified (You may use this 'month' parameter OR 'start'/'end' parameters)
#' @param day get data of one full day of one month only, parameter month neads to be specified (You may use this 'day' parameter OR 'start'/'end' parameters)
#' @param by_year draw all data in one row or draw one row per year
#' @param time_scale draw a time scale
#' @return nativeRaster
#' @author woellauer
#' @seealso \link{TubeDB} \link{TubeDB} \link{query_timeseries} \link{query_regions} \link{query_region_plots} \link{query_region_sensors} \link{query_diagram}
#' @examples
#' library(rTubeDB)
#' tubedb <- TubeDB(url="http://localhost:8080", user="user", password="password")
#' rs <- regions(tubedb)
#' r <- region_metadata(tubedb, "REG1")
#' img <- query_heatmap(tubedb, plot="PLOT1", sensor="Ta_200", start="2017", end="2019")
#' grid::grid.raster(img)
#' @export
query_heatmap <- function(tubedb, plot, sensor, quality = "physical", interpolated = FALSE, start = NULL, end = NULL, year = NULL, month = NULL, day = NULL, by_year = TRUE, time_scale = TRUE) {
  stopifnot(isClass(tubedb, TubeDB))
  args <- list(
    plot = plot,
    quality = quality,
    interpolated = interpolated,
    start = start,
    end = end,
    year = year,
    month = month,
    day = day,
    by_year = by_year,
    time_scale = time_scale
  )
  names(plot) <- rep("plot", length(plot))
  names(sensor) <- rep("sensor", length(sensor))
  args <- c(plot, sensor, args)
  r <- apiGet(tubedb, "query_heatmap", args, FALSE)
  img <- png::readPNG(r$content, native = TRUE, info = TRUE)
  return(img)
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
#' @param colHour add numeric hour column in resulting data.frame (0 to 23)
#' @param colWeek add numeric day of week column in resulting data.frame (1 to 53) (ISO week based)
#' @param colDayOfWeek add numeric day of week column in resulting data.frame (1 to 7) (ISO week based)
#' @return data.frame time series with datetime column + sensor columns
#' @author woellauer
#' @seealso \link{POSIXct} \link{POSIXlt} \link{query_timeseries} \link{TubeDB} \link{query_regions} \link{query_region_plots} \link{query_region_sensors}
#' @export
read_timeseries <- function(file, datetimeFormat = "character", colYear = FALSE, colMonth = FALSE, colDay = FALSE, colHour = FALSE, colWeek = FALSE, colDayOfWeek = FALSE) {
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
  if(colHour) {
    t$hour <- getHour(t$datetime)
  }
  if(colDayOfWeek) {
    t$dayOfWeek <- getDayOfWeek(t$datetime)
  }
  if(colWeek) {
    t$week <- getWeek(t$datetime)
  }
  if(datetimeFormat == "character") {
    return(t)
  } else if(datetimeFormat == "POSIXct" || datetimeFormat == "POSIXlt") {
    datetime <- t$datetime
    if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7 || nchar(datetime[1]) == 8) { # year or month or week
      datetime <- fillTimestamp(datetime)
    }
    format <- getTimestampFormat(datetime[1])
    if(datetimeFormat == "POSIXct") {
      if(format == '%Y-W%V-%d') {
        date <- ISOweek::ISOweek2date(datetime)
        datetime <- as.character(date)
        format <- '%Y-%m-%d'
      }
      t$datetime <- as.POSIXct(datetime, format = format)
    } else {
      if(format == '%Y-W%V-%d') {
        date <- ISOweek::ISOweek2date(datetime)
        datetime <- as.character(date)
        format <- '%Y-%m-%d'
      }
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
  if(format == '%Y-W%V-%d') {
    date <- ISOweek::ISOweek2date(datetime)
    datetime <- as.character(date)
    format <- '%Y-%m-%d'
  }
  t <- as.POSIXlt(datetime, format = format)
  year <- 1900 + t$year
  return(year)
}

getMonth <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  if(format == '%Y-W%V-%d') {
    date <- ISOweek::ISOweek2date(datetime)
    datetime <- as.character(date)
    format <- '%Y-%m-%d'
  }
  t <- as.POSIXlt(datetime, format = format)
  month <- t$mon + 1
  return(month)
}

getDay <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  if(format == '%Y-W%V-%d') {
    date <- ISOweek::ISOweek2date(datetime)
    datetime <- as.character(date)
    format <- '%Y-%m-%d'
  }
  t <- as.POSIXlt(datetime, format = format)
  day <- t$mday
  return(day)
}

getHour <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  if(format == '%Y-W%V-%d') {
    date <- ISOweek::ISOweek2date(datetime)
    datetime <- as.character(date)
    format <- '%Y-%m-%d'
  }
  t <- as.POSIXlt(datetime, format = format)
  hour <- t$hour
  return(hour)
}

getWeek <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  if(format == '%Y-W%V-%d') {
    date <- ISOweek::ISOweek2date(datetime)
    datetime <- as.character(date)
    format <- '%Y-%m-%d'
  }
  t <- as.POSIXlt(datetime, format = format)
  yearWeek <- ISOweek::ISOweek(t)
  week <- as.integer(substr(yearWeek, 7, 8))
  return(week)
}

getDayOfWeek <- function(datetime) {
  if(nchar(datetime[1]) == 4 || nchar(datetime[1]) == 7) {
    datetime <- fillTimestamp(datetime)
  }
  format <- getTimestampFormat(datetime[1])
  if(format == '%Y-W%V-%d') {
    date <- ISOweek::ISOweek2date(datetime)
    datetime <- as.character(date)
    format <- '%Y-%m-%d'
  }
  t <- as.POSIXlt(datetime, format = format)
  dayOfWeek <- ISOweek::ISOweekday(t)
  return(dayOfWeek)
}

fillTimestamp <- function(timestamp) {
  n <- nchar(timestamp[1])
  if(n == 4) { # year
    return(paste0(timestamp, "-01-01"))
  }
  if(n == 7) { # month
    return(paste0(timestamp, "-01"))
  }
  if(n == 8) { # week
    return(paste0(timestamp, "-1"))
  }
  return(timestamp)
}

getTimestampFormat <- function(timestamp) {
  n <- nchar(timestamp[1])
  if(n == 10) {
    if(grepl('W', timestamp)) { # week
      return("%Y-W%V-%d") # %V is not implemented (on windows)
    } else {
      return("%Y-%m-%d")
    }
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
#' @seealso \link{read_timeseries} \link{query_timeseries} \link{TubeDB} \link{query_regions} \link{query_region_plots} \link{query_region_sensors}
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
