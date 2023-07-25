package com.example.api.payload.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    private String userUuid;
    private String name;
    private Long parentId = 0L;
    private String message;
}
