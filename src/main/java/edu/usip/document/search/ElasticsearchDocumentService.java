package edu.usip.document.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchDocumentService {

    private final ElasticsearchOperations operations;

    public void index(SearchDocument document) {
        operations.save(document);
    }

    public void deleteById(Long id) {
        operations.delete(id.toString(), SearchDocument.class);
    }

    public SearchHits<SearchDocument> search(String queryText, Pageable pageable) {
        var query = new NativeQueryBuilder()
                .withQuery(q -> q.bool(b -> b
                        .must(m -> m.multiMatch(mm -> mm
                                .query(queryText)
                                .fields("title^4", "author^3", "degree^2", "content")
                                .fuzziness("AUTO")
                        ))
                        .filter(f -> f.term(t -> t.field("active").value(true)))
                ))
                .withPageable(pageable)
                .build();

        return operations.search(query, SearchDocument.class);
    }
}