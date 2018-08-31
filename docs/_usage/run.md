---
title: "Run"
---

requirement: [installed TubeDB](../install)

TubeDB is managed by shell scripts.

Linux (Ubuntu)
---

#### `server.sh`
Start (web)server. Open a browser and type `http://localhost:8080` (default). Terminate the server with key `ctrl-c`. 

#### `explorer.sh`
Open desktop GUI for time series and meta data inspection.

#### `clear_import.sh`
Clear all time series (and mask) data and import time series from data files.

#### `import.sh`
Import time series from data files. Does not clear previous time series.

#### `clear_load_masks.sh`
Clear all mask data and load masks from config files.

#### `interactive.sh` (for advanced usage)
Open TubeDB shell.

#### `tsdb.sh` (for advanced usage)
Run command in TubeDB (`tsdb.sh [COMMAND]`). This is the entry point for other scripts to execute commands on TubeDB.

### Background Server

For operation in background TubeDB uses [screen](https://wiki.ubuntuusers.de/Screen/). You may need to install screen `sudo apt-get install screen`
Following scripts manage TubeDB in background mode with screen.

#### `start_server_background.sh`
runs the TubeDB webserver in background. For this the application *screen* is required (e.g. run `sudo apt-get install screen`)

#### `stop_server_background.sh`
stops a running background server of TubeDB.

#### `shutdown_server.sh`

#### `check_server_background.sh`
checks if a TubeDB server is running in background.

#### Advanced Usage

If you are experienced with screen. You can operate with the TubeDB background session. The screen session name is `tubedb`. It is recommended to first become familiar with [screen](https://wiki.ubuntuusers.de/Screen/) workflow.

Check if screen session is running `screen -ls`

Open running screen session `screen -r tubedb` or `screen -d -r tubedb` (if session is attached)

Leaf opened screen session, session continues to run in background `ctrl-a d`

Leaf and terminate opened screen session `ctrl-a \`

### Automated Operation

#### `backup_cron.sh`
Create Backup of TubeDB. It is recommended to shutdown TubeDB before backup.

#### `cronjob.sh`
Shutdown server, create backup, clear database, import time series files, start server. Creates log file and calls `cronjob_internal.sh`.

#### `cronjob_internal.sh` (for internal usage)
Intended to be used as internal skript for `cronjob.sh`. Shutdown server, create backup, clear database, import time series files, start server: `shutdown_server.sh`, `backup_cron.sh`, `clear_import.sh`, `start_server_background.sh`

#### `cronjob_wrapper.sh` (cron job taget)
Helper script for cronjob.sh to execute in correct working directory if called from outside of working directory (e.g. as cron job). Use this script as linux cron job entry. You need to change absolute path to correct TubeDB working directory.


Windows
---

#### `server.cmd`
Start (web)server. Open a browser and type `http://localhost:8080` (default). Terminate the server with key `ctrl-c`. 

#### `explorer.cmd`
Open desktop GUI for time series and meta data inspection.

#### `clear_import.cmd`
Clear all time series (and mask) data and import time series from data files.

#### `import.cmd`
Import time series from data files. Does not clear previous time series.

#### `clear_load_masks.cmd`
Clear all mask data and load masks from config files.

#### `interactive.cmd` (for advanced usage)
Open TubeDB shell.

#### `tsdb.cmd` (for advanced usage)
Run command in TubeDB (`tsdb.cmd [COMMAND]`). This is the entry point for other scripts to execute commands on TubeDB.