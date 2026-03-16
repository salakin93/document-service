package edu.usip.document.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "documents")
public class ElasticsearchDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "spanish_asciifolding")
    private String title;

    @Field(type = FieldType.Text, analyzer = "spanish_asciifolding")
    private String author;

    @Field(type = FieldType.Text, analyzer = "spanish_asciifolding")
    private String degree;

    @Field(type = FieldType.Text, analyzer = "spanish_asciifolding")
    private String content; // texto extraído del PDF

    @Field(type = FieldType.Date)
    private LocalDate defenseDate;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Boolean)
    private boolean active;
}