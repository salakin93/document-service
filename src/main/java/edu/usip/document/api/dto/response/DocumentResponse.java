package edu.usip.document.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DocumentResponse {

    private Long id;
    private String degree;
    private String title;
    private String author;
    private LocalDate defenseDate;
    private String sourceId;
    private String fileName;
    private String downloadUrl;
    private long size;
}