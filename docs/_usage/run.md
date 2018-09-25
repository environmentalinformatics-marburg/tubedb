---
title: "Run"
---

requirement: [installed TubeDB](../install)

TubeDB is managed by shell scripts.

**Note**: Only one instance of TubeDB may open the database. So you can start TubeDB as server or for data import or as local GUI etc. but only one at a time. If you run TubeDB as background server make sure to shutdown the server before you initiate a data import etc.

Linux (Ubuntu)
---

#### `./server.sh`
Start (web)server. Open a browser and type `http://localhost:8080` (default). Terminate the server with key `ctrl-c`. 

#### `./explorer.sh`
Open desktop GUI for time series and meta data inspection.

#### `./clear_import.sh`
Clear all time series and import from data files and load masks from config.

#### `./import.sh`
import time series data from files and load masks from config. Does not clear previous time series.

#### `./clear_load_masks.sh`
Clear all time series masks and load from config. Does not clear time series data.

#### `./interactive.sh` (for advanced usage)
Open TubeDB shell.

#### `./tsdb.sh` (for advanced usage)
Run command in TubeDB (`tsdb.sh [COMMAND]`). This is the entry point for other scripts to execute commands on TubeDB.

### Background Server

For operation in background TubeDB uses [screen](https://wiki.ubuntuusers.de/Screen/). You may need to install screen `sudo apt-get install screen`
Following scripts manage TubeDB in background mode with screen.

#### `start_server_background.sh`
Start TubeDB (web)server in background.

#### `shutdown_server.sh`
Send shutdown request to background TubeDB (web)server to terminate.

#### `stop_server_background.sh`
Directly terminate background TubeDB (web)server.

#### `check_server_background.sh`
Check if background TubeDB (web)server is running.

#### Advanced Usage

If you are experienced with screen you can operate with the TubeDB background session. The screen session name is `tubedb`. It is recommended to first become familiar with [screen](https://wiki.ubuntuusers.de/Screen/) workflow.

Check if screen session is running `screen -ls`

Open running screen session `screen -r tubedb` or `screen -d -r tubedb` (if session is attached)

Leave opened screen session, session continues to run in background `ctrl-a d`

Leave and terminate opened screen session `ctrl-a \`

### Automated Operation

#### `backup_cron.sh`
Create Backup of TubeDB. It is recommended to shutdown TubeDB before backup.

#### `cronjob.sh`
Shutdown server, create backup, clear database, import time series files, start server. Creates log file and calls `cronjob_internal.sh`.

#### `cronjob_internal.sh` (for internal usage)
Intended to be used as internal skript for `cronjob.sh`. Shutdown server, create backup, clear database, import time series files, start server: `shutdown_server.sh`, `backup_cron.sh`, `clear_import.sh`, `start_server_background.sh`

#### `cronjob_wrapper.sh` (cron job target)
Helper script for cronjob.sh to execute in correct working directory if called from outside of working directory (e.g. as cron job). Use this script as linux cron job entry. You need to change absolute path to correct TubeDB working directory.


Windows
---

#### `server.cmd`
Start (web)server. Open a browser and type `http://localhost:8080` (default). Terminate the server with key `ctrl-c`. 

#### `explorer.cmd`
Open desktop GUI for time series and meta data inspection.

#### `clear_import.cmd`
Clear all time series and import from data files and load masks from config.

#### `import.cmd`
import time series data from files and load masks from config. Does not clear previous time series.

#### `clear_load_masks.cmd`
Clear all time series masks and load from config. Does not clear time series data.

#### `interactive.cmd` (for advanced usage)
Open TubeDB shell.

#### `tsdb.cmd` (for advanced usage)
Run command in TubeDB (`tsdb.cmd [COMMAND]`). This is the entry point for other scripts to execute commands on TubeDB.