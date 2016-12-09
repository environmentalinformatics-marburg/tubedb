library(data.table)

#be_i_full <- fread("be_station_inventory.csv", header=TRUE)
be_i <- fread("be_station_inventory.csv", header=TRUE, select=c("PLOTID","TYPE","LOGGER"), col.names=c("plot","focal","logger"))

be_m <- fread("be_station_master.csv", header=TRUE, select=c("EP_Plotid", "PlotID", "Lat", "Lon"), col.names=c("plot", "alternative_id", "lat", "lon"))

be_i$focal[be_i$focal=="VIP"] <- "Y"   
be_i$focal[be_i$focal=="EP"] <- "N"

be_i$general <- "__???__"
be_i$general[be_i$plot %like% "AEG"] <- "AEG"
be_i$general[be_i$plot %like% "AEW"] <- "AEW"
be_i$general[be_i$plot %like% "SEG"] <- "SEG"
be_i$general[be_i$plot %like% "SEW"] <- "SEW"
be_i$general[be_i$plot %like% "HEG"] <- "HEG"
be_i$general[be_i$plot %like% "HEW"] <- "HEW"
be_i$general[be_i$plot %like% "AET"] <- "AEW"
be_i$general[be_i$plot %like% "SET"] <- "SEW"
be_i$general[be_i$plot %like% "HET"] <- "HEW"

be_final <- merge(be_i, be_m, all.x=TRUE)

be_final$is_station <- "Y"

setcolorder(be_final, c("plot","general","lat","lon","focal","is_station","logger","alternative_id"))

setorder(be_final, plot)

fwrite(be_final, "plot_inventory.csv")

