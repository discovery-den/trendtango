package org.pandey.dataextraction.service;

import org.pandey.dataextraction.dao.Metadata;
import org.pandey.dataextraction.repo.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


/**
 * Service class for managing {@link Metadata} entities.
 * <p>
 * This service provides methods for interacting with the {@link MetadataRepository},
 * including methods for inserting metadata entries into the database.
 * </p>
 * <p>
 * The MetadataService encapsulates the business logic for managing metadata,
 * ensuring that all interactions with the repository are handled correctly.
 * </p>
 *
 * @see Metadata
 * @see MetadataRepository
 */
@Service
public class MetadataService {

    private final MetadataRepository metadataRepository;

    /**
     * Constructs a new MetadataService with the specified MetadataRepository.
     *
     * @param metadataRepository the repository used for managing Metadata entities
     */
    @Autowired
    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    /**
     * Inserts a new metadata entry into the database.
     * <p>
     * This method creates a new Metadata entity with the specified date, status, and file location,
     * and saves it to the database using the MetadataRepository.
     * </p>
     *
     * @param date the date associated with the metadata entry, in LocalDate format
     * @param status the status of the metadata entry
     * @param fileLocation the file location associated with the metadata entry
     */
    public void insertMetadata(LocalDate date, String status, String fileLocation) {
        Metadata metadata = new Metadata(date, status, fileLocation);
        metadataRepository.save(metadata);
    }
}
