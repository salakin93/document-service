package edu.usip.document.service;

import edu.usip.document.api.dto.request.DocumentUploadRequest;
import edu.usip.document.api.error.DocumentNotFoundException;
import edu.usip.document.api.error.DuplicateSourceIdException;
import edu.usip.document.api.error.FileAccessException;
import edu.usip.document.api.error.FileSizeExceededException;
import edu.usip.document.domain.Document;
import edu.usip.document.dto.records.DocumentDownload;
import edu.usip.document.repo.DocumentRepository;
import edu.usip.document.repo.DocumentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final long MAX_FILE_SIZE_BYTES = 35L * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final FileStorageService storageService;
    private final DocumentIndexingJob indexingJob;

    @Transactional
    public Document upload(DocumentUploadRequest request, MultipartFile file) throws IOException {
        validateFileSize(file);

        String sourceId = resolveSourceId(request.getSourceId());
        validateUniqueSourceId(sourceId);

        String storagePath = storageService.storeFile(file, sourceId);

        Document document = Document.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .degree(request.getDegree())
                .defenseDate(request.getDefenseDate())
                .sourceId(sourceId)
                .fileName(file.getOriginalFilename())
                .storagePath(storagePath)
                .size(file.getSize())
                .createdBy(resolveCurrentUsername())
                .active(true)
                .build();

        Document saved = documentRepository.save(document);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                indexingJob.indexAsync(saved, storagePath);
            }
        });

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Document> search(String title, String author, String degree, Pageable pageable) {
        Specification<Document> specification = Specification.where(DocumentSpecification.isActive());

        Specification<Document> titleSpec = DocumentSpecification.titleContains(title);
        if (titleSpec != null) {
            specification = specification.and(titleSpec);
        }

        Specification<Document> authorSpec = DocumentSpecification.authorContains(author);
        if (authorSpec != null) {
            specification = specification.and(authorSpec);
        }

        Specification<Document> degreeSpec = DocumentSpecification.degreeContains(degree);
        if (degreeSpec != null) {
            specification = specification.and(degreeSpec);
        }

        return documentRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public Document getById(Long id) {
        return documentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public DocumentDownload download(Long id) {
        Document document = getById(id);

        try {
            return new DocumentDownload(
                    storageService.getFile(document.getStoragePath()),
                    document.getFileName()
            );
        } catch (IOException e) {
            throw new FileAccessException(document.getStoragePath(), e);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileSizeExceededException(MAX_FILE_SIZE_BYTES);
        }
    }

    private String resolveSourceId(String sourceId) {
        return (sourceId != null && !sourceId.isBlank())
                ? sourceId
                : UUID.randomUUID().toString();
    }

    private void validateUniqueSourceId(String sourceId) {
        if (documentRepository.findBySourceId(sourceId).isPresent()) {
            throw new DuplicateSourceIdException(sourceId);
        }
    }

    private String resolveCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}