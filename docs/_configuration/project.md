---
title: "Project Configuration"
---

Project specific configuration is located in files of one subfolder of `config` per project.

---
### `config/project1/region.ini`

Ini-file sets region name and valid time range of project.

file structure:

`[region]`

`REGION_NAME = REGION_LONG_NAME`

`[region_view_time_range]`
 
`REGION_NAME = [YEAR_MIN, YEAR_MAX]`

---
### `config/project1/general_stations.ini`

Ini-file lists plot groups ("general stations"). One plot is contained in exactly one plot group.

Optionally several groups may form a super group. If present all plots of one super group are the set of plots for interpolation and reference generation purposes.

file structure:

`[general_stations]`

`GROUP_NAME = REGION_NAME`

...

`[general_station_long_names]`
 
`GROUP_NAME = GROUP_LONG_NAME`

...

`[general_station_groups]` (optional)

`GROUP_NAME = SUPER_GROUP_NAME`

...

---
### `config/project1/logger_type_schema.ini`

Ini-file lists station logger types and contained sensors.

file structure:

`[LOGGER_TYPE_NAME]`

`SENSOR_NAME`

---
### `config/project1/plot_inventory.csv`

Csv-file lists plots and their properties.

columns:

* `plot` plot name
* `general` plot group ("general station") of this plot
* `focal` (optional) if plot is a focal plot (`Y`/`N`), defaults to `N`.
* `lat` (optional) WGS84 position latitude 
* `lon` (optional) WGS84 position longitude
* `easting` (optional) project specific (planar) coordinate system position (e.g. one UTM zone) first axis
* `northing` (optional) project specific (planar) coordinate system position (e.g. one UTM zone) second axis
* `elevation` (optional) elevation a.s.l.
* `is_station` (optional) if plot name denotes a station with same name (`Y`/`N`), defaults to `N`.
* `logger` (required if plot is station) logger type name
* `alternative_id` (optional) alternative plot id for information purposes only 

---
### `config/project1/station_inventory.csv` (optional)

Csv-file lists stations and their properties.

If all plots are defined as stations (in `plot_inventory.csv`) this file may be omitted.

columns:

* `plot` plot name
* `logger` logger type name
* `serial` station name
* `start` (optional) start of measurement, defaults to `*`
* `end` (optional) end of measurement, defaults to `*`

Columns `start` and `end` may be set to `*` to denote open start / open end.

Additional custom columns are inserted into station properties. 

---
### `config/project1/sensor_translation.ini` (optional)

Ini-file contains sensor name mappings from raw measurement files to database sensor names.

file structure:

`[LOGGER_TYPE_NAME_logger_type_sensor_translation]`

`RAW_SOURCE_NAME = SENSOR_NAME`

...

...

`[GROUP_NAME_generalstation_sensor_translation]`

`RAW_SOURCE_NAME = SENSOR_NAME`

...

...

`[STATION_NAME_station_sensor_translation]`

`RAW_SOURCE_NAME = SENSOR_NAME`

...

...

Details:

ini-sections.

`[<LOGGER_TYPE_NAME>_logger_type_sensor_translation]`

`[<GENERAL_STAION_NAME>_generalstation_sensor_translation]`

`[<STATION_NAME>_station_sensor_translation]`


entries.

translate raw -> sensor: `<RAW_SENSOR_NAME> = <SENSOR_NAME>`

ignore raw sensor: `<RAW_SENSOR_NAME> = NaN`


translation process of raw sensor name.

1. check entry of station

2. (if not found) check entry of general station

3. (if not found) check entry of logger type

4. (if not found) check if raw sensor name is contained in logger schema (defined `logger_type_schema.ini`)

5. (if not found) no insert into database (add entry in log file)

---
### `config/project1/sensor_name_correction.json` (optional)

Json-file contains sensor name mappings for specific time ranges.

For each entry one raw source name of one plot is mapped to one sensor name if imported data source file is contained in specified time interval.

file structure:

`[`

`{plot:"PLOT_NAME", raw:"RAW_SOURCE_NAME", correct:"SENSOR_NAME", start:"START_DATE_TIME", end:"END_DATE_TIME"},`

...

`]`

Details:

This config file contains correction information for wrong sensor names from raw-data-input-files. Original sensor names are mapped to corrected sensor names. This mapping is applied before the sensor name translation step with information from config file `sensor_translation.ini`. Each entry contains start and end date of validity. Only files with a time-interval that is completely within the time-interval of the entry are applied to the correction-entry. Files with a time-interval that is (partly) outside of an entry are NOT applied. So its possible to define start and end date imprecise (a bit lower for start resp. a bit higher for end). If multiple entries fit to one file and raw sensor name the first entry is applied.

File format is JSON with block comments.

file structure (JSON array of entries):

`[`

`<ENTRY1>,`

`<ENTRY2>,`

`<ENTRY3>,`

`]`


entry structure (as JSON object):

`{plot:"<PLOT_NAME>", raw:"RAW_SENSOR_NAME", correct:"CORRECTED_SENSOR_NAME", start:"START_DATE", end:"END_DATE"}`

date format (ISO 8601):

YEAR-MONTH-DAY

`YYYY` eg. `2009`

`YYYY-MM` eg. `2009-12`

`YYYY-MM-DD` eg. `2009-12-31`

`YYYY-MM-DDThh:mm` eg. `2009-12-31T14:09`

Shortened end dates are set to end of that period e.g. end date `2009` leads to `2009-12-31T23:59`.

For unspecified start or end `*` can be used.


example file-content:
`[`

`{plot:"AEG00", raw:"Ta_2000", correct:"Ta_200", start:"2001-11-22T00:57", end:"2011-12-21T22:33"},`

`]`

---
### `config/project1/station_properties.yaml` (optional)

Yaml-file lists custom station properties that are valid for a given interval of time.

file structure:

`- {station: STATION_NAME, start: START_DATE, end: END_DATE, content: {label: PROPERTY_NAME, C1_NAME: C1_VALUE, C2_NAME: C2_VALUE, ...} }`

...

---
### `config/project1/mask.csv` (optional)

Csv-file lists time-ranges of sensor malfunctions.

To assist automatic quality checks known time ranges of sensor malfunctions can be declared here.

columns:

* `station` station name
* `sensor` sensor name
* `start` date of malfunction start
* `end` date of malfunction end
* `comment` (optional) note about type of malfunction for information purposes only

Note: In contrast to all other config files `mask.csv` changes need to be explicitly updated (`ClearLoadMasks.sh`).
