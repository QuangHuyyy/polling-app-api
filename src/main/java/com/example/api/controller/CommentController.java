package com.example.api.controller;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Poll;
import com.example.api.payload.request.CommentRequest;
import com.example.api.payload.response.CommentResponse;
import com.example.api.payload.response.PagedResponse;
import com.example.api.payload.response.ResponseMessage;
import com.example.api.service.ICommentService;
import com.example.api.service.IPollService;
import com.example.api.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = {"https://quanghuy-polling-app.web.app", "http://localhost:8080"}, maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/comments/{pollUuid}")
@RequiredArgsConstructor
public class CommentController {
    private final ICommentService commentService;
    private final IPollService pollService;

    @GetMapping()
    public ResponseEntity<PagedResponse<CommentResponse>> getAllComments(
            @PathVariable("pollUuid") String pollUuid,
            @RequestParam(name = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort){
        try {
            Poll poll = pollService.getByUuid(pollUuid);
            PagedResponse<CommentResponse> comments = commentService.getAllCommentsOfPoll(poll, page, size, sort);

            return ResponseEntity.ok().body(comments);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping()
    public ResponseEntity<ResponseMessage> addComment(@PathVariable("pollUuid") String pollUuid, @RequestBody CommentRequest commentRequest) {
        try {
            Poll poll = pollService.getByUuid(pollUuid);
            commentService.addComment(commentRequest, poll);

            return ResponseEntity.ok().body(new ResponseMessage("Comment successfully."));
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PutMapping()
    public ResponseEntity<ResponseMessage> editComment(@PathVariable("pollUuid") String pollUuid,
                                         @RequestParam(name = "commentId") Long commentId,
                                         @RequestParam(name = "ownerUuid") String ownerUuid,
                                         @RequestParam(name = "message") String message) {
        try {
            Poll poll = pollService.getByUuid(pollUuid);

            commentService.editComment(poll, ownerUuid, commentId, message);
            return ResponseEntity.ok().body(new ResponseMessage("Update comment successfully!"));
        } catch (PermissionException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping()
    public ResponseEntity<ResponseMessage> deleteComment(@PathVariable("pollUuid") String pollUuid,
             @RequestParam(name = "commentId") Long commentId,
             @RequestParam(name = "ownerUuid") String ownerUuid) {
        Poll poll = pollService.getByUuid(pollUuid);

        commentService.deleteComment(poll, ownerUuid, commentId);

        return ResponseEntity.ok().body(new ResponseMessage("Delete comment successfully!"));
    }

    @DeleteMapping("/all")
    public ResponseEntity<ResponseMessage> deleteAllComment(@PathVariable("pollUuid") String pollUuid,
                                                            @RequestParam(name = "ownerUuid") String ownerUuid){
        Poll poll = pollService.getByUuid(pollUuid);
        commentService.deleteAllComment(poll, ownerUuid);
        return ResponseEntity.ok().body(new ResponseMessage("Delete all comment poll: " + pollUuid + " successfully!"));
    }
}
