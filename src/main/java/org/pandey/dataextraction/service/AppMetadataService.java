package org.pandey.dataextraction.service;

import org.pandey.dataextraction.dao.JobMetadata;
import org.pandey.dataextraction.error.MetadataException;
import org.pandey.dataextraction.repo.MetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class AppMetadataService {
    private static final Logger logger = LoggerFactory.getLogger(AppMetadataService.class);
    private static final String METADATA_INSERT_OPERATION = "metadata_insert";

    private final MetadataRepository metadataRepository;

    @Autowired
    public AppMetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Transactional
    public void insertMetadata(LocalDate date, String status, String fileLocation) throws MetadataException {
        validateInputParameters(date, status, fileLocation);

        logger.atDebug()
                .setMessage("Attempting to insert metadata")
                .addKeyValue("operation", METADATA_INSERT_OPERATION)
                .addKeyValue("date", date)
                .addKeyValue("status", status)
                .addKeyValue("fileLocation", fileLocation)
                .log();

        try {
            JobMetadata jobMetadata = new JobMetadata(date, status, fileLocation);
            metadataRepository.save(jobMetadata);

            logger.atInfo()
                    .setMessage("Successfully inserted metadata")
                    .addKeyValue("operation", METADATA_INSERT_OPERATION)
                    .addKeyValue("date", date)
                    .addKeyValue("metadataId", jobMetadata.getId())
                    .log();
        } catch (DataAccessException dae) {
            handleDataAccessException(date, dae);
        } catch (Exception e) {
            handleUnexpectedException(date, e);
        }
    }

    private void validateInputParameters(LocalDate date, String status, String fileLocation) {
        if (date == null) {
            logger.atError()
                    .setMessage("Validation failed: date cannot be null")
                    .addKeyValue("operation", METADATA_INSERT_OPERATION)
                    .log();
            throw new IllegalArgumentException("Date cannot be null");
        }

        if (!StringUtils.hasText(status)) {
            logger.atError()
                    .setMessage("Validation failed: status cannot be empty")
                    .addKeyValue("operation", METADATA_INSERT_OPERATION)
                    .addKeyValue("date", date)
                    .log();
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        if (!StringUtils.hasText(fileLocation)) {
            logger.atError()
                    .setMessage("Validation failed: file location cannot be empty")
                    .addKeyValue("operation", METADATA_INSERT_OPERATION)
                    .addKeyValue("date", date)
                    .addKeyValue("status", status)
                    .log();
            throw new IllegalArgumentException("File location cannot be null or empty");
        }

        if (date.isAfter(LocalDate.now())) {
            logger.atWarn()
                    .setMessage("Future date detected in metadata")
                    .addKeyValue("operation", METADATA_INSERT_OPERATION)
                    .addKeyValue("date", date)
                    .log();
        }
    }

    private void handleDataAccessException(LocalDate date, DataAccessException dae) {
        String errorMessage = "Database operation failed";

        logger.atError()
                .setMessage(errorMessage)
                .addKeyValue("operation", METADATA_INSERT_OPERATION)
                .addKeyValue("date", date)
                .setCause(dae)
                .log();

        throw new MetadataException(errorMessage, dae);
    }

    private void handleUnexpectedException(LocalDate date, Exception e) {
        String errorMessage = "Unexpected error during metadata operation";

        logger.atError()
                .setMessage(errorMessage)
                .addKeyValue("operation", METADATA_INSERT_OPERATION)
                .addKeyValue("date", date)
                .setCause(e)
                .log();

        throw new MetadataException(errorMessage, e);
    }
}