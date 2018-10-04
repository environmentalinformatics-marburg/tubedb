data <- data.table::fread("plot_inventory_source.csv")

data$plot <- paste0(data$plot, "f")
data$general <- paste0(data$general, "f")
data$logger <- "filled"
data$alternative_id <- NULL

data.table::fwrite(data, "plot_inventory_result.csv")
