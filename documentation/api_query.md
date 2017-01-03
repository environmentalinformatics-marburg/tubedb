Query API
===

With methods of query API time series measurement data can be processed and retrieved.

API identifier: `tsdb`

In the following methods of query API are specified and described: 
* `query_csv`
* `query_heatmap`
* `heatmap_scale` 
* `query_image`


###query_csv

Get one timeseries as CSV-file.

syntax: `query_csv?[PARAMETER1]=[VALUE1]&[PARAMETER2]=[VALUE2]&[PARAMETER3]=[VALUE3] ...`

possible paramters: 
* `plot` 
* `sensor` 
* `aggregation` 
* `quality` 
* `interpolated` 
* `year` 
* `month`

In the following the parameters are specified (optional parameters are set to default if not used):

`plot` plot identifier. Either `[PLOT]` or `[PLOT:STATION]`. e.g. `plot=AEW02` or `plot=cof1:51021020159`

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

`year` (optional)

* if left empty all time series data is queried (default)

* the set year of data is queries only

`month` (optional) (only used if year is set)

* if left empty whole year of time series data is queried (default)

* the set (value 1 to 12) month of time series data is queried only    

example:

`http://localhost:8080/tsdb/query_csv?plot=AEW02&sensor=rH_200&sensor=Ta_200&aggregation=day&quality=empirical&interpolated=true&year=2010&month=2`

Retrieves a CSV-file with header.

example of returned data:

`datetime,rH_200,Ta_200`

`2010-02-01T00:00,82.4,-5.9`

`2010-02-02T00:00,87.1,-2.4`


###query_heatmap

Get heatmap of one sensor of one timeseries aggregated to timesteps of hour.

Heatmap visualisation transforms each measurement value (one hour) to a colored pixel. The result is a colored area with days in x-direction and hour of day in y-direction.

syntax: `query_heatmap?[PARAMETER1]=[VALUE1]&[PARAMETER2]=[VALUE2]&[PARAMETER3]=[VALUE3] ...`

possible parameters: 
* `plot` 
* `sensor` 
* `quality` 
* `interpolated` 
* `year` 
* `month`

These parameters are described in `query_csv` and additional:

`sensor` exactly one sensor needs to be specified.

`aggregation` is set to `hour`

example: `http://localhost:8080/tsdb/query_heatmap?plot=AEW02&sensor=Ta_200&quality=empirical&interpolated=true&year=2010`

Retrieves a PNG-image-file.

###heatmap_scale

Get color scale of heatmap of one sensor.

syntax: `heatmap_scale?sensor=[SENSOR]`

example: `http://localhost:8080/tsdb/heatmap_scale?sensor=Ta_200`

Retrieves a PNG-image-file.

###query_image

Get diagram or box-plot of one sensor of one timeseries.

syntax: `query_image?[PARAMETER1]=[VALUE1]&[PARAMETER2]=[VALUE2]&[PARAMETER3]=[VALUE3] ...`

possible paramters: 
* `plot` 
* `sensor` 
* `aggregation` 
* `boxplot` 
* `quality` 
* `interpolated` 
* `year` 
* `month` 
* `width` 
* `height`

These Parameters are described in `query_csv` and additional:

`sensor` exactly one sensor needs to be specified.

`boxplot` (optional) visualisation type:

* 'false' draw a time-value graph (default)

* 'true' draw a series of box-plots. Aggregation needs to be one of `day`, `week`, `month` or `year`. For each timestep a box-plot is drawn. e.g. with aggregation `month` for each month in time series a box-plot over the measurent values (aggregated to hour) of this month is calculated.

`width` (optional) width of resulting image (default 1500)

`height` (optional) height of resulting image (default 200)

example: `http://localhost:8080/tsdb/query_image?plot=AEW02&sensor=Ta_200&aggregation=day&quality=empirical&interpolated=true&year=2010&boxplot=true&width=1000&height=200`

Retrieves a PNG-image-file.
