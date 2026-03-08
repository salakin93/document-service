package edu.usip.document.service;

import edu.usip.document.api.dto.request.DocumentUploadRequest;
import edu.usip.document.domain.Document;
import edu.usip.document.dto.records.DocumentDownload;
import edu.usip.document.repo.DocumentRepository;
import edu.usip.document.repo.DocumentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService storageService;

    @Transactional
    public Document upload(DocumentUploadRequest request, MultipartFile file) throws IOException {
        if (file.getSize() > 35 * 1024 * 1024) {
            throw new RuntimeException("El archivo supera los 35MB");
        }

        String sourceId = (request.getSourceId() != null && !request.getSourceId().isBlank())
                ? request.getSourceId()
                : UUID.randomUUID().toString();

        documentRepository.findBySourceId(sourceId).ifPresent(d -> {
            throw new RuntimeException("Ya existe un documento con sourceId=" + sourceId);
        });

        String storagePath = storageService.storeFile(file, sourceId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = auth != null ? auth.getName() : "system";

        Document document = Document.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .degree(request.getDegree())
                .defenseDate(request.getDefenseDate())
                .sourceId(sourceId)
                .fileName(file.getOriginalFilename())
                .storagePath(storagePath)
                .size(file.getSize())
                .createdBy(createdBy)
                .active(true)
                .build();

        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public Page<Document> search(String title, String author, String degree, Pageable pageable) {
        Specification<Document> spec = Specification.where(DocumentSpecification.isActive());

        Specification<Document> titleSpec = DocumentSpecification.titleContains(title);
        if (titleSpec != null) spec = spec.and(titleSpec);

        Specification<Document> authorSpec = DocumentSpecification.authorContains(author);
        if (authorSpec != null) spec = spec.and(authorSpec);

        Specification<Document> degreeSpec = DocumentSpecification.degreeContains(degree);
        if (degreeSpec != null) spec = spec.and(degreeSpec);

        return documentRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Document getById(Long id) {
        return documentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
    }

    @Transactional(readOnly = true)
    public DocumentDownload download(Long id) {
        Document doc = getById(id);

        try {
            return new DocumentDownload(storageService.getFile(doc.getStoragePath()), doc.getFileName());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo", e);
        }
    }
}