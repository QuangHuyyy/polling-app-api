package com.example.api.payload.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageAnswerResponse {
    private Long id;
    private String imageUrl;
    private String label;
}
