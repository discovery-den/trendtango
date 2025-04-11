package org.pandey.dataextraction.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.pandey.dataextraction.error.JsonSerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.util.Objects;

/**
 * Utility class for JSON serialization operations with comprehensive error handling.
 */
public class SerializeUtil {
    private static final Logger logger = LoggerFactory.getLogger(SerializeUtil.class);
    private static final String SERIALIZATION_OPERATION = "json_serialization";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private SerializeUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Serializes an object to a JSON byte array with comprehensive error handling.
     *
     * @param object the object to serialize (must not be null)
     * @return the serialized JSON byte array
     * @throws JsonSerializationException if serialization fails
     * @throws IllegalArgumentException if the input object is null
     */
    public static byte[] serializeToJsonBytes(Object object) throws JsonSerializationException {
        Objects.requireNonNull(object, "Object to serialize cannot be null");

        final String objectType = ClassUtils.getQualifiedName(object.getClass());

        logger.atDebug()
                .setMessage("Attempting JSON serialization")
                .addKeyValue("operation", SERIALIZATION_OPERATION)
                .addKeyValue("objectType", objectType)
                .log();

        try {
            byte[] result = objectMapper.writeValueAsBytes(object);

            logger.atDebug()
                    .setMessage("Successfully serialized object to JSON")
                    .addKeyValue("operation", SERIALIZATION_OPERATION)
                    .addKeyValue("objectType", objectType)
                    .addKeyValue("bytesLength", result.length)
                    .log();

            return result;
        } catch (JsonProcessingException e) {
            handleJsonProcessingException(objectType, e);
        } catch (Exception e) {
            handleUnexpectedException(objectType, e);
        }
        return new byte[0]; // This line is unreachable due to exception handling
    }

    private static void handleJsonProcessingException(String objectType, JsonProcessingException e) throws JsonSerializationException {
        String errorMsg = String.format("JSON processing error for type %s", objectType);

        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", SERIALIZATION_OPERATION)
                .addKeyValue("objectType", objectType)
                .addKeyValue("errorPath", e.getMessage())
                .setCause(e)
                .log();

        throw new JsonSerializationException(errorMsg, e);
    }

    private static void handleUnexpectedException(String objectType, Exception e) throws JsonSerializationException {
        String errorMsg = String.format("Unexpected error serializing type %s", objectType);

        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", SERIALIZATION_OPERATION)
                .addKeyValue("objectType", objectType)
                .setCause(e)
                .log();

        throw new JsonSerializationException(errorMsg, e);
    }
}