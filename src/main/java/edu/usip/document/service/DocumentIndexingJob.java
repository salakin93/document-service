package edu.usip.document.service;

import edu.usip.document.domain.Document;
import edu.usip.document.search.ElasticsearchDocument;
import edu.usip.document.search.ElasticsearchDocumentService;
import edu.usip.document.search.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIndexingJob {

    private final PdfTextExtractor textExtractor;
    private final ElasticsearchDocumentService esService;

    @Async("indexingExecutor")
    public void indexAsync(Document saved, String storagePath) {
        try (InputStream in = Files.newInputStream(Paths.get(storagePath))) {
            String content = textExtractor.extract(in);
            ElasticsearchDocument esDoc = ElasticsearchDocument.builder()
                    .id(saved.getId())
                    .title(saved.getTitle())
                    .author(saved.getAuthor())
                    .degree(saved.getDegree())
                    .defenseDate(saved.getDefenseDate())
                    .createdAt(saved.getCreatedAt())
                    .active(saved.isActive())
                    .content(content)
                    .build();
            esService.index(esDoc);
            log.info("Documento {} indexado (async) en Elasticsearch", saved.getId());
        } catch (Exception e) {
            log.error("Fallo indexación async para documento {}", saved.getId(), e);
        }
    }
}
