package edu.usip.document.service;

import edu.usip.document.api.dto.response.DocumentSearchResponse;
import edu.usip.document.api.mapper.DocumentResponseMapper;
import edu.usip.document.domain.Document;
import edu.usip.document.repo.DocumentRepository;
import edu.usip.document.repo.DocumentSpecification;
import edu.usip.document.search.ElasticsearchDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final ElasticsearchDocumentService esService;
    private final DocumentRepository documentRepository;
    private final DocumentResponseMapper mapper;

    @Transactional(readOnly = true)
    public Page<DocumentSearchResponse> searchFullText(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            Page<Document> page = documentRepository.findAll(
                    Specification.where(DocumentSpecification.isActive()),
                    pageable
            );
            return page.map(document -> mapper.toSearchResponse(document, null, 0f));
        }

        var hits = esService.search(query, pageable);

        List<Long> ids = hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getId())
                .toList();

        Map<Long, Document> documentsById = documentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Document::getId, Function.identity()));

        List<DocumentSearchResponse> items = hits.getSearchHits().stream()
                .map(hit -> {
                    Document document = documentsById.get(hit.getContent().getId());
                    if (document == null) {
                        return null;
                    }

                    String snippet = null;
                    List<String> highlights = hit.getHighlightFields().get("content");
                    if (highlights != null && !highlights.isEmpty()) {
                        snippet = highlights.get(0);
                    }

                    float score = hit.getScore() == null ? 0f : hit.getScore().floatValue();

                    return mapper.toSearchResponse(document, snippet, score);
                })
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(items, pageable, hits.getTotalHits());
    }
}