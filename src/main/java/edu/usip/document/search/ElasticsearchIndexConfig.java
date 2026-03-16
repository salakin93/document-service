package edu.usip.document.search;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexConfig {

    private final ElasticsearchOperations operations;

    @PostConstruct
    public void init() {
        if (!operations.indexOps(ElasticsearchDocument.class).exists()) {
            String settings = """
            {
              "settings": {
                "analysis": {
                  "filter": {
                    "spanish_stemmer": { "type": "stemmer", "language": "spanish" }
                  },
                  "analyzer": {
                    "spanish_asciifolding": {
                      "tokenizer": "standard",
                      "filter": ["lowercase", "asciifolding", "spanish_stemmer"]
                    }
                  }
                }
              },
              "mappings": {
                "properties": {
                  "id":        { "type": "long" },
                  "title":     { "type": "text", "analyzer": "spanish_asciifolding" },
                  "author":    { "type": "text", "analyzer": "spanish_asciifolding" },
                  "degree":    { "type": "text", "analyzer": "spanish_asciifolding" },
                  "content":   { "type": "text", "analyzer": "spanish_asciifolding" },
                  "createdAt": { "type": "date" },
                  "defenseDate": { "type": "date" },
                  "active":    { "type": "boolean" }
                }
              }
            }
            """;

            operations.indexOps(ElasticsearchDocument.class).create(settings);
        }
    }
}