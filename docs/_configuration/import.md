---
title: "Data Import Configuration"
---

In `import.ini`-file at TubeDB root folder locations of source data files are specified.
Import of data files can then be executed by import commend (see [run](../../usage/run)).

---
### `import.ini` description

Ini-file defines imports. For each region one section specifies file types and paths data files.
There may be multiple entries per section (possibly of same import type).

`import.ini`-file structure:
~~~ ini
[REGION_NAME]
IMPORT_TYPE = PATH
IMPORT_TYPE = PATH

[REGION_NAME]
IMPORT_TYPE = PATH
~~~

---
### `import.ini` example

For region "CALDERN" files in folder (and subfolders) "data/caldern" of file format "generic CSV" should be imported into TubeDB.

~~~ ini
[CALDERN]

csv = data/caldern
~~~

---
### Extended format

The simple format is:
~~~ ini
IMPORT_TYPE = PATH
~~~

With extended format, parameters in addition to path can be specified. It is JSON format:
~~~ ini
IMPORT_TYPE = {KEY1: "VALUE1", KEY2: "VALUE2"}
~~~

Example
~~~ ini
[MyProject]
treetalker = {path: "c:/data/treetalker_data1"}
treetalker = {path: "c:/data/treetalker_data2", time_offset: "PT1H"}
~~~

---
### import types

(For detailed file format description see [import formats](../../documentation/import_formats).)

* `csv`
  
  Generic CSV-files.

* `csv_tfi`

  "tfi" specific CSV-files.

* `csv_hobo`

  "hobo-logger" specific CSV-files.

* `tsa`

  Generic TSA files.

* `asc`

  Generic ASC logger files.

* `asc_ki`

  "ki" specific ASC-files.

* `asc_sa_own`

  "sa_own" specific ASC-files.
  
* `udbf_be`

  Generic UDBF files with some "be" specific extensions.	

* `toa5`

  Data files in TOA5 format (CSV-like strcuture).
  
* `influx`

  (Experimental) Online import of time series from InfluxDB server. Path specified YAML-config file with connection details (url, password, etc.).

* `mof`

  (Experimental) Data files in MOF format (CSV-like structure).  

* `treetalker`

  Data files in treetalker text format. With parameter `time_offset` timestamps can be shifted for e.g. time zone translation ([time format specification](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-)).
 
  Example
  ~~~ ini
  [MyProject]
  # No time shifting
  treetalker = c:/data/treetalker_data1
  treetalker = {path: "c:/data/treetalker_data2"}
  
  # Offset of one hour, e.g. shifting time from UTC to UTC+1
  treetalker = {path: "c:/data/treetalker_data3", time_offset: "PT1H"}

  # Offset of minus one hour, e.g. shifting time from UTC to UTC-1
  treetalker = {path: "c:/data/treetalker_data4", time_offset: "-PT1H"}

  # Offset of 15 minutes
  treetalker = {path: "c:/data/treetalker_data5", time_offset: "PT15M"}

  # Offset of 2 hours and 15 minutes
  treetalker = {path: "c:/data/treetalker_data6", time_offset: "PT2H15M"}
  ~~~




