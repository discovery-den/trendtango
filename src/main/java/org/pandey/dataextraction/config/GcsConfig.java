package org.pandey.dataextraction.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Configuration class for setting up Google Cloud Storage (GCS) client.
 */
@Configuration
public class GcsConfig {

    @Value("${gcs.secret.path}")
    private String gcsSecretPath;

    /**
     * Creates and configures the GCS storage client.
     *
     * @return The configured GCS storage client.
     * @throws IOException If an I/O error occurs.
     */
    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(gcsSecretPath));
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}
