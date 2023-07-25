package com.example.api.service.impl;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Comment;
import com.example.api.model.Poll;
import com.example.api.model.User;
import com.example.api.payload.request.CommentRequest;
import com.example.api.payload.response.CommentResponse;
import com.example.api.payload.response.PagedResponse;
import com.example.api.repository.ICommentRepository;
import com.example.api.repository.IUserRepository;
import com.example.api.service.ICommentService;
import com.example.api.service.IFileDataService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SemanticException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {
    private final ICommentRepository commentRepository;
    private final IUserRepository userRepository;
    private final IFileDataService fileDataService;

    @Override
    public Comment getCommentById(Long id) {
        Comment comment = commentRepository.getCommentById(id);
        if (comment == null){
            throw new ResourceNotFoundException("Comment", "id", id.toString());
        }
        return comment;
    }

    @Override
    public PagedResponse<CommentResponse> getAllCommentsOfPoll(Poll poll, int page, int size, String[] sort) {
        List<Order> orders = new ArrayList<>();

        if (sort[0].contains(",")) {
//            sort = ["field1,direction", "field2,direction"]
//            sort more than 2 fields
//            sortOrder = "field, direction"
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                    orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));

            }
        } else {
//            sort = [field, direction]
            orders.add(new Order(getSortDirection(sort[1]), sort[0]));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        try {
            Page<Comment> pageComments = commentRepository.findAllRootCommentByPollUuid(poll, pageable);
            List<Comment> comments = pageComments.getContent();

            List<CommentResponse> commentsResponse;
            if (pageComments.getNumberOfElements() == 0) {
                commentsResponse = Collections.emptyList();
            } else {
                commentsResponse = convertToListResponse(comments, 0);
            }

            return new PagedResponse<>(commentsResponse, pageComments.getNumber(), pageComments.getSize(), pageComments.getTotalElements(), pageComments.getTotalPages(), pageComments.getNumber(), pageComments.isFirst(), pageComments.isLast());
        } catch (SemanticException e) {
            throw new BadRequestException("Sort field invalid!");
        }
    }

    @Override
    public void addComment(CommentRequest commentRequest, Poll poll) {
        if (!poll.getSetting().isAllowComment()) {
            throw new BadRequestException("This poll isn't allow comment!");
        }
        Comment comment = convertToEntity(commentRequest, poll);
        comment.setDeleted(false);

        try {
            commentRepository.save(comment);
        } catch (ConstraintViolationException e) {
            throw new BadRequestException("Please complete all information");
        }
    }

    @Override
    public Comment convertToEntity(CommentRequest commentRequest, Poll poll) {
        Comment comment = Comment.builder()
                .message(commentRequest.getMessage())
                .poll(poll)
                .build();

        if (commentRequest.getUserUuid() != null) {
            User user = userRepository.findById(commentRequest.getUserUuid()).orElseThrow((() -> new ResourceNotFoundException("User", "uuid", commentRequest.getUserUuid())));
            comment.setUser(user);
            comment.setParticipantName(user.getName());
        } else {
            comment.setParticipantName(commentRequest.getName());
        }

        if (commentRequest.getParentId() != null && commentRequest.getParentId() != 0) {
            Comment parent = commentRepository.getCommentById(commentRequest.getParentId());
            if (parent == null){
                throw new ResourceNotFoundException("Comment", "parentId", commentRequest.getParentId().toString());
            }
            if (getLevelComment(parent, 1) >= 3) {
                Long preParentId = commentRepository.getParentIdCommentById(parent.getId());
                Comment preParent = commentRepository.getCommentById(preParentId);
                comment.setParent(preParent);
//                preParent.addCommentReplies(comment);
            } else {
                comment.setParent(parent);
//                parent.addCommentReplies(comment);
            }
        }

        return comment;
    }

    @Override
    public CommentResponse convertToResponse(Comment comment) {
        int level = getLevelComment(comment, 1);
        String avatarUrl = comment.getUser() == null
                ? null : (comment.getUser().getAvatar() == null ? null : fileDataService.getUrlFile(comment.getUser().getAvatar().getFilename()));
        return CommentResponse.builder()
                .id(comment.getId())
                .name(comment.getUser() == null ? comment.getParticipantName() : comment.getUser().getName())
                .avatarUrl(avatarUrl)
                .userUuid(comment.getUser() == null ? null : comment.getUser().getUuid())
                .message(comment.getMessage())
                .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                .oldParentId(comment.getOldParentId())
                .createdAt(comment.getCreatedAt().toString())
                .levelComment(level)
//                .deletedStatus(comment.getDeleted().toString())
                .build();
    }

    @Override
    public void editComment(Poll poll, String ownerUuid, Long commentId, String message) {
        Comment comment = getCommentById(commentId);

        if (comment.getUser() != null && comment.getUser().getUuid().equals(ownerUuid)) {
            if (commentRepository.existsByIdAndPoll(commentId, poll)) {
                commentRepository.editComment(commentId, message);
            } else {
                throw new ResourceNotFoundException("Comment", "commentIdOrPoll", commentId + " | " + poll.getUuid());
            }
        } else {
            throw new PermissionException("Sorry! You don't have permission to edit!");
        }
    }

    @Override
    public void deleteComment(Poll poll, String ownerUuid, Long commentId) {
        Comment comment = getCommentById(commentId);

        if (comment.getUser() != null && comment.getUser().getUuid().equals(ownerUuid)) {
            if (commentRepository.existsByIdAndPoll(commentId, poll)) {
                int levelComment = getLevelComment(comment, 1);
                switch (levelComment) {
                    case 1 -> deleteAllChildOfComment(comment, poll);
                    case 2 -> {
                        commentRepository.updateDeletedComment(poll, comment.getId());
                        List<Comment> relies = commentRepository.getReplyComment(comment.getId());
                        if (!relies.isEmpty()) {
                            relies.forEach(reply ->
                                    commentRepository.updateParenIdAndOldParentId(reply.getId(), comment.getParent(), reply.getParent().getId())
                            );
                        }
                    }
                    case 3 -> commentRepository.updateDeletedComment(poll, comment.getId());
                }
            } else {
                throw new ResourceNotFoundException("Comment", "pollUuid", poll.getUuid());
            }
        } else {
            throw new BadRequestException("You not owner the comment!");
        }
    }

    private void deleteAllChildOfComment(Comment comment, Poll poll) {
//        commentRepository.updateDeletedComment(poll, comment.getId());
        commentRepository.updateDeletedComment(poll, comment.getId());
        List<Comment> relies = commentRepository.getReplyComment(comment.getId());
        if (!relies.isEmpty()) {
            relies.forEach(reply -> {
                if (!reply.isDeleted()) {
                    deleteAllChildOfComment(reply, poll);
                }
            });
        }
    }

    @Override
    public void deleteAllComment(Poll poll, String ownerUuid) {
        if (poll.getCreatedBy().equals(ownerUuid)) {
            commentRepository.updateDeletedAllComments(poll);
        } else {
            throw new ResourceNotFoundException("Comment", "userUuid", ownerUuid);
        }
    }

    private List<CommentResponse> convertToListResponse(List<Comment> comments, int count) {
        List<CommentResponse> repliesRes = new ArrayList<>();
        for (Comment comment : comments) {
            if (!comment.isDeleted()) { // not deleted
                CommentResponse commentResponse = convertToResponse(comment);

                if (comment.getReplies() != null) {
                    count += 1;
                    commentResponse.setReplies(convertToListResponse(comment.getReplies(), count));
                }

                repliesRes.add(commentResponse);
            }
        }
        return repliesRes;
    }

    private int getLevelComment(Comment comment, int level) {
        Long parentId = commentRepository.getParentIdCommentById(comment.getId());
        if (parentId != null){
            Comment parent = commentRepository.getCommentById(parentId);
            if (parent != null){
                level += 1;
                return getLevelComment(parent, level);
            }
        }

        return level;
    }

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }
}
