# Distribution

## folders and files within distribution

### config

Meta data configuration of TubeDB.

### lib

External libraries needed to run TubeDB.

### log

Application log-files. (will be created by running TubeDB)

### source

Default location of raw source time series data files.

### storage

Timeseries-Database files. (will be created by running TubeDB)

### webcontent

Web based user interface files for client side (e.g. HTML, JavaScript).

### webFiles

User generated content that is included in Web user interface.

`info.html` Content will be shown at web user interface main page in 'Infobox'. It may contain textual information about collected data.

In subfolder `supplement` contained files and folders will be shown at web user interface 'auxiliary files'-page. It may contain additional content for end users like project information files.

### root folder

`realm.properties` usernames and passwords to access TubeDB if it is configured to require access control.

`tsdb.jar` Compiled TubeDB application.

`tsdb_paths.ini` Configuration of TubeDB. Included documentation.


