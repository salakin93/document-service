package edu.usip.document.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResponse extends DocumentResponse {

    private String snippet;
    private float score;
}
