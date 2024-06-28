package org.pandey.dataextraction.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Represents market news data retrieved from AlphaVantage API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
public class NewsData {
    @JsonProperty("items")
    private String items;

    @JsonProperty("sentiment_score_definition")
    private String sentimentScoreDefinition;

    @JsonProperty("relevance_score_definition")
    private String relevanceScoreDefinition;

    @JsonProperty("feed")
    private List<Feed> feed;

    /**
     * Represents single news feed item.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
    private static class Feed {
        @JsonProperty("title")
        private String title;

        @JsonProperty("url")
        private String url;

        @JsonProperty("time_published")
        private String timePublished;

        @JsonProperty("authors")
        private List<String> authors;

        @JsonProperty("summary")
        private String summary;

        @JsonProperty("banner_image")
        private String bannerImage;

        @JsonProperty("source")
        private String source;

        @JsonProperty("category_within_source")
        private String categoryWithinSource;

        @JsonProperty("source_domain")
        private String sourceDomain;

        @JsonProperty("topics")
        private List<Topic> topics;

        @JsonProperty("overall_sentiment_score")
        private double overallSentimentScore;

        @JsonProperty("overall_sentiment_label")
        private String overallSentimentLabel;

        @JsonProperty("ticker_sentiment")
        private List<TickerSentiment> tickerSentiment;

        /**
         * Represents a topic related to the news item.
         */
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
        private static class Topic {
            @JsonProperty("topic")
            private String topic;

            @JsonProperty("relevance_score")
            private String relevanceScore;

            // Getters and Setters
        }

        /**
         * Represents sentiment related to a specific ticker in the news item.
         */
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
        private static class TickerSentiment {
            @JsonProperty("ticker")
            private String ticker;

            @JsonProperty("relevance_score")
            private String relevanceScore;

            @JsonProperty("ticker_sentiment_score")
            private String tickerSentimentScore;

            @JsonProperty("ticker_sentiment_label")
            private String tickerSentimentLabel;

            // Getters and Setters
        }
    }
}

