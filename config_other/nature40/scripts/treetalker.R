# Generate TreeTalker metadata from RSDB TreeTalker vector layer.

# Install RSDB package and automatically install updated versions.
if(!require("remotes")) install.packages("remotes")
remotes::install_github("environmentalinformatics-marburg/rsdb/r-package")
# In some cases a restart of R is needed to work with a updated version of RSDB package (in RStudio - Session - Terminate R).

# create connection to RSDB server.
url <- 'https://example.com:8080'
userpwd <- 'user:password'
remotesensing <- RSDB::RemoteSensing$new(url, userpwd=userpwd)

# get documentation
?'RSDB-package'

# list vector layers
remotesensing$vectordbs

# open cst_tt vector layer
vectordb <- remotesensing$vectordb("cst_tt")

# get vector data as WGS84
df <- vectordb$getVectorsWGS84()

# remove duplicates: keep last name-serial entry only
composite_keys <- paste0(df$name, ".", df$Serial.Number.Treetalker)
dups <- duplicated(composite_keys, fromLast = TRUE)
df <- df[!dups,]
df <- df[order(df$name),]

df$serial <- paste0("tt_", df$Serial.Number.Treetalker)

station_inventory <- data.frame(plot=df$name, logger="treetalker_logger", serial=df$serial, start="*", end="*")
station_inventory <- rbind(station_inventory, data.frame(plot="mof_node_C2030119", logger="treetalker_node", serial="tt_C2030119", start="*", end="*"))
station_inventory <- rbind(station_inventory, data.frame(plot="mof_node_C2030115", logger="treetalker_node", serial="tt_C2030115", start="*", end="*"))
station_inventory <- rbind(station_inventory, data.frame(plot="mof_node_C2030117", logger="treetalker_node", serial="tt_C2030117", start="*", end="*"))
station_inventory <- rbind(station_inventory, data.frame(plot="mof_node_C2030123", logger="treetalker_node", serial="tt_C2030123", start="*", end="*"))

write.csv(station_inventory, "station_inventory_add.csv", row.names = FALSE, quote = FALSE)

plot_inventory <- data.frame(plot=df$name, general="treetalker_nature40", lat=df$lat, lon=df$lon, focal="", is_station="", logger="treetalker_logger", alternative_id="", elevation=df$alt, comment="")
plot_inventory <- rbind(plot_inventory, data.frame(plot="mof_node_C2030119", general="treetalker_nature40_nodes", lat="", lon="", focal="", is_station="", logger="treetalker_node", alternative_id="", elevation="", comment=""))
plot_inventory <- rbind(plot_inventory, data.frame(plot="mof_node_C2030115", general="treetalker_nature40_nodes", lat="", lon="", focal="", is_station="", logger="treetalker_node", alternative_id="", elevation="", comment=""))
plot_inventory <- rbind(plot_inventory, data.frame(plot="mof_node_C2030117", general="treetalker_nature40_nodes", lat="", lon="", focal="", is_station="", logger="treetalker_node", alternative_id="", elevation="", comment=""))
plot_inventory <- rbind(plot_inventory, data.frame(plot="mof_node_C2030123", general="treetalker_nature40_nodes", lat="", lon="", focal="", is_station="", logger="treetalker_node", alternative_id="", elevation="", comment=""))

write.csv(plot_inventory, "plot_inventory_add.csv", row.names = FALSE, quote = FALSE)
