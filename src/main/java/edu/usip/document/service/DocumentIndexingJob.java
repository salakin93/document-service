package edu.usip.document.service;

import edu.usip.document.domain.Document;
import edu.usip.document.search.ElasticsearchDocumentService;
import edu.usip.document.search.PdfTextExtractor;
import edu.usip.document.search.SearchDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIndexingJob {

    private final PdfTextExtractor textExtractor;
    private final ElasticsearchDocumentService esService;

    @Async("indexingExecutor")
    public void indexAsync(Document document, String storagePath) {
        Path path = Path.of(storagePath);

        try (InputStream inputStream = Files.newInputStream(path)) {
            String content = textExtractor.extract(inputStream);

            SearchDocument searchDocument = SearchDocument.builder()
                    .id(document.getId())
                    .title(document.getTitle())
                    .author(document.getAuthor())
                    .degree(document.getDegree())
                    .defenseDate(document.getDefenseDate())
                    .createdAt(document.getCreatedAt())
                    .active(document.isActive())
                    .content(content)
                    .build();

            esService.index(searchDocument);
            log.info("Documento {} indexado correctamente en Elasticsearch", document.getId());
        } catch (Exception e) {
            log.error("Error indexando documento {}", document.getId(), e);
        }
    }
}