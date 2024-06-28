package org.pandey.dataextraction.controller;

import org.pandey.dataextraction.error.RestClientRuntimeException;
import org.pandey.dataextraction.dao.WeeklyAdjustedStock;
import org.pandey.dataextraction.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClient;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * DataExtractionController.java
 * <p>
 * This is the main controller class responsible for the following operations:
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
 *   the availability of new data
 * <p>
 * Dependencies:
 * - Kafka producer for sending messages
 * - HTTP client or web scraping tools for data extraction
 * - Database or metadata storage system
 * <p>
 * Usage example:
 * {@code
 * DataExtractionController controller = new DataExtractionController();
 * controller.pullStockData();
 * controller.pullNewsData();
 * controller.updateMetadata();
 * controller.sendNotification();
 * }
 *
 * Note: Ensure that all the necessary configurations for data sources and Kafka are set
 * up correctly before using this controller.
 * <p>
 * Author: Anil Pandey
 * Date: 25.07.2024
 */

@Controller
public class DataExtractionController {

    @Autowired
    private final MetadataService metadataService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private final RestClient restClient;

    public DataExtractionController(RestClient restClient, MetadataService metadataService) {
        this.restClient = restClient;
        this.metadataService = metadataService;
    }

    /**
     * Pulls stock data from the specified API base URL.
     *
     * @param apiBaseUrl The base URL of the API to pull stock data from.
     * @return The Stock entity containing the stock data.
     */
    public WeeklyAdjustedStock pullStockData(String apiBaseUrl) {
        String apiUrl = apiBaseUrl + "/stock-data-endpoint";

        return restClient.get().uri(apiUrl).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RestClientRuntimeException("Error occurred while fetching stock data", response.getStatusCode());
                }
                ).body( WeeklyAdjustedStock.class);
    }

    /**
     * Inserts a new metadata entry into the database.
     * <p>
     * This method creates a new Metadata entity with the specified date, status, and file location,
     * and saves it to the database using the MetadataRepository.
     * </p>
     *
     * @param status the status of the metadata entry
     * @param fileLocation the file location associated with the metadata entry
     */
    private void updateMetadata(String status, String fileLocation){
        metadataService.insertMetadata(LocalDate.parse(dateFormat.format(new Date())), status, fileLocation);
    }
}
