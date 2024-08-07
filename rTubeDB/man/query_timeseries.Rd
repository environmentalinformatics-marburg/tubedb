% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/tubedb.R
\name{query_timeseries}
\alias{query_timeseries}
\title{query time series}
\usage{
query_timeseries(
  tubedb,
  plot,
  sensor,
  aggregation = "hour",
  quality = "physical",
  interpolated = FALSE,
  start = NULL,
  end = NULL,
  year = NULL,
  month = NULL,
  day = NULL,
  quality_counter = FALSE,
  casted = FALSE,
  spatial_aggregated = FALSE,
  datetimeFormat = "character",
  colYear = FALSE,
  colPlot = TRUE,
  colMonth = FALSE,
  colDay = FALSE,
  colHour = FALSE,
  colWeek = FALSE,
  colDayOfWeek = FALSE,
  colDayOfYear = FALSE,
  colMinute = FALSE
)
}
\arguments{
\item{tubedb}{instance of TubeDB class}

\item{plot}{character or list of character, one plot or list of plots}

\item{sensor}{character or list of character, one sensor or list of sensors}

\item{aggregation}{time steps, one of: raw hour day week month year}

\item{quality}{quality checks, one of: no, physical, step, empirical}

\item{interpolated}{apply interpolation, FALSE or TRUE}

\item{start}{start time of timeseries}

\item{end}{end time of timeseries}

\item{year}{get data from one full year only (You may use this 'year' parameter OR 'start', 'end' parameters)}

\item{month}{get data of one full month of one year only, parameter year neads to be specified (You may use this 'month' parameter OR 'start'/'end' parameters)}

\item{day}{get data of one full day of one month only, parameter month neads to be specified (You may use this 'day' parameter OR 'start'/'end' parameters)}

\item{quality_counter}{qualityCounter column to count aggregated measured values and interpolated values}

\item{casted}{all plot-sensor pairs in one row per timestamp, e.g. columns plot1.sensor1, plot2.sensor1, plot1.sensor2}

\item{spatial_aggregated}{combine all values of one sensor over all plots to one value per timestamp by mean}

\item{datetimeFormat}{character, requested type of timestamps. one of: "character", "POSIXct", "POSIXlt"}

\item{colYear}{add numeric year column in resulting data.frame (calendar year)}

\item{colPlot}{add column with plot name}

\item{colMonth}{add numeric month column in resulting data.frame (1 to 12)}

\item{colDay}{add numeric day column in resulting data.frame (1 to 31)}

\item{colHour}{add numeric hour column in resulting data.frame (0 to 23)}

\item{colWeek}{add numeric day of week column in resulting data.frame (1 to 53) (ISO week based)}

\item{colDayOfWeek}{add numeric day of week column in resulting data.frame (1 to 7) (ISO week based)}

\item{colDayOfYear}{add numeric day of year column in resulting data.frame (1 to 366)}

\item{colMinute}{add numeric minute in resulting data.frame (0 to 59)}
}
\value{
data.frame time series with datetime column + sensor columns
}
\description{
Query time series as data.frame at one plot of several sensors from TubeDB server and create timestamps of specified type.
}
\details{
Mandatory parameters: \strong{tubedb}, \strong{plot}, \strong{sensor}
}
\section{Time span of time series}{


no time parameter -> full avaiable timespan

\code{year} parameter -> one full year

\code{year} and \code{month} parameter -> one full month

\code{start} and \code{end} parameter -> exact time span

Format of \code{start} and \code{end} parameter: (character) yyyy-MM-ddTHH:mm, abbreviations allowed. e.g.  2018-12-31T23:59, 2018-12-31T23, 2018-12-31, 2018-12, 2018. '\code{end}' abbreviations are filled, e.g. 2018-01 -> 2018-01-31T23:59
}

\examples{
# load package
library(rTubeDB)

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
}
\seealso{
\link{TubeDB} \link{query_regions} \link{query_region_plots} \link{query_region_sensors} \link{POSIXct} \link{POSIXlt} \link{read_timeseries} \link{query_diagram} \link{query_heatmap}
}
\author{
woellauer
}
