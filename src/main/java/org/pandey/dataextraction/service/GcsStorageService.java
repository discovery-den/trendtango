package org.pandey.dataextraction.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.pandey.dataextraction.error.GcsOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Service class for interacting with Google Cloud Storage (GCS).
 */
@Service
public class GcsStorageService {
    private static final Logger logger = LoggerFactory.getLogger(GcsStorageService.class);
    private static final String GCS_OPERATION = "gcs_write_operation";

    private final Storage storage;
    private final String bucketName;

    @Autowired
    public GcsStorageService(Storage storage,
                             @Value("${gcs.bucket.name}") String bucketName) {
        this.storage = Objects.requireNonNull(storage, "Storage client cannot be null");
        this.bucketName = Objects.requireNonNull(bucketName, "Bucket name cannot be null");
    }

    /**
     * Writes data to a file in Google Cloud Storage (GCS).
     *
     * @param fileName the name of the file to create or overwrite (must not be empty)
     * @param content  the content to write to the file (must not be null)
     * @throws GcsOperationException if the operation fails
     * @throws IllegalArgumentException if input parameters are invalid
     */
    public void writeDataToGcs(String fileName, byte[] content) throws GcsOperationException {
        validateInputParameters(fileName, content);

        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

            logger.atDebug()
                    .setMessage("Attempting to write file to GCS")
                    .addKeyValue("operation", GCS_OPERATION)
                    .addKeyValue("bucket", bucketName)
                    .addKeyValue("fileName", fileName)
                    .addKeyValue("contentSize", content.length)
                    .log();

            Blob blob = storage.create(blobInfo, inputStream);

            logger.atInfo()
                    .setMessage("File successfully uploaded to GCS")
                    .addKeyValue("operation", GCS_OPERATION)
                    .addKeyValue("bucket", bucketName)
                    .addKeyValue("fileName", fileName)
                    .addKeyValue("blobSize", blob.getSize())
                    .addKeyValue("blobGeneration", blob.getGeneration())
                    .log();

        } catch (StorageException e) {
            handleStorageException(fileName, e);
        } catch (IOException e) {
            handleIOException(fileName, e);
        } catch (Exception e) {
            handleUnexpectedException(fileName, e);
        }
    }

    private void validateInputParameters(String fileName, byte[] content) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
    }

    private void handleStorageException(String fileName, StorageException e) throws GcsOperationException {
        String errorMsg = String.format("GCS operation failed for file %s", fileName);

        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", GCS_OPERATION)
                .addKeyValue("bucket", bucketName)
                .addKeyValue("fileName", fileName)
                .addKeyValue("gcsStatusCode", e.getCode())
                .addKeyValue("gcsReason", e.getReason())
                .setCause(e)
                .log();

        throw new GcsOperationException(errorMsg, e);
    }

    private void handleIOException(String fileName, IOException e) throws GcsOperationException {
        String errorMsg = String.format("IO error while writing file %s to GCS", fileName);

        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", GCS_OPERATION)
                .addKeyValue("bucket", bucketName)
                .addKeyValue("fileName", fileName)
                .setCause(e)
                .log();

        throw new GcsOperationException(errorMsg, e);
    }

    private void handleUnexpectedException(String fileName, Exception e) throws GcsOperationException {
        String errorMsg = String.format("Unexpected error while writing file %s to GCS", fileName);

        logger.atError()
                .setMessage(errorMsg)
                .addKeyValue("operation", GCS_OPERATION)
                .addKeyValue("bucket", bucketName)
                .addKeyValue("fileName", fileName)
                .setCause(e)
                .log();

        throw new GcsOperationException(errorMsg, e);
    }
}