library(data.table)

si <- fread("sa_station_inventory.csv", select=c("station","general","lat","lon"), col.names=c("plot","general","lat","lon"))

sis <- fread("sa_station_SASSCAL_inventory.csv", select=c("station","general","lat","lon"), col.names=c("plot","general","lat","lon"))

si_part <- data.table(plot=si$plot)

si_m <- merge(si_part, sis, all.x=TRUE)

si_m_na <- si_m[is.na(general)]
si_m_na <- data.table(plot=si_m_na$plot)

si_m_full <- si_m[!is.na(general)]

si_filled <- merge(si_m_na, si, all.x=TRUE)

si_final <- rbind(si_filled, si_m_full)

si_final$is_station <- "Y"

si_final$logger <- "___---??-----____"
si_final$logger[si_final$general=="SAWS"] <- "SAWS_logger"   
si_final$logger[si_final$general %like% "^SASSCAL"] <- "SASSCAL_logger" 

setorder(si_final, plot)

fwrite(si_final, "plot_inventory.csv")
