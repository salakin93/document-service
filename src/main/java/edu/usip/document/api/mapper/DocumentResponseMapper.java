package edu.usip.document.api.mapper;

import edu.usip.document.api.dto.response.DocumentResponse;
import edu.usip.document.api.dto.response.DocumentSearchResponse;
import edu.usip.document.domain.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentResponseMapper {

    public DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .author(document.getAuthor())
                .degree(document.getDegree())
                .defenseDate(document.getDefenseDate())
                .sourceId(document.getSourceId())
                .fileName(document.getFileName())
                .downloadUrl(buildDownloadUrl(document.getId()))
                .size(document.getSize())
                .build();
    }

    public DocumentSearchResponse toSearchResponse(Document document, String snippet, float score) {
        return DocumentSearchResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .author(document.getAuthor())
                .degree(document.getDegree())
                .defenseDate(document.getDefenseDate())
                .sourceId(document.getSourceId())
                .fileName(document.getFileName())
                .downloadUrl(buildDownloadUrl(document.getId()))
                .size(document.getSize())
                .snippet(snippet)
                .score(score)
                .build();
    }
    private String buildDownloadUrl(Long id) {
        return "/v1/documents/" + id + "/download";
    }
}