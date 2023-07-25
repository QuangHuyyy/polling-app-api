package com.example.api.payload.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageAnswerRequest {
    private MultipartFile image;
    private String label;
}
