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

    public void index(ElasticsearchDocument doc) {
        operations.save(doc);
    }

    public void deleteById(Long id) {
        operations.delete(id.toString(), ElasticsearchDocument.class);
    }

    public SearchHits<ElasticsearchDocument> search(String q, Pageable pageable) {

        var query = new NativeQueryBuilder()
                .withQuery(qb -> qb.multiMatch(m -> m
                        .query(q)
                        .fields("title^4,author^3,degree^2,content")
                        .fuzziness("AUTO")
                        .type(org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.BEST_FIELDS)
                ))
                .withFilter(fb -> fb.term(t -> t.field("active").value(true)))
                .withSort(s -> s.score(sc -> sc))
                .withPageable(pageable)
                .withHighlightQuery(hq -> hq
                        .withFields("content")
                        .withPreTags("<mark>")
                        .withPostTags("</mark>")
                )
                .build();

        return operations.search(query, ElasticsearchDocument.class);
    }
}