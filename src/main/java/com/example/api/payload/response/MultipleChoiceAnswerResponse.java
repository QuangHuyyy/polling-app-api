package com.example.api.payload.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultipleChoiceAnswerResponse {
    private Long id;
    private String value;
    private boolean isOther;
}
