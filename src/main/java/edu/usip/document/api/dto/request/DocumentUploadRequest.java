package edu.usip.document.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class DocumentUploadRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String degree;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate defenseDate;

    private String sourceId;
}