package org.pandey.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(StockScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(cron = "@hourly")
    public void pullStockData() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }
}
