library(data.table)

col_cpy <- c("pu1_P_RT_NRT", "pu2_1", "pu2_2", "wxt_SWDR_300", "wxt_SWUR_300", "wxt_LWDR_300", "wxt_LWUR_300", "SERIAL_PYR01", "SERIAL_PYR02", "SERIAL_PAR01", "SERIAL_PAR02", "pu2_1_type", "pu2_2_type")
col_src <- c("PLOTID", "LOGGER", "SERIAL", "DATE_START", "DATE_END", col_cpy)
col_tgt <- c("plot", "logger", "serial", "start", "end", col_cpy)
col_cls <- c("character")
inventory <- fread("ki_station_inventory.csv", select=col_src, col.names=col_tgt, colClasses=col_cls)
inventory_full <- fread("ki_station_inventory.csv", colClasses=col_cls)

inventory$logger[inventory$logger=="000rug"] <- "rug"
inventory$logger[inventory$logger=="001rug"] <- "rug"
inventory$logger[inventory$logger=="000pu1"] <- "pu1"
inventory$logger[inventory$logger=="001pu1"] <- "pu1"
inventory$logger[inventory$logger=="002pu1"] <- "pu1"
inventory$logger[inventory$logger=="000rad"] <- "rad"
inventory$logger[inventory$logger=="001rad"] <- "rad"
inventory$logger[inventory$logger=="002rad"] <- "rad"
inventory$logger[inventory$logger=="003rad"] <- "rad"
inventory$logger[inventory$logger=="004rad"] <- "rad"
inventory$logger[inventory$logger=="000wxt"] <- "wxt"
inventory$logger[inventory$logger=="000pu2"] <- "pu2"
inventory$logger[inventory$logger=="001pu2"] <- "pu2"
inventory$logger[inventory$logger=="000tfi"] <- "tfi"
inventory$logger[inventory$logger=="000gp1"] <- "gp1"

inventory[inventory=="NaN"] <- ""

setorder(inventory, plot, logger, start, serial)

fwrite(inventory, "station_inventory.csv")

# source columns:
#
# EASTING (remove) utm easting
# NORTHING (remove) utm northing
# POINT_X (remove) unknown
# POINT_Y (remove) unknown
# TYPE (remove) general station long name
# PLOTID (transfer) plot
# LOGGER (transform) logger
# SERIAL (transfer) serial
# DATE_START (transfer) start
# DATE_END (transfer) end
# HEADER (remove) unknown
# DATA (remove) unknown
# pu1_P_RT_NRT  (copy) kili logger property
# pu2_1 (copy) kili logger property
# pu2_2 (copy) kili logger property
# wxt_SWDR_300 (copy) kili logger property
# wxt_SWUR_300 (copy) kili logger property
# wxt_LWDR_300 (copy) kili logger property
# wxt_LWUR_300 (copy) kili logger property
# cf_kd_rad (remove) unknown
# cf_par_rad (remove) unknown
# SERIAL_VAISALA (remove) unknown
# SERIAL_CNR4 (remove) unknown
# SERIAL_ARG (remove) unknown
# SERIAL_PYR01 (copy) kili logger property
# SERIAL_PYR02 (copy) kili logger property
# SERIAL_PAR01 (copy) kili logger property
# SERIAL_PAR02 (copy) kili logger property
# TF_COLOR (remove) unknown
# TF_B (remove) unknown
# TF_B01 (remove) unknown
# TF_B02 (remove) unknown
# TF_B03 (remove) unknown
# TF_B04 (remove) unknown
# TF_B05 (remove) unknown
# TF_B06 (remove) unknown
# TF_B07 (remove) unknown
# TF_B08 (remove) unknown
# Misc (remove) unknown
# pu2_1_type (copy) kili logger property
# pu2_2_type (copy) kili logger property
# pu2_1_mapping (remove) unknown
# pu2_2_mapping (remove) unknown