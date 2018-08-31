---
title: "Import Formats"
---

TubeDB contains readers for several timeseries data source file formats:

- `CSV` - generic comma-separated values: *text format*
- `ASC` - specific logger: *text format*
- `UDBF` - specific logger: *binary  format*
- `TSA` - TubeDB timeseries archive: *binary format*


CSV - Comma Separated Values
---

Generic CSV text transfer format of timeseries data. 

Files created by API method `query_csv` are in generic csv format.

This format may be used as intermediate representation of station timeseries data. In a first step the logger specific format (e.g. specific CSV-Format) may be transformed by external tools to generic CSV and then in a next step imported into TubeDB.

typical file pattern: `*.csv`

reader class: `tsdb.run.ImportGenericCSV`

### format specification

#### filename:

Station name of timeseries is extracted from filename. 

format: `STATION_TEXT.csv`

examples: 
- `HEG01_.csv` -> HEG01 
- `MyPlot_2010.csv` -> MyPlot 
- `123_old.csv` -> 123

#### header:

First line of file content is header. 

First columns is date and following columns contain sensor names.

format: `datetime,SENSOR1,SENSOR2,SENSOR2,...`

examples: 
- `datetime,Ta_200,rH_200,p_QNH,WV,WD,P_RT_NRT_01,Trad,SWDR_300,SWUR_300,LWDR_300,LWUR_300,Rn_300`
- `datetime,Ta_200,rH_200`

#### data-rows:

format: `DATETIME,VALUE1,VALUE2,VALUE3` 

Datetime is in format `yyyy-mm-ddThh:MM` *(ISO 8601)*  e.g. `2014-10-12T09:50`


#### complete example:

filename: `aet1_2014__2015_11_05.csv` -> plot: aet1

file content:

`datetime,Ta_200,rH_200`

`2014-01-01T00:10,-9,86.1`

`2014-01-01T00:20,-9.1,86`

`2014-01-01T00:30,-9.1,86`


ASC
---

typical file pattern: `*.asc`

reader class: `tsdb.loader.ki.AscParser`


UDBF - Universal-Data-Bin-File
---

Files of version 1.07 can be read.

typical file pattern: `*.dat`

reader class: `tsdb.loader.be.UniversalDataBinFile`


TSA - Time-Series-Archiv
---

TubeDB binary timeseries archiv format. 

This format can be used to write (parts of) TubeDB to File for archive purposes and later read it into another TubeDB instance.

This format may be an alternative to CSV. Advantages over CSV are more compact representation (smaller file size, only one file instead of one file per plot), much faster write/read and better integration of metadata (plot names, sensor names).


typical file pattern: `*.tsa`

reader class: `tsdb.TimeSeriesArchivReader`

writer class: `tsdb.TimeSeriesArchivWriter`

### format specification

data type definitions:
- *(byte)* one byte
- *(int)* 32 bit integer (four bytes big-endian)
- *(packed_int)* packed integer number (one to five bytes): Sequence of bytes. If highest bit of current byte is set then next byte is part of this sequence. Lower 7 bits of byte are used to code value: bn denotes the 7 value bits of byte n: interger `value == b1 | (b2<<7) | (b3<<14)`
- *(text)*  sequence of characters coded by count of characters followed by characters as bytes: *(packed_int)* *(byte)* *(byte)* *(byte)* ...
- marker is coded as *(text)*
- *(float)* IEEE 754 single-precision binary floating-point format 32 bit (four bytes big-endian)


marker definitions:
- `TOC_HEAD` = "`Time_Series_Archiv_v_1_0_0`"
- `TOC_START` = "`TimeSeriesArchiv:start`"
- `TOC_END` = "`TimeSeriesArchiv:end`"
- `TOC_ENTRY` = "`Entry`"
- `TOC_TYPE_TIMESTAMPSERIES` = "`TimestampSeries`"
- `TOC_TYPE_DATAENTRYARRAY` = "`DataEntryArray`"

File content starts (at position 0) with `TOC_HEAD` then `TOC_START` , contains one ore more entries and ends with `TOC_END`.

An entry starts with `TOC_ENTRY` and then entry type `TOC_TYPE_TIMESTAMPSERIES` or `TOC_TYPE_DATAENTRYARRAY`.

#### entry `TOC_TYPE_DATAENTRYARRAY`:

This entry stores one time series of one station and one sensor.

marker definitions:
- `TOC_START` = "`DataEntryArray:start`"
- `TOC_END` = "`DataEntryArray:end`"

content:
 
*(text)*[station name] 

*(text)*[sensor name] 

`TOC_START`

*(packed_int)*[count of entries]

*(int)*\[timestamp] 

*(float)*\[value]

*(int)*\[timestamp] 

*(float)*\[value]

*(int)*\[timestamp] 

*(float)*\[value]

...

`TOC_END`


#### entry `TOC_TYPE_TIMESTAMPSERIES`:

This entry stores one time series of one station and several sensors.

marker definitions:
- `TOC_START` = "`TimestampSeries:start`";
- `TOC_END` = "`TimestampSeries:end`";

content:

`TOC_START`

*(text)*[station name]

*(packed_int)*[count of sensors]

*(text)*[sensor name 1]

*(text)*[sensor name 2]

...

*(packed_int)*[count of entries]

*(int)*\[timestamp] 

*(float)*\[value 1] 

*(float)*\[value 2] 

...

*(int)*\[timestamp] 

*(float)*\[value 1] 

*(float)*\[value 2] 

...

...

`TOC_END`

