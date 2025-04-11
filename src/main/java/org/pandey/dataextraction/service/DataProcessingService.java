package org.pandey.dataextraction.service;

import org.pandey.dataextraction.dao.NewsData;
import org.pandey.dataextraction.dao.StockWeeklyData;
import org.pandey.dataextraction.error.DataProcessingException;
import org.pandey.dataextraction.error.KafkaProducerException;
import org.pandey.dataextraction.utils.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.CompletionException;

@Service
public class DataProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessingService.class);
    private static final String STOCK_FILE_PREFIX = "stock_weekly_data_";
    private static final String NEWS_FILE_PREFIX = "news_data_";
    private static final String GCS_URI_PREFIX = "gs://bucket/";
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(5);

    private final AppMetadataService appMetadataService;
    private final WebClient webClient;
    private final KafkaProducerService kafkaProducerService;
    private final GcsStorageService gcsStorageService;

    @Value("${api.token}")
    private String apiToken;

    @Value("${api.baseUrl:'https://www.alphavantage.co/query'}")
    private String baseUrl;

    @Autowired
    public DataProcessingService(WebClient.Builder webClientBuilder,
                                 AppMetadataService appMetadataService,
                                 KafkaProducerService kafkaProducerService,
                                 GcsStorageService gcsStorageService) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
        this.appMetadataService = appMetadataService;
        this.kafkaProducerService = kafkaProducerService;
        this.gcsStorageService = gcsStorageService;
    }

    /**
     * Executes the complete data processing pipeline:
     * 1. Pulls stock and news data
     * 2. Saves data to GCS
     * 3. Updates metadata
     * 4. Sends notification
     */
    public void executeAndSaveData() throws DataProcessingException {
        LocalDate processingDate = LocalDate.now();

        logger.atInfo()
                .setMessage("Starting data processing pipeline for date: {}")
                .addArgument(processingDate)
                .log();

        try {
            Mono.zip(
                            fetchStockData(),
                            fetchNewsData()
                    )
                    .flatMap(tuple -> saveDataToGcs(tuple.getT1(), tuple.getT2(), processingDate))
                    .doOnSuccess(_ -> handleSuccess(processingDate))
                    .doOnError(e -> handleFailure(processingDate, e))
                    .block(); // Blocking call - use subscribe() in reactive applications
        } catch (CompletionException e) {
            throw unwrapCompletionException(e);
        } catch (Exception e) {
            throw new DataProcessingException("Unexpected error during data processing", e);
        }
    }

    private void handleSuccess(LocalDate processingDate) {
        try {
            updateMetadata(processingDate, "SUCCESS");
            sendNotification(processingDate, true, null);

            logger.atInfo()
                    .setMessage("Data processing completed successfully for date: {}")
                    .addArgument(processingDate)
                    .log();
        } catch (DataProcessingException e) {
            logger.atError()
                    .setMessage("Failed to update metadata or send notification for successful processing: {}")
                    .addArgument(processingDate)
                    .setCause(e)
                    .log();
            throw new CompletionException(e);
        }
    }

    private void handleFailure(LocalDate processingDate, Throwable e) {
        try {
            logger.atError()
                    .setMessage("Data processing failed for date: {}")
                    .addArgument(processingDate)
                    .setCause(e)
                    .log();

            updateMetadata(processingDate, "FAILURE");
            sendNotification(processingDate, false, e.getMessage());
        } catch (Exception ex) {
            logger.atError()
                    .setMessage("Failed to handle processing error for date: {}")
                    .addArgument(processingDate)
                    .setCause(ex)
                    .log();
            throw new CompletionException(ex);
        }
    }

    private DataProcessingException unwrapCompletionException(CompletionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof DataProcessingException) {
            return (DataProcessingException) cause;
        }
        return new DataProcessingException("Unexpected error during data processing", cause);
    }

    /**
     * Fetches stock data from API with retry and error handling
     */
    private Mono<StockWeeklyData> fetchStockData() {
        validateApiConfiguration();

        logger.atInfo()
                .setMessage("Fetching stock data from API")
                .addKeyValue("symbol", "IBM")
                .log();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(baseUrl)
                        .queryParam("function", "TIME_SERIES_WEEKLY_ADJUSTED")
                        .queryParam("symbol", "IBM")
                        .queryParam("apikey", apiToken)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> handleErrorResponse(response, "Client error fetching stock data")
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> handleErrorResponse(response, "Server error fetching stock data")
                )
                .bodyToMono(StockWeeklyData.class)
                .retryWhen(buildRetryStrategy())
                .doOnSuccess(_ -> logger.atDebug()
                        .setMessage("Successfully fetched stock data")
                        .addKeyValue("symbol", "IBM")
                        .log())
                .doOnError(e -> logger.atError()
                        .setMessage("Failed to fetch stock data")
                        .addKeyValue("symbol", "IBM")
                        .setCause(e)
                        .log());
    }

    private Mono<Throwable> handleErrorResponse(ClientResponse response, String errorPrefix) {
        return response.createException()
                .flatMap(error -> {
                    HttpStatusCode statusCode = response.statusCode();
                    String errorMsg = String.format("%s: %s - %s",
                            errorPrefix,
                            statusCode.value(),
                            statusCode);

                    logger.atError()
                            .setMessage(errorMsg)
                            .addKeyValue("statusCode", statusCode.value())
                            .addKeyValue("statusText", statusCode)
                            .addKeyValue("headers", response.headers().asHttpHeaders())
                            .setCause(error)
                            .log();

                    return Mono.error(new DataProcessingException(errorMsg, error));
                });
    }

    private Retry buildRetryStrategy() {
        return Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                .filter(this::isRetryableError)
                .doBeforeRetry(retry -> logger.atWarn()
                        .setMessage("Retrying stock data fetch")
                        .addKeyValue("attempt", retry.totalRetries() + 1)
                        .addKeyValue("maxAttempts", MAX_RETRIES)
                        .log())
                .onRetryExhaustedThrow((_, _) -> {
                    String errorMsg = String.format("Failed to fetch stock data after %d attempts", MAX_RETRIES);
                    logger.atError()
                            .setMessage(errorMsg)
                            .addKeyValue("maxAttempts", MAX_RETRIES)
                            .log();
                    return new DataProcessingException(errorMsg);
                });
    }

    /**
     * Fetches news data from API with retry and error handling
     */
    private Mono<NewsData> fetchNewsData() {
        validateApiConfiguration();

        logger.atInfo()
                .setMessage("Fetching news data from API")
                .addKeyValue("ticker", "IBM")
                .log();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(baseUrl)
                        .queryParam("function", "NEWS_SENTIMENT")
                        .queryParam("tickers", "IBM")
                        .queryParam("apikey", apiToken)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> handleErrorResponse(response, "Client error fetching news data")
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> handleErrorResponse(response, "Server error fetching news data")
                )
                .bodyToMono(NewsData.class)
                .retryWhen(buildNewsRetryStrategy())
                .doOnSuccess(_ -> logger.atDebug()
                        .setMessage("Successfully fetched news data")
                        .addKeyValue("ticker", "IBM")
                        .log())
                .doOnError(e -> logger.atError()
                        .setMessage("Failed to fetch news data")
                        .addKeyValue("ticker", "IBM")
                        .setCause(e)
                        .log());
    }

    private Retry buildNewsRetryStrategy() {
        return Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                .filter(this::isRetryableError)
                .doBeforeRetry(retry -> logger.atWarn()
                        .setMessage("Retrying news data fetch")
                        .addKeyValue("attempt", retry.totalRetries() + 1)
                        .addKeyValue("maxAttempts", MAX_RETRIES)
                        .log())
                .onRetryExhaustedThrow((_, _) -> {
                    String errorMsg = String.format("Failed to fetch news data after %d attempts", MAX_RETRIES);
                    logger.atError()
                            .setMessage(errorMsg)
                            .addKeyValue("maxAttempts", MAX_RETRIES)
                            .log();
                    return new DataProcessingException(errorMsg);
                });
    }

    /**
     * Saves data to Google Cloud Storage
     */
    private Mono<Void> saveDataToGcs(StockWeeklyData stockData, NewsData newsData, LocalDate date) {
        return Mono.fromCallable(() -> {
            try {
                String stockFileName = STOCK_FILE_PREFIX + date;
                String newsFileName = NEWS_FILE_PREFIX + date;

                byte[] stockDataBytes = SerializeUtil.serializeToJsonBytes(stockData);
                byte[] newsDataBytes = SerializeUtil.serializeToJsonBytes(newsData);

                gcsStorageService.writeDataToGcs(stockFileName, stockDataBytes);
                gcsStorageService.writeDataToGcs(newsFileName, newsDataBytes);

                logger.info("Successfully saved data to GCS for date: {}", date);
                return null;
            } catch (Exception e) {
                throw new DataProcessingException("Failed to save data to GCS", e);
            }
        });
    }

    /**
     * Updates metadata with processing status
     */
    private void updateMetadata(LocalDate date, String status) throws DataProcessingException {
        try {
            logger.info("Updating metadata for date: {} with status: {}", date, status);
            String fileLocation = GCS_URI_PREFIX + STOCK_FILE_PREFIX + date;
            appMetadataService.insertMetadata(date, status, fileLocation);
        } catch (Exception e) {
            throw new DataProcessingException("Failed to update metadata", e);
        }
    }

    /**
     * Sends processing notification
     */
    private void sendNotification(LocalDate date, boolean success, String errorMessage) {
        String message = success
                ? String.format("Data processing completed successfully for %s", date)
                : String.format("Data processing failed for %s: %s", date, errorMessage);

        try {
            logger.info("Sending notification: {}", message);
            kafkaProducerService.sendMessage(message);
        } catch (KafkaException e) {
            throw new KafkaProducerException("Failed to send Kafka notification", e);
        }
    }

    /**
     * Validates if an error is retryable
     */
    private boolean isRetryableError(Throwable error) {
        return error instanceof WebClientResponseException &&
                ((WebClientResponseException) error).getStatusCode().is5xxServerError();
    }

    /**
     * Validates API configuration
     */
    private void validateApiConfiguration() {
        if (!StringUtils.hasText(apiToken)) {
            throw new IllegalStateException("API token is not configured");
        }
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalStateException("Base URL is not configured");
        }
    }
}