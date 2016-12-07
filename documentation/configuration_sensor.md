Sensor Configuration
===

Climate parameters are defined by sensor configuration. 

Sensor definitions are global to all projects of one TubeDB instance.

### `global_scale_rainbow.png`

Image-file defines the color map used by heatmap visualisations.

The image does have a an arbitrary width and height of 1 pixel. Range of values is mapped to colors from left to right.

### `global_scale_round_rainbow.png`

Image-file defines the color map used by heatmap visualisations with same start and end color (e.g. wind direction).

The image does have a an arbitrary width and height of 1 pixel. Range of values is mapped to colors from left to right.

### `global_sensor_aggregation.ini`

Ini-file defines default aggregation per sensor.

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

file structure:

`[base_aggregation]`

`SENSOR_NAME = AGGREGATION_TYPE`
 
...

### `global_sensor_category.ini`

Ini-file defines visualisation category per sensor.

* `temperature` xy plot with red color
* `water` xy bar plot with blue color
* `other` xy plot with black color

file structure:

`[sensor_category]`

`SENSOR_NAME = CATEGORY`
 
...


### `global_sensor_description.ini`

Ini-file defines text description per sensor.

file structure:

`[sensor_description]`

`SENSOR_NAME = DESCRIPTION`
 
...


### `global_sensor_empirical_diff.ini`

Ini-file defines absolute maximum difference value for empirical quality check per sensor.

file structure:

`[parameter_empirical_diff]`

`SENSOR_NAME = MAX_DIFF`
 
...

### `global_sensor_ignore.ini`

Ini-file lists sensor names that are ignored at data import.

file structure:

`[ignore_sensors]`

`SENSOR_NAME`
 
...

### `global_sensor_internal.ini`

Ini-file lists administrative sensors that should be visible only if TubeDB is run in "internal" mode.

file structure:

`[internal_sensors]`

`SENSOR_NAME`
 
...

### `global_sensor_interpolation.ini`

Ini-file lists sensors that can be interpolated and for each sensor maximum acceptable MSE (mean square error) of regression.

file structure:

`[interpolation_sensors]`

`SENSOR_NAME = MAX_MSE`
 
...

### `global_sensor_physical_range.ini`

Ini-file defines range of values for physical quality check per sensor.

file structure:

`[parameter_physical_range]`

`SENSOR_NAME = [MIN, MAX]`
 
...

### `global_sensor_step_range.ini`

Ini-file defines range of step values per time step for step quality check per sensor.

file structure:

`[paramter_step_range]`

`SENSOR_NAME = [MIN, MAX]`
 
...

### `global_sensor_unit.ini`

Ini-file defines text description of measurement unit per sensor.

file structure:

`[sensor_unit]`

`SENSOR_NAME = UNIT`
 
...
