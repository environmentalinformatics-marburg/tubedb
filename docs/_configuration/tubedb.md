---
title: "TubeDB Configuration"
---

In `tsdb_paths.ini`-file at TubeDB root global settings for TubeDB are specified.

---
### `tsdb_paths.ini` description

Ini-file defines setting. One per line.

`tsdb_paths.ini`-file with default settings: (if a setting is not set that defaults are used)
~~~ ini
[tsdb_paths]

# folder of meta data config files  
CONFIG_PATH = config
 
# folder of database storage files
STORAGE_PATH = storage

# folder of some processing
OUTPUT_PATH = output

# folder with web based user interface files for client side (e.g. HTML, JavaScript).
WEBCONTENT_PATH = webcontent

# temporary folder with generated zip-archives from timeseries export
WEBDOWNLOAD_PATH = webDownload

# User generated content that is included in Web user interface.
WEBFILES_PATH = webFiles

# TubeDB URL base prefix (empty per default)
WEB_SERVER_PREFIX_BASE_URL = 

# HTTP port of web server for web UI and API
WEB_SERVER_PORT = 8080

# Sensors that are marked 'internal' are not listed in API methods.
HIDE_INTENAL_SENSORS = true

# Request user and password for API and web UI.
WEB_SERVER_LOGIN = false
~~~

---
### `tsdb_paths.ini` example

Start webserver at port 8081, activate access control and show internal public sensors.

~~~ ini
[tsdb_paths]
WEB_SERVER_PORT = 8080
WEB_SERVER_LOGIN = true
HIDE_INTENAL_SENSORS = false
~~~