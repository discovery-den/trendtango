package org.pandey.dataextraction.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pandey.dataextraction.error.JsonSerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class for serializing objects to JSON byte arrays.
 */
public class SerializeUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(SerializeUtil.class);

    /**
     * Serializes an object to a JSON byte array.
     *
     * @param object the object to serialize
     * @return the serialized JSON byte array
     * @throws JsonSerializationException if the object cannot be serialized
     */
    public static byte[] serializeToJsonBytes(Object object) throws JsonSerializationException {
        try {
            logger.debug("Serializing object to JSON byte array: {}", object);
            return objectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            logger.error("Failed to serialize object to JSON", e);
            throw new JsonSerializationException("Failed to serialize object to JSON", e);
        }
    }
}
