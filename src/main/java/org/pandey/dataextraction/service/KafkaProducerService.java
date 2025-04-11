package org.pandey.dataextraction.service;

import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.TimeoutException;
import org.pandey.dataextraction.error.KafkaProducerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Service for producing messages to a Kafka topic with reliable delivery.
 */
@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "event_update";
    private static final long PRODUCE_TIMEOUT_MS = 5000;
    private static final String KAFKA_OPERATION = "kafka_produce";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "KafkaTemplate cannot be null");
    }

    /**
     * Send a message to the Kafka topic with synchronous confirmation.
     *
     * @param message the message to send (must not be null or empty)
     * @throws KafkaProducerException if message delivery fails
     * @throws IllegalArgumentException if message is invalid
     */
    public void sendMessage(String message) throws KafkaProducerException {
        validateMessage(message);

        try {
            logger.atDebug()
                    .setMessage("Attempting to send Kafka message")
                    .addKeyValue("operation", KAFKA_OPERATION)
                    .addKeyValue("topic", TOPIC)
                    .addKeyValue("messageLength", message.length())
                    .log();

            SendResult<String, String> result = kafkaTemplate.send(TOPIC, message)
                    .get(PRODUCE_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            logSuccessfulDelivery(result);

        } catch (InterruptedException e) {
            handleInterruptedException(message, e);
        } catch (ExecutionException e) {
            handleExecutionException(message, e);
        } catch (Exception e) {
            handleUnexpectedException(message, e);
        }
    }

    private void validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
    }

    private void logSuccessfulDelivery(SendResult<String, String> result) {
        logger.atInfo()
                .setMessage("Successfully delivered Kafka message")
                .addKeyValue("operation", KAFKA_OPERATION)
                .addKeyValue("topic", TOPIC)
                .addKeyValue("partition", result.getRecordMetadata().partition())
                .addKeyValue("offset", result.getRecordMetadata().offset())
                .addKeyValue("timestamp", result.getRecordMetadata().timestamp())
                .log();
    }

    private void handleInterruptedException(String message, InterruptedException e) {
        String errorMsg = String.format("Message sending interrupted for topic %s", TOPIC);
        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", KAFKA_OPERATION)
                .addKeyValue("topic", TOPIC)
                .addKeyValue("message", truncateMessage(message))
                .setCause(e)
                .log();
        Thread.currentThread().interrupt();
        throw new KafkaProducerException(errorMsg, e);
    }

    private void handleExecutionException(String message, ExecutionException e) {
        String errorMsg = String.format("Failed to send message to topic %s", TOPIC);
        if (e.getCause() instanceof AuthorizationException) {
            errorMsg = "Kafka authorization failed";
        } else if (e.getCause() instanceof TimeoutException) {
            errorMsg = "Kafka broker timeout";
        }

        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", KAFKA_OPERATION)
                .addKeyValue("topic", TOPIC)
                .addKeyValue("message", truncateMessage(message))
                .setCause(e.getCause())
                .log();

        throw new KafkaProducerException(errorMsg, e.getCause());
    }

    private void handleUnexpectedException(String message, Exception e) {
        String errorMsg = String.format("Unexpected error sending to topic %s", TOPIC);
        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", KAFKA_OPERATION)
                .addKeyValue("topic", TOPIC)
                .addKeyValue("message", truncateMessage(message))
                .setCause(e)
                .log();

        throw new KafkaProducerException(errorMsg, e);
    }

    private String truncateMessage(String message) {
        return message.length() > 100 ? message.substring(0, 100) + "..." : message;
    }
}