package org.pandey.dataextraction.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

/**
 * Entity class representing Metadata.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
public class Metadata {

    /**
     * The unique identifier for the Metadata entity.
     * This field is auto-generated.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The run date of scheduler.
     */
    @NonNull
    private LocalDate date;

    /**
     * The status of the pulling data from api.
     */
    @NonNull
    private String status;

    /**
     * The file location path in the object storage.
     */
    @NonNull
    private String fileLocation;
}

