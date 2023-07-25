package com.example.api.repository;

import com.example.api.model.Comment;
import com.example.api.model.Poll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ICommentRepository extends JpaRepository<Comment, Long> {
    boolean existsByIdAndPoll(Long id, Poll poll);

    @Query(value = "select * from comment where comment.id = ?1 and comment.is_deleted = false", nativeQuery = true)
    Comment getCommentById(Long id);

    @Query(value = "select comment.parent_id from comment where comment.id = ?1 and comment.is_deleted = false", nativeQuery = true)
    Long getParentIdCommentById(Long id);

    @Query(value = "select * from comment where comment.parent_id = ?1 and comment.is_deleted = false", nativeQuery = true)
    List<Comment> getReplyComment(Long id);

    @Query(value = "select cmt from Comment cmt where cmt.poll = :poll and cmt.parent.id = null and cmt.isDeleted = false ")
    Page<Comment> findAllRootCommentByPollUuid(Poll poll, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "update Comment cmt set cmt.message = ?2 where cmt.id = ?1 and cmt.isDeleted = false")
    void editComment(Long commentId, String message);
    
    @Transactional
    @Modifying
    @Query(value = "update Comment cmt set cmt.parent = ?2, cmt.oldParentId = ?3 where cmt.id = ?1")
    void updateParenIdAndOldParentId(Long commentId, Comment newParent, Long oldParentId);

    @Transactional
    @Modifying
    @Query(value = "update Comment cmt set cmt.isDeleted = true where cmt.poll = ?1 and cmt.id = ?2")
    void updateDeletedComment(Poll poll, Long commentId);

    @Transactional
    @Modifying
    @Query(value = "update Comment cmt set cmt.isDeleted = true " +
            "where cmt.poll = ?1")
    void updateDeletedAllComments(Poll poll);

}
