package org.pandey.dataextraction.service;

import org.pandey.dataextraction.dao.NewsData;
import org.pandey.dataextraction.dao.StockWeeklyData;
import org.pandey.dataextraction.error.DataProcessingException;
import org.pandey.dataextraction.error.KafkaProducerException;
import org.pandey.dataextraction.error.RestClientRuntimeException;
import org.pandey.dataextraction.utils.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.kafka.KafkaException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * DataProcessingService.java
 * <p>
 * This is the main service class responsible for the following operations:
 * - Pulling stock and news data from various sources
 * - Updating metadata related to the pulled data
 * - Sending notification messages on a Kafka topic
 * <p>
 * The class handles the orchestration of data extraction processes, ensuring that the
 * data is fetched, processed, and communicated to other systems via Kafka efficiently.
 * <p>
 * Main responsibilities:
 * - Retrieve stock data from specified APIs or web sources
 * - Fetch news data relevant to stocks or financial markets
 * - Update metadata in the system to keep track of the data extraction status
 * - Send notification messages to a specified Kafka topic to notify other systems about
 * the availability of new data
 * <p>
 * Dependencies:
 * - Kafka producer for sending messages
 * - HTTP client or web scraping tools for data extraction
 * - Database or metadata storage system
 * <p>
 * Usage example:
 * {@code
 * DataProcessingService controller = new DataProcessingService();
 * controller.pullStockData();
 * controller.pullNewsData();
 * controller.updateMetadata();
 * controller.sendNotification();
 * }
 * <p>
 * Note: Ensure that all the necessary configurations for data sources and Kafka are set
 * up correctly before using this controller.
 * <p>
 * Author: Anil Pandey
 * Date: 25.07.2024
 */

