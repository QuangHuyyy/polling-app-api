package com.example.api.service;

import com.example.api.model.Comment;
import com.example.api.model.Poll;
import com.example.api.payload.request.CommentRequest;
import com.example.api.payload.response.CommentResponse;
import com.example.api.payload.response.PagedResponse;

public interface ICommentService {
    Comment getCommentById(Long id);
    PagedResponse<CommentResponse> getAllCommentsOfPoll(Poll poll, int page, int size, String[] sort);
    void addComment(CommentRequest commentRequest, Poll poll);

    Comment convertToEntity(CommentRequest commentRequest, Poll poll);

    CommentResponse convertToResponse(Comment comment);

    void editComment(Poll poll, String ownerId, Long commentId, String message);
    void deleteComment(Poll poll, String ownerId, Long commentId);

    void deleteAllComment(Poll poll, String ownerUuid);
}
