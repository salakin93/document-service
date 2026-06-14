package edu.usip.document.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResponse {

    private Long id;
    private String degree;
    private String title;
    private String author;
    private LocalDate defenseDate;
    private String sourceId;
    private String fileName;
    private String downloadUrl;
    private long size;
    private String snippet;
    private float score;
}
