---
title: "Sensor Configuration"
---

Climate parameters are defined by sensor configuration (in config-folder). Sensor definitions are global to all projects of one TubeDB instance.

---
### `config/sensors.yaml`

This Yaml-file defines sensor meta data.

file structure:

~~~ yaml
SENSOR_NAME_1:
  PROPERTY_NAME_1: PROPERTY_VALUE
  PROPERTY_NAME_2: PROPERTY_VALUE
SENSOR_NAME_2:
  PROPERTY_NAME_1: PROPERTY_VALUE
~~~

example file:

Define two sensors (Ta_200, rH_200) and set properties of that sensors. (sensor properties are indented by two space characters)

~~~ yaml
Ta_200:
  description: Air temperature at 2 meters above ground
  unit: °C (degree Celcius, -40 to 60)
  aggregation: average
  physical_range: [-40.0, 60.0]
  step_range: [0.0, 10.0]
  empirical_diff: 10.0
  interpolation_mse: 7
  category: temperature

rH_200:
  description: Relative air humidity at 2 meters above ground
  unit: '% (percentage of relative humidity, 0-100)'
  aggregation: average
  physical_range: [0.0, 100.0]
  step_range: [0.0, 100.0] #high fluctuation possible
  empirical_diff: 50
  interpolation_mse: 100 
~~~

| property | description | example
|-------|--------|---------|
| **description** | sensor description | 'Air temperature at 2 meters above ground' |
| **unit** | measurement unit | '% (percentage of relative humidity, 0-100)' |
| **aggregation** | time aggregation type | average |
| **physical_range** | sensor value range | [0.0, 100.0] |
| **step_range** | sensor range of value changes over time | [0.0, 10.0] |
| **empirical_diff** | max difference to reference time series | 10.0 |
| **interpolation_mse** | max MSE of linear regression | 7 |
| **category** | category type for visualisation | temperature |
| **visibility** | visible for target audience | public |
| **raw_source** | raw data is copied from specified sensor | Ta_200_internal |
| **dependency** | list all needed sensors | [Ta_200, rH_200] |
| **post_hour_func** | fomula for application directly after hour aggregation | 2*Ta_200 |
| **post_day_func** | fomula for application directly after day aggregation| 2*Ta_200 + rH_200 |
| **derived** | group sensor in real measurement / derived | true |
| **aggregation_hour** | type of aggregation specific to raw->hour | average |
| **aggregation_day** | type of aggregation specific to hour->day  | minimum |
| **aggregation_week** | type of aggregation specific to day->week  | sum |
| **aggregation_month** | type of aggregation specific to day->month  | sum |
| **aggregation_year** | type of aggregation specific to month->year  | maximum |


All properties are optional, but depending TubeDB functionality may be missing for that sensor or revert to defaults.

property details:

**aggregation**: `AGGREGATION TYPE`

Type of measurement time aggregation.

* `average` average of values
* `average_albedo` albedo specific	
* `average_zero` without negative values
* `average_wind_direction` marker for wind aggregation			
* `average_wind_velocity` marker for wind aggregation
* `maximum` maximum of values
* `minimum` minimum of values
* `none`	no aggregation						
* `sum` sum of values		
* `sum_of_average` average for base aggregation then sum for further aggregation			
* `sum_radiation` radiation specific
* `sum_sunshine`  sunshine specific

This type is set to all aggregation granularities of one sensor. It can be set to specific aggregation types with properties aggregation_hour, aggregation_day, aggregation_week, aggregation_month and/or aggregation_year.

**physical_range**: `[MIN, MAX]`

Valid range of measurement values for physical quality check.

**step_range**: `[MIN, MAX]`

Valid step range of consecutive measurement values for step quality check.

**empirical_diff**: `[MAX]`

Absolute maximum difference to reference time series for empirical quality check.

**interpolation_mse**: `[MAX_MSE]`

Maximum acceptable MSE (mean square error) of regression models used for Interpolated.

Defaults to no interpolation.

**category**: `[CATEGORY]`

Sensor category for visualisation purposes.

Valid values: `temperature`, `water`, `other`.

Defaults to `other`.

**visibility**: `[VISIBILITY_TYPE]`

Valid values: `internal`, `public`.

Defaults to `public`.

Internal sensor is visible only if TubeDB is run in internal mode.
 
---
### `config/sensor_ignore.ini`

Ini-file lists sensor names that are ignored at data import.

file structure:

~~~ ini
[ignore_sensors]
SENSOR_NAME1
SENSOR_NAME2
SENSOR_NAME3
~~~

---
### `config/scale_rainbow.png` 
(default color file is included in package)

Image-file defines the color map used by heatmap visualisations.

The image does have a height of 1 pixel and an arbitrary width. Range of values is mapped to colors from left to right.

---
### `config/scale_round_rainbow.png`
(default color file is included in package)

Image-file defines the color map used by heatmap visualisations with same start and end color (e.g. wind direction).

The image does have a height of 1 pixel and an arbitrary width. Range of values is mapped to colors from left to right.

