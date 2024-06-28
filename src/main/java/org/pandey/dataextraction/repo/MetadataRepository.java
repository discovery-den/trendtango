package org.pandey.dataextraction.repo;

import org.pandey.dataextraction.dao.Metadata;
import org.pandey.dataextraction.service.MetadataService;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link Metadata} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide CRUD operations
 * for {@link Metadata} entities. By extending JpaRepository, it inherits several
 * methods for working with Metadata persistence, including methods for saving,
 * deleting, and finding Metadata entities.
 * </p>
 * <p>
 * The MetadataRepository is used by the {@link MetadataService} to interact
 * with the underlying database.
 * </p>
 *
 * @see Metadata
 * @see JpaRepository
 */
public interface MetadataRepository extends JpaRepository<Metadata, Long> {
}

