Sensor Configuration
===

Climate parameters are defined by sensor configuration. 

Sensor definitions are global to all projects of one TubeDB instance.

### `scale_rainbow.png`

Image-file defines the color map used by heatmap visualisations.

The image does have a an arbitrary width and height of 1 pixel. Range of values is mapped to colors from left to right.

### `scale_round_rainbow.png`

Image-file defines the color map used by heatmap visualisations with same start and end color (e.g. wind direction).

The image does have a an arbitrary width and height of 1 pixel. Range of values is mapped to colors from left to right.

### `sensor_ignore.ini`

Ini-file lists sensor names that are ignored at data import.

file structure:

`[ignore_sensors]`

`SENSOR_NAME`
 
...

### `sensors.yaml`

Yaml-file defines sensor meta data.

properties:

(optional) description: `DESCRIPTION`

Description of the sensor.

(optional) unit: `UNIT DESCRIPTION`

Measurement unit and description.

(optional) aggregation: `AGGREGATION TYPE`

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

(optional) physical_range: `[MIN, MAX]`

Valid range of measurement values for physical quality check.

(optional) step_range: `[MIN, MAX]`

Valid step range of consecutive measurement values for step quality check.

(optional) empirical_diff: `[MAX]`

Absolute maximum difference to reference time series for empirical quality check.

(optional) interpolation_mse: `[MAX_MSE]`

Maximum acceptable MSE (mean square error) of regression models used for Interpolated.

Defaults to no interpolation.

(optional) category: `[CATEGORY]`

Sensor category for visualisation purposes.

Valid values: `temperature`, `water`, `other`.

Defaults to `other`.

(optional) visibility: `[VISIBILITY_TYPE]`

Valid values: `internal`, `public`.

Defaults to `public`.

Internal sensor are visible only if TubeDB is run in internal mode.
 

file structure:

`SENSOR_NAME:`

`  PROPERTY_NAME: PROPERTY_VALUE`

...

example file-content:

```YAML

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

```
