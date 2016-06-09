Meta Data API
---

With methods of Meta Data API information associated with time series measurement data like contained plots and sensor can be retrieved.

API identifier: `tsdb`

In the following methods of Meta Data API are specified and described: 
* `region_list` 
* `generalstation_list` 
* `plot_list`  
* `plotstation_list` 
* `sensor_list` 
* `status`

###region_list

Get regions (collection of plots within one project).

syntax: `region_list`

example: `http://localhost:8080/tsdb/region_list`

Retrieves a new-line separated list of regions with region identifier and region long name separated by semicolon.

example of returned data:

`BE;Exploratories`

`KI;Kilimanjaro`


###generalstation_list

Get general stations of one region (sub-collection of plots in one region / project). 

Typically one region contains several general stations that contains several plots. 

syntax: `generalstation_list?region=[REGION]`

example: `http://localhost:8080/tsdb/generalstation_list?region=BE`

Retrieves a new-line separated list of general stations with general station identifier and general station long name separated by semicolon.

example of returned data:

`AEG;Schwäbische Alb Grünland`

`AEW;Schwäbische Alb Wald`


###plot_list

Get plots of one general station or of one region (all plots from all general stations within one region).

syntax: `plot_list?region=[REGION]` or `plot_list?generalstation=[GENERAL STATION]`

example: `http://localhost:8080/tsdb/plot_list?generalstation=AEW` 

Retrieves a new-line separated list of plots with plot identifier, plot type (normal or vip) and logger type separated by semicolon.

example of returned data:

`AEW09;vip;00CEMU`

`AEW10;normal;00CEMU`


###plotstation_list

Get stations of one plot.

A plot may consist of several independent loggers (stations).

If a plot is identical with one station then this method returns nothing.

syntax: `plotstation_list?plot=[PLOT]`

example: `http://localhost:8080/tsdb/plotstation_list?plot=cof1`

Retrieves a new-line separated list of stations with station identifier and logger type separated by semicolon.

example of returned data:

`51021020159;rug`

`51021020218;rug`


###sensor_list

Get all sensors (climatic parameters) of one plot or of one general station or of one region or of one station.

Optionally including sensors that can not be aggregated (raw).

syntax: 

`sensor_list?plot=[PLOT]` or `sensor_list?plot=[PLOT]&raw=true`

`sensor_list?general_station=[GENERAL STATION]` or `sensor_list?general_station=[GENERAL STATION]&raw=true`

`sensor_list?region=[REGION]` or `sensor_list?region=[REGION]&raw=true`

`sensor_list?station=[STATION]` or `sensor_list?station=[STATION]&raw=true`

example: `http://localhost:8080/tsdb/sensor_list?plot=AEW09`

Retrieves a new-line separated list of sensors with sensor identifier, sensor description, sensor unit description, default aggregation and flag if sensor is not public separated by semicolon.

example of returned data:

`rH_200;Relative air humidity at 2 meters above ground;% (percentage of relative humidity, 0-100);AVERAGE;false`

`Ta_200;Air temperature at 2 meters above ground;°C (degree Celcius, -40 to 60);AVERAGE;false`


###status

Get plot status information of plots within one general station or within one region.

syntax `status?generalstation=[GENERAL STATION]` or `status?region=[REGION]`

example: `http://localhost:8080/tsdb/status?generalstation=AEW`

Retrieves a JSON array of JSON objects for each plot.

JSON object of one plot: 

`{plot:[PLOT], first_timestamp:[TIMESTAMP], last_timestamp:[TIMESTAMP], first_datetime:[DATE], last_datetime:[DATE], voltage:[VALUE], message_date:[DATE], message:[TEXT]}`

`plot`: plot identifier

`first_timestamp` / `first_datetime`: timestamp / date as text of earliest data measurement

`last_timestamp` / `last_datetime`: timestamp / date as text of latest data measurement

`voltage`: (optional) latest internal logger voltage measurement

`message`: (optional) latest internal log message

`message_date`: (optional) date as text of log message

example of returned data:

`[{"plot":"AEW10","first_timestamp":1234,"last_timestamp":2234,"first_datetime":"2000-01-01T00:00","last_datetime":"2010-10-10T10:10","voltage":12.76,"message_date":"2010-10-10T10:20","message":"OK"},{"plot":"AEW11","first_timestamp":1244,"last_timestamp":2244,"first_datetime":"2000-01-02T00:00","last_datetime":"2010-10-10T10:30","voltage":11.92,"message_date":"2010-10-10T10:40","message":"OK"}]`  
