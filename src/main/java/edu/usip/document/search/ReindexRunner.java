package edu.usip.document.search;

import edu.usip.document.domain.Document;
import edu.usip.document.repo.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.search.reindex-on-startup", havingValue = "true")
public class ReindexRunner implements CommandLineRunner {

    private final DocumentRepository repository;
    private final PdfTextExtractor extractor;
    private final ElasticsearchDocumentService esService;

    @Override
    public void run(String... args) {
        for (Document document : repository.findAll()) {
            if (!document.isActive()) {
                continue;
            }

            try (InputStream inputStream = Files.newInputStream(Path.of(document.getStoragePath()))) {
                String content = extractor.extract(inputStream);

                esService.index(SearchDocument.builder()
                        .id(document.getId())
                        .title(document.getTitle())
                        .author(document.getAuthor())
                        .degree(document.getDegree())
                        .defenseDate(document.getDefenseDate())
                        .createdAt(document.getCreatedAt())
                        .active(document.isActive())
                        .content(content)
                        .build());

            } catch (Exception e) {
                log.error("Error reindexando documento {}", document.getId(), e);
            }
        }
    }
}