package com.example.api.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultipleChoiceAnswerRequest {
    private Long id;
    private String value;
    private boolean isOther;
}
