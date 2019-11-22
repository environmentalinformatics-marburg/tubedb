---
title: "Query API"
---

**Note**: See [API](../../api) for introduction to API based access.

With methods of query API time series measurement data can be processed and retrieved.

API identifier: `tsdb`

In the following methods of query API are specified and described: 
* `query_csv`
* `query_image`
* `query_heatmap`
* `heatmap_scale` 


## query_csv

Get one timeseries as CSV-file.

syntax: `query_csv?[PARAMETER1]=[VALUE1]&[PARAMETER2]=[VALUE2]&[PARAMETER3]=[VALUE3] ...`

possible paramters: 
* `plot` 
* `sensor` 
* `aggregation` 
* `quality` 
* `interpolated` 
* `start` 
* `end` 
* `year` 
* `month`
* `day`
* `end_year`
* `end_month`
* `end_day`
* `nan_text`

In the following the parameters are specified (optional parameters are set to default if not used):

`plot` plot identifier. Either `[PLOT]` or `[PLOT:STATION]`. e.g. `plot=AEW02` or `plot=cof1:51021020159`. To query multiple plots into one CSV-file this parameter can be specified multiple times in one query.

`sensor` sensor to query. To query multiple sensors into one CSV-file this parameter can be specified multiple times in one query.

`aggregation` (optional) timestep the time series should be aggregated to. Possible values are:

* `raw`  unprocessed time series data (raw).
* `hour` time series data will be aggregated to timesteps of hour. (default)
* `day` time series data will be aggregated to timesteps of day.
* `week` time series data will be aggregated to timesteps of week.
* `month` time series data will be aggregated to timesteps of month.
* `year` time series data will be aggregated to timesteps of year.

`quality` (optional) time series data will be filtered based on quality setting:

* `no` time series data will not be filtered
* `physical` time series data will be filtered by physical sensor range
* `step` same as physical and additional filter by measurement change-rate in time. (default)
* `empirical` same as step and additional filter by compare to average of sensors at similar plots.

`interpolated` (optional) gaps in time series data will be tried to fill by interpolation:

* `false` no gap filling (default)
* `true`  gap filling by interpolation

`start` (optional) start time of time series. Format: yyyy-MM-ddTHH:mm, abbreviations allowed. e.g.  2018-01-01T00:00, 2018-01-01T00, 2018-01-01, 2018-01, 2018.

`end` (optional) end time of time series. Format: yyyy-MM-ddTHH:mm, abbreviations allowed (filled to end of that period of time). e.g.  2018-01-31T23:59, 2018-01-31T23, 2018-01-31, 2018-01, 2018.

`year` (optional) One full year of data is queried only. (If `year` parameter is specified, parameters `start` and `end` should not be specified)

`month` (optional) (value 1 to 12) One full month of time series data is queried only (parameter `year` needed).

`day` (optional) (value 1 to 31) One full day of time series data is queried only (parameters `year` and `month` needed).

`end_year` (optional) query up to this year (end). (Should be used together with start parameter `year`)

`end_month` (optional) query up to this month (end). (Should be used together with start parameter `month`, parameter `end_year` needed).

`end_day` (optional) query up to this day (end). (Should be used together with start parameter `day`, parameters `end_year` and `end_month` needed).

If no time parameter is specified (`start`, `end`, `year`, `month`, `day`, `end_year`, `end_month`, `end_day`) full time series is returned.

For time span you should use one of the parameter sets (`start`, `end`) OR (`year`, `month`, `day`) OR (`year`, `month`, `day`, `end_year`, `end_month`, `end_day`).

`nan_text` (optional) set text of NA-values. (default: `NA`)

example:

`http://localhost:8080/tsdb/query_csv?plot=AEW02&sensor=rH_200&sensor=Ta_200&aggregation=day&quality=empirical&interpolated=true&year=2010&month=2`

Retrieves a CSV-file with header.

example of returned data:

`datetime,rH_200,Ta_200`

`2010-02-01T00:00,82.4,-5.9`

`2010-02-02T00:00,87.1,-2.4`


## query_image

Get diagram or box-plot of one sensor of one timeseries.

syntax: `query_image?[PARAMETER1]=[VALUE1]&[PARAMETER2]=[VALUE2]&[PARAMETER3]=[VALUE3] ...`

possible paramters: 
* `plot` 
* `sensor` 
* `aggregation` 
* `boxplot` 
* `quality` 
* `interpolated`
* `start` 
* `end` 
* `year` 
* `month` 
* `day` 
* `end_year` 
* `end_month` 
* `end_day` 
* `width` 
* `height`

These Parameters are described in `query_csv` and additional:

`plot` exactly one plot needs to be specified.

`sensor` exactly one sensor needs to be specified.

`boxplot` (optional) visualisation type:

* 'false' draw a time-value graph (default)

* 'true' draw a series of box-plots. Aggregation needs to be one of `day`, `week`, `month` or `year`. For each timestep a box-plot is drawn. e.g. with aggregation `month` for each month in time series a box-plot over the measurent values (aggregated to hour) of this month is calculated.

`width` (optional) width of resulting image (default 1500)

`height` (optional) height of resulting image (default 200)

example: `http://localhost:8080/tsdb/query_image?plot=AEW02&sensor=Ta_200&aggregation=day&quality=empirical&interpolated=true&year=2010&boxplot=true&width=1000&height=200`

Retrieves a PNG-image-file.


## query_heatmap

Get heatmap of one sensor of one timeseries aggregated to timesteps of hour.

Heatmap visualisation transforms each measurement value (one hour) to a colored pixel. The result is a colored area with days in x-direction and hour of day in y-direction.

syntax: `query_heatmap?[PARAMETER1]=[VALUE1]&[PARAMETER2]=[VALUE2]&[PARAMETER3]=[VALUE3] ...`

possible parameters: 
* `plot` 
* `sensor` 
* `quality` 
* `interpolated`
* `start` 
* `end` 
* `year` 
* `month` 
* `day` 
* `end_year` 
* `end_month` 
* `end_day`
* `by_year`
* `time_scale` 

These parameters are described in `query_csv` and additional:

`plot` exactly one plot needs to be specified.

`sensor` exactly one sensor needs to be specified.

`aggregation` is set to `hour`

`by_year` draw all data in one row or one row per year (default: false, all data in one row)

`time_scale` draw a time scale (default: true)

example: `http://localhost:8080/tsdb/query_heatmap?plot=AEW02&sensor=Ta_200&quality=empirical&interpolated=true&year=2010`

Retrieves a PNG-image-file.


## heatmap_scale

Get color scale of heatmap of one sensor.

syntax: `heatmap_scale?sensor=[SENSOR]`

example: `http://localhost:8080/tsdb/heatmap_scale?sensor=Ta_200`

Retrieves a PNG-image-file.

