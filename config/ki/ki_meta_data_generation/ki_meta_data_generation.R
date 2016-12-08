library(data.table)

elevation <- fread("ki_plot_elevation.csv", col.names=c("plot","elevation"))
position <- fread("ki_plot_position.csv", select=c("PlotID","Easting","Northing","Lat","Lon"), col.names=c("plot","easting","northing","lat","lon"))
master <- fread("ki_station_master.csv", select=c("PlotID", "FocalPlot"), col.names=c("plot","focal"))
general <- fread("ki_plot_general.csv")


inventory <- merge(position, elevation, all=TRUE)
inventory <- merge(inventory, master, all=TRUE)
inventory <- merge(inventory, general, all=TRUE)

inventory$focal[is.na(inventory$focal)] <- "N"

setkey(inventory, plot)

setcolorder(inventory, c("plot","general", "focal", "lat", "lon", "easting", "northing", "elevation"))

fwrite(inventory, "plot_inventory.csv")
