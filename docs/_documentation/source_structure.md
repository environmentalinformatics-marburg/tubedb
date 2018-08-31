---
title: "source structure"
---

### package tsdb
Base package of TubeDB.

#### class TsDB:
Central class of TubeDB.

#### class TsDBFactory:
Factory to create instance of TsDB.

### package tsdb.component
Selfcontained components of TsDB, that do not contain references to TsDB object.

### package tsdb.explorer
Desktop GUI of TsDB (no web interface).

#### class Explorer:
Entry point of GUI application.


### package tsdb.graph

Contains nodes of on demand processing graph.

#### class QueryPlan:
Functionality to create default processing graphs based on parameters.


### package tsdb.iterator
Iterators which process data. A processing graph node produces iterators.


### package tsdb.loader
Converters of logger input formats to database entries.

### package tsdb.remote
Interface to TsDB for "external" components like Explorer and web-server.
Interface is callable from a remote computer by Java-RMI.

#### Interface RemoteTsDB:
The interface to TsDB.

#### class ServerTsDB:
Local implementation of RemoteTsDB.

#### class StartServerTsDB:
Entry point to start a TsDB instance that is callable remotely by Java-RMI.

#### class ZipExport:
Creation of zip-files with data of TsDB (connects to RemoteTsDB).

### package tsdb.run
Several entry points for typical running tasks of TsDB.

### package tsdb.streamdb
StreamDB is the storage backend of TsDB.

### package tsdb.testing
Some functionality checking of TsDB.

### package tsdb.usecase
Use cases of data processing with TsDB.

### package tsdb.util
Utility functionality and building blocks for TsDB components.

### package tsdb.util.gui
Serverside visualisation functionality of TsDB.
Creation of diagrams and heatmaps.

### package tsdb.web
HTTP web server.

#### class Main:
Entry point to start web server.

#### class Run:
Entry point to start web server and RMI server.

### package tsdb.web.api
Handlers for HTTP calls to the web API of TsDB.

### package tsdb.web.util
Helper functionality for loggers.

### package tsdb.web.generator
Helper classes to generate HTML pages.