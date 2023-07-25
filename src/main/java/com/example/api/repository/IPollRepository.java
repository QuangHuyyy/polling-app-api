package com.example.api.repository;

import com.example.api.model.EPollStatus;
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
public interface IPollRepository extends JpaRepository<Poll, String> {
    @Transactional
    @Modifying
    @Query("update Poll p set p.status = ?2 where p.uuid = ?1")
    void updateStatusByUuid(String uuid, EPollStatus status);

    @Query(value = "SELECT * from poll where created_by = ?1", nativeQuery = true)
    Page<Poll> findAllByUserUuid(String userUuid, Pageable pageable);

/*    @Query(value = "select p.*, v.user_uuid from poll p " +
            "left join multiple_choice_answer mca on p.uuid = mca.poll_uuid " +
            "left join vote_choice vc on mca.id = vc.image_answer_id " +
            "left join vote v on vc.vote_id = v.id " +
            "where v.user_uuid = ?1", nativeQuery = true)
    Page<Poll> finAllByParticipatedMultipleChoice(String userUuid, Pageable pageable);

    @Query(value = "select p.*, v.user_uuid from poll p " +
            "left join image_answer ia on p.uuid = ia.poll_uuid " +
            "left join vote_choice vc on ia.id = vc.image_answer_id " +
            "left join vote v on vc.vote_id = v.id " +
            "where v.user_uuid = ?1", nativeQuery = true)
    Page<Poll> finAllByParticipatedImageChoice(String userUuid, Pageable pageable);

    @Query(value = "select p.*, v.user_uuid from poll p " +
            "left join meeting_answer ma on p.uuid = ma.poll_uuid " +
            "left join vote_choice vc on ma.id = vc.image_answer_id " +
            "left join vote v on vc.vote_id = v.id " +
            "where v.user_uuid = ?1", nativeQuery = true)
    Page<Poll> finAllByParticipatedMeetingChoice(String userUuid, Pageable pageable);*/

    @Query(value = "select * from poll " +
            "where uuid in  " +
            "(select distinct concat(coalesce(mca.poll_uuid, ''), coalesce(ia.poll_uuid, ''), " +
            "coalesce(ma.poll_uuid, '')) as poll_uuid " +
            "from vote v " +
            "left join vote_choice vc on v.id = vc.vote_id " +
            "left join multiple_choice_answer mca on vc.multiple_choice_answer_id = mca.id " +
            "left join image_answer ia on vc.image_answer_id = ia.id " +
            "left join meeting_answer ma on vc.meeting_answer_id = ma.id " +
            "where v.user_uuid = ?1 )", nativeQuery = true)
    Page<Poll> findAllByParticipated(String userUuid, Pageable pageable);

//    @Query(value = "select poll.uuid, poll.title from Poll poll where poll.title like %?1% or poll.description like %?1%")
    @Query(value = "select distinct p.uuid, p.title from vote " +
            "join vote_choice vc on vote.id = vc.vote_id " +
            "join multiple_choice_answer mca on mca.id = vc.multiple_choice_answer_id " +
            "right join poll p on mca.poll_uuid = p.uuid " +
            "where (user_uuid = ?2 or p.created_by = ?2) " +
            "and (p.title like ?1 or p.description like ?1) " +
            "union " +
            "select distinct p.uuid, p.title from vote " +
            "join vote_choice vc on vote.id = vc.vote_id " +
            "join image_answer ia on vc.image_answer_id = ia.id " +
            "right join poll p on ia.poll_uuid = p.uuid " +
            "where (user_uuid = ?2 or p.created_by = ?2) " +
            "and (p.title like ?1 or p.description like ?1) " +
            "union " +
            "select distinct p.uuid, p.title from vote " +
            "join vote_choice vc on vote.id = vc.vote_id " +
            "join meeting_answer ma on vc.meeting_answer_id = ma.id " +
            "right join poll p on ma.poll_uuid = p.uuid " +
            "where (user_uuid = ?2 or p.created_by = ?2) " +
            "and (p.title like ?1 or p.description like ?1)", nativeQuery = true)
    List<Object> searchPoll(String query, String userUuid);
}
