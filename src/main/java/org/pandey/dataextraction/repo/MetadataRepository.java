package org.pandey.dataextraction.repo;

import org.pandey.dataextraction.dao.JobMetadata;
import org.pandey.dataextraction.service.AppMetadataService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link JobMetadata} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations
 * for {@link JobMetadata} entities. By extending JpaRepository, it inherits several
 * methods for working with Metadata persistence, including methods for saving,
 * deleting, and finding Metadata entities.
 * </p>
 * <p>
 * The MetadataRepository is used by the {@link AppMetadataService} to interact
 * with the underlying database.
 * </p>
 *
 * @see JobMetadata
 * @see JpaRepository
 */
@Repository
public interface MetadataRepository extends JpaRepository<JobMetadata, Long> {
}

