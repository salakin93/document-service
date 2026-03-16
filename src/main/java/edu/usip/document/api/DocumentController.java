package edu.usip.document.api;

import edu.usip.document.api.dto.request.DocumentUploadRequest;
import edu.usip.document.api.dto.response.DocumentResponse;
import edu.usip.document.api.dto.response.DocumentSearchResponse;
import edu.usip.document.api.mapper.DocumentResponseMapper;
import edu.usip.document.domain.Document;
import edu.usip.document.dto.records.DocumentDownload;
import edu.usip.document.service.DocumentSearchService;
import edu.usip.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentSearchService documentSearchService;
    private final DocumentResponseMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DocumentResponse> upload(
            @Valid @ModelAttribute DocumentUploadRequest request,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Document document = documentService.upload(request, file);
        return ResponseEntity.ok(mapper.toResponse(document));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STUDENT')")
    public ResponseEntity<Page<DocumentSearchResponse>> searchFullText(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(documentSearchService.searchFullText(q, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STUDENT')")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(documentService.getById(id)));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_STUDENT')")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentDownload download = documentService.download(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.fileName() + "\"")
                .body(download.resource());
    }
}