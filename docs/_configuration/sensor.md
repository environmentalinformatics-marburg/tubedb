---
title: "Sensor Configuration"
---

Climate parameters are defined by sensor configuration in config-folder. Sensor definitions are global to all projects of one TubeDB instance.

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

| property | description | default | example
|-------|--------|---------|---------|
| **description** | textual sensor description |-| 'Air temperature at 2 meters above ground' |
| **unit** | textual measurement unit |-| '% (percentage of relative humidity, 0-100)' |
| **aggregation** | time-aggregation type |none| average |
| **physical_range** | sensor value range |-| [0.0, 100.0] |
| **step_range** | sensor range of value changes over time |-| [0.0, 10.0] |
| **empirical_diff** | max difference to reference time series |-| 10.0 |
| **interpolation_mse** | max MSE of linear regression |-| 7 |
| **category** | visualisation category-type  |other| temperature |
| **visibility** | visible for target audience |public| internal |
| **raw_source** | raw data is copied from specified sensor |-| Ta_200_internal |
| **dependency** | list all needed sensors |-| [Ta_200, rH_200] |
| **post_hour_func** | fomula for application directly after hour aggregation |-| 2*Ta_200 |
| **post_day_func** | fomula for application directly after day aggregation|-| 2*Ta_200 + rH_200 |
| **derived** | mark as from real measurements derived |-| true |
| **aggregation_hour** | type of aggregation specific to raw->hour |->aggregation| average |
| **aggregation_day** | type of aggregation specific to hour->day  |->aggregation| minimum |
| **aggregation_week** | type of aggregation specific to day->week  |->aggregation| sum |
| **aggregation_month** | type of aggregation specific to day->month  |->aggregation| sum |
| **aggregation_year** | type of aggregation specific to month->year  |->aggregation| maximum |


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

**visibility**: `[VISIBILITY_TYPE]`

Valid values: `internal`, `public`.

Internal sensor is visible only if TubeDB is run in internal mode: In [TubeDB configuration](../tubedb) set setting `HIDE_INTENAL_SENSORS = false`

**raw_source**: `[SENSOR_NAME]`

Before application of any processing, values of specified sensor are copied to this sensor. This can be used to provide multiple aggregations of one sensor, e.g. temperaur sensor `Ta_200` with `average` aggregation and `Ta_200_max` with `minimum` aggregation and `raw_source: Ta_200`

**dependency**: `[[SENSOR_NAME1], [SENSOR_NAME2]]`

example: `dependency: [Ta_200]` 

example: `dependency: [Ta_200, rH_200]`

List all sensors that are needed to provide this sensor.

Only if all dependencies are fulfilled, a sensor is shown for a station. e.g. a sensor `Ta_200_max` is shown at a statins if that station measures `Ta_200.`

**post_hour_func**: `[FORMULA]`

**post_day_func**: `[FORMULA]`

Given formula is applied at processing after hour-aggregation resp. after day-aggregation. All used sensor need to be specified as dependency.

formula is composed of following terms: (descending operator precedence)

numbers: `40`, `26.7`, `-8.784695`, `-3.582e-6`

sensors: `Ta_200`, `rH_200`

brackets: `(a+b)*(c+d)`

exponentiation: `Ta_200^2`

multiplication and division: `1.61139411 * Ta_200`, `Ta_200 / 10`

addition, subtraction: `Ta_200 + 100`, `Ta_200 - 100`

conditional expression: `(CONDITION ? TRUE_FORMULA : FALSE_FORMULA)` (mandatory brackets), `(Ta_200 < 0 ? Ta_200^2 : Ta_200)`

condition predicate less, less equal, equal, greater equal, greater, not equal, brackets: `a<b`, `a<=b`, `a=b`, `a>=b`, `a>b`, `a<>b`, `(a==b && c==d)`

condition not: `!a`, `(!(Ta_200 < 0)  ? Ta_200^2 : Ta_200)`

condition and: `a && b`

condition or: `a || b`


example: calculate heat-index
~~~ yaml
Ta_200_heat_index:  
  description: heat index based on Ta_200 and rH_200
  unit: °C (degree Celcius)
  dependency: [Ta_200, rH_200]
  post_hour_func: "(26.7 <= Ta_200 && 40 <= rH_200 ? -8.784695 +1.61139411*Ta_200 +2.338549*rH_200 -0.14611605*Ta_200*rH_200 -1.2308094e-2*Ta_200^2 -1.6424828e-2*rH_200^2 +2.211732e-3*Ta_200^2*rH_200 +7.2546e-4*Ta_200*rH_200^2 -3.582e-6*Ta_200^2*rH_200^2 : Ta_200)"
  aggregation: average
  category: temperature  
~~~ 

 
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

