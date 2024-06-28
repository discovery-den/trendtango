package org.pandey.dataextraction.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Represents weekly adjusted time series data for a stock.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockWeeklyData {

    @JsonProperty("Meta Data")
    private MetaData metaData;

    @JsonProperty("Weekly Adjusted Time Series")
    private Map<String, WeeklyData> weeklyAdjustedTimeSeries;

    /**
     * MetaData inner class representing metadata for the stock data.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MetaData {
        @JsonProperty("1. Information")
        private String information;

        @JsonProperty("2. Symbol")
        private String symbol;

        @JsonProperty("3. Last Refreshed")
        private String lastRefreshed;

        @JsonProperty("4. Time Zone")
        private String timeZone;
    }

    /**
     * WeeklyData inner class representing weekly data points.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class WeeklyData {
        @JsonProperty("1. open")
        private String open;

        @JsonProperty("2. high")
        private String high;

        @JsonProperty("3. low")
        private String low;

        @JsonProperty("4. close")
        private String close;

        @JsonProperty("5. adjusted close")
        private String adjustedClose;

        @JsonProperty("6. volume")
        private String volume;

        @JsonProperty("7. dividend amount")
        private String dividendAmount;
    }
}

