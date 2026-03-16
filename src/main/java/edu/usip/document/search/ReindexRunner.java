package edu.usip.document.search;

import edu.usip.document.domain.Document;
import edu.usip.document.repo.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class ReindexRunner implements CommandLineRunner {

    private final DocumentRepository repo;
    private final PdfTextExtractor extractor;
    private final ElasticsearchDocumentService esService;

    @Override
    public void run(String... args) throws Exception {
        // Ejecuta una sola vez o protégelo con una flag/env var
        for (Document d : repo.findAll()) {
            if (!d.isActive()) continue;
            try (InputStream in = Files.newInputStream(Paths.get(d.getStoragePath()))) {
                String content = extractor.extract(in);
                esService.index(ElasticsearchDocument.builder()
                        .id(d.getId())
                        .title(d.getTitle())
                        .author(d.getAuthor())
                        .degree(d.getDegree())
                        .defenseDate(d.getDefenseDate())
                        .createdAt(d.getCreatedAt())
                        .active(d.isActive())
                        .content(content)
                        .build());
            } catch (Exception e) {
                // log y seguir; puedes llevar un registro de fallidos
            }
        }
    }
}