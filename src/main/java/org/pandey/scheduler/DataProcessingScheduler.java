package org.pandey.scheduler;

import org.pandey.dataextraction.service.DataProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DataProcessingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessingScheduler.class);

    @Autowired
    private final DataProcessingService dataProcessingService;

    @Autowired
    private final RetryTemplate retryTemplate;


    public DataProcessingScheduler(DataProcessingService dataProcessingService, RetryTemplate retryTemplate) {
        this.dataProcessingService = dataProcessingService;
        this.retryTemplate = retryTemplate;
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Scheduled task that executes the data processing and saving task every hour at the top of the hour.
     */
    @Scheduled(cron = "@hourly") // Runs every hour at the top of the hour
    public void pullStockData() {
        logger.info("The time is now {}", dateFormat.format(new Date()));
        logger.info("Starting scheduled data processing task.");
        try {
            retryTemplate.execute(context -> {
                dataProcessingService.executeAndSaveData();
                return null;
            });
        } catch (Exception e) {
            logger.error("Scheduled data processing task failed: ", e);
        }
    }

}
