package edu.usip.document.repo;

import edu.usip.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByIdAndActiveTrue(Long id);

    Optional<Document> findBySourceId(String sourceId);
}