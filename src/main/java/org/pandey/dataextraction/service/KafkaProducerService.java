package org.pandey.dataextraction.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for producing messages to a Kafka topic.
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "event_update";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Send a message to the Kafka topic "stock".
     *
     * @param message the message to send
     */
    public void sendMessage(String message) {
        logger.info("Sending message to topic {}: {}", TOPIC, message);
        kafkaTemplate.send(TOPIC, message);
    }
}

