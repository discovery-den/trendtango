package org.pandey.dataextraction.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;

/**
 * Entity class representing job metadata.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "job_metadata")
public class JobMetadata {

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
    @Column(nullable = false)
    private LocalDate date;

    /**
     * The status of the pulling data from api.
     */
    @NonNull
    @Column(nullable = false, length = 50)
    private String status;

    /**
     * The file location path in the object storage.
     */
    @NonNull
    @Column(name = "file_location", nullable = false, length = 500)
    private String fileLocation;
}