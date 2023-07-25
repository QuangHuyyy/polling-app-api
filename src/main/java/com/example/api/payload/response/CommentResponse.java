package com.example.api.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String name;
    private String avatarUrl;
    private String userUuid;
    private String message;
    private Long parentId;
    private Long oldParentId;
    private String createdAt;
    private int levelComment;
//    private String deletedStatus;
    private List<CommentResponse> replies;
}
