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

Data files in TOA5 format (CSV-like strucuture)
  
* `influx`

(Experimental) Online import of time series from InfluxDB server. Path specified YAML-config file with connection details (url, password, etc.).

* `mof`

(Experimental) Data files in MOF format (CSV-like strucuture)  