@Service
public class DataProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessingService.class);

    @Autowired
    private final AppMetadataService appMetadataService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private final RestClient restClient;

    @Autowired
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    private final GcsStorageService gcsStorageService;

    @Value("${api.token}")
    private String apiToken;

    @Value("${api.baseUrl:'https://www.alphavantage.co/query'}")
    private String baseUrl;

    public DataProcessingService(RestClient restClient, AppMetadataService appMetadataService, KafkaProducerService kafkaProducerService, GcsStorageService gcsStorageService) {
        this.restClient = restClient;
        this.appMetadataService = appMetadataService;
        this.kafkaProducerService = kafkaProducerService;
        this.gcsStorageService = gcsStorageService;
    }

    @Async
    public CompletableFuture<StockWeeklyData> pullStockDataAsync() {
        logger.info("Starting async pull of stock data");
        return CompletableFuture.supplyAsync(this::pullStockData);
    }

    @Async
    public CompletableFuture<NewsData> pullNewsDataAsync() {
        logger.info("Starting async pull of news data");
        return CompletableFuture.supplyAsync(this::pullNewsData);
    }

    /**
     * Executes the process of pulling stock and news data, saving the data to GCS, updating metadata, and sending a notification message.
     * The data is pulled in parallel using asynchronous tasks.
     *
     * @throws DataProcessingException if an error occurs during data processing
     */
    public void executeAndSaveData() throws DataProcessingException {
        logger.info("Executing and saving data process started");
        CompletableFuture<StockWeeklyData> stockDataFuture = CompletableFuture.supplyAsync(this::pullStockData);
        CompletableFuture<NewsData> newsDataFuture = CompletableFuture.supplyAsync(this::pullNewsData);

        CompletableFuture.allOf(stockDataFuture, newsDataFuture).thenAcceptAsync(ignored -> {
            try {
                StockWeeklyData stockData = stockDataFuture.get();
                NewsData newsData = newsDataFuture.get();

                String stockFileName = "stock_weekly_data_" + LocalDate.now();
                String newsFileName = "news_data_" + LocalDate.now();

                byte[] stockDataBytes = SerializeUtil.serializeToJsonBytes(stockData);
                byte[] newsDataBytes = SerializeUtil.serializeToJsonBytes(newsData);

                gcsStorageService.writeDataToGcs(stockFileName, stockDataBytes);
                gcsStorageService.writeDataToGcs(newsFileName, newsDataBytes);

                appMetadataService.insertMetadata(LocalDate.now(), "SUCCESS", "gs://bucket/" + stockFileName);
                appMetadataService.insertMetadata(LocalDate.now(), "SUCCESS", "gs://bucket/" + newsFileName);

                sendMessage("Data saved successfully for " + LocalDate.now());
                logger.info("Data saved successfully for {}", LocalDate.now());
            } catch (Exception e) {
                logger.error("Error occurred during data processing and saving: ", e);
                handleProcessingError(new DataProcessingException(e.getMessage(), e));
            }
        });
    }

    /**
     * Handles errors during the data processing and saving operations.
     *
     * @param e the exception that occurred
     */
    private void handleProcessingError(Exception e) {
        logger.error("Handling processing error: ", e);
        try {
            appMetadataService.insertMetadata(LocalDate.now(), "FAILURE", e.getMessage());
            sendMessage("Data saving failed for " + LocalDate.now() + ": " + e.getMessage());
            logger.error("Data saving failed for {}: {}", LocalDate.now(), e.getMessage());
        } catch (Exception ex) {
            logger.error("Error handling processing error: ", ex);
        }
    }

    /**
     * Pulls stock data from the specified API base URL.
     *
     * @return The Stock entity containing the stock data.
     */
    private StockWeeklyData pullStockData() {
        logger.info("Pulling stock data from API");
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("function", "TIME_SERIES_WEEKLY_ADJUSTED")
                .queryParam("symbol", "IBM")
                .queryParam("apikey", apiToken).build()
                .toUri();

        return restClient.get().uri(uri).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                            throw new RestClientRuntimeException("Error occurred while fetching stock data", response.getStatusCode());
                        }
                ).body(StockWeeklyData.class);
    }

    /**
     * Pull News data from the specified api base url
     *
     * @return News data entity containing the News Data
     */
    private NewsData pullNewsData() {
        logger.info("Pulling news data from API");
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("function", "NEWS_SENTIMENT")
                .queryParam("tickers", "IBM")
                .queryParam("apikey", apiToken).build()
                .toUri();

        return restClient.get().uri(uri).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                            throw new RestClientRuntimeException("Error occurred while fetching news data", response.getStatusCode());
                        }
                ).body(NewsData.class);
    }

    /**
     * Inserts a new metadata entry into the database.
     * <p>
     * This method creates a new App Metadata entity with the specified date, status, and file location,
     * and saves it to the database using the MetadataRepository.
     * </p>
     *
     * @param status       the status of the metadata entry
     * @param fileLocation the file location associated with the metadata entry
     */
    private void updateMetadata(String status, String fileLocation) {
        logger.info("Updating metadata with status: {} and file location: {}", status, fileLocation);
        appMetadataService.insertMetadata(LocalDate.parse(dateFormat.format(new Date())), status, fileLocation);
    }

    /**
     * Sends a message to a Kafka topic.
     * <p>
     * This method takes a message as a string and sends it to a predefined Kafka topic.
     * <p>
     *
     * @param message The message to be sent to the Kafka topic. Must not be null.
     * @throws IllegalArgumentException if the message is null or empty.
     */
    private void sendMessage(String message) {
        logger.info("Sending message to Kafka: {}", message);
        try {
            if (message == null || message.isEmpty()) {
                throw new IllegalArgumentException("Message must not be null or empty");
            }
            kafkaProducerService.sendMessage(message);
        } catch (IllegalArgumentException e) {
            throw new KafkaProducerException("Invalid message: " + e.getMessage(), e);
        } catch (KafkaException e) {
            throw new KafkaProducerException("Error while sending message to Kafka: " + e.getMessage(), e);
        }
    }
}

