package org.pandey.dataextraction.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Service class for interacting with Google Cloud Storage (GCS).
 */
@Service
public class GcsStorageService {

    private static final Logger logger = LoggerFactory.getLogger(GcsStorageService.class);

    @Autowired
    private Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    /**
     * Writes data to a file in Google Cloud Storage (GCS).
     *
     * @param fileName the name of the file to create or overwrite
     * @param content  the content to write to the file
     * @return the name of the file saved in GCS if successful, or null if an error occurred
     */
    public String writeDataToGcs(String fileName, byte[] content) {
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
            Blob blob = storage.create(blobInfo, inputStream);
            logger.info("File {} uploaded to GCS successfully.", fileName);
            return blob.getName();
        } catch (Exception e) {
            logger.error("Failed to write data to GCS bucket: {}", e.getMessage());
            return null;
        }
    }
}


