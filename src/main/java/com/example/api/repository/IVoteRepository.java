package com.example.api.repository;

import com.example.api.model.Vote;
import com.example.api.payload.response.ChoiceVoteCount;
import com.example.api.payload.response.IParticipantVoted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IVoteRepository extends JpaRepository<Vote, Long> {
    @Query(value = "select count(vote_id) from vote " +
            "join vote_choice vc on vote.id = vc.vote_id " +
            "join multiple_choice_answer mca on mca.id = vc.multiple_choice_answer_id " +
            "where mca.poll_uuid = ?1 and vote.ip_address = ?2", nativeQuery = true)
    int checkIpAddressVoteMultipleAnswer(String pollUuid, String ipAddress);

    @Query(value = "select count(vote_id) from vote " +
            "join vote_choice vc on vote.id = vc.vote_id " +
            "join image_answer ia on ia.id = vc.image_answer_id " +
            "where ia.poll_uuid = ?1 and vote.ip_address = ?2", nativeQuery = true)
    int checkIpAddressVoteImageAnswer(String pollUuid, String ipAddress);

    @Query(value = "select count(vote_id) from vote " +
            "join vote_choice vc on vote.id = vc.vote_id " +
            "join meeting_answer ma on ma.id = vc.meeting_answer_id " +
            "where ma.poll_uuid = ?1 and vote.ip_address = ?2", nativeQuery = true)
    int checkIpAddressVoteMeetingAnswer(String pollUuid, String ipAddress);

    @Query(value = "select count(distinct vote.id) from vote join vote_choice vc on vote.id = vc.vote_id " +
            "left join multiple_choice_answer mca on mca.id = vc.multiple_choice_answer_id " +
            "left join image_answer ia on ia.id = vc.image_answer_id " +
            "left join meeting_answer ma on vc.meeting_answer_id = ma.id " +
            "where mca.poll_uuid = ?1 or ia.poll_uuid = ?1 " +
            "or ma.poll_uuid = ?1", nativeQuery = true)
    int getParticipantQuantityPoll(String pollUuid);
    @Query(value = "select " +
            "new com.example.api.payload.response.ChoiceVoteCount(answer.id, answer.value, answer.isOther, count(vc.id)) " +
            "from MultipleChoiceAnswer answer " +
            "left join answer.voteChoices vc " +
            "where answer.poll.uuid = ?1 " +
            "group by answer.id")
    List<ChoiceVoteCount> countMultipleChoiceByPollId(String pollId);

    @Query(value = "select v.id as voteId, v.participant, v.user_uuid as userUuid, group_concat(mca.id separator ',') as choiceIds " +
        "from vote v " +
        "left join vote_choice vc on v.id = vc.vote_id " +
        "left join multiple_choice_answer mca on vc.multiple_choice_answer_id = mca.id " +
        "where mca.poll_uuid = ?1 " +
        "group by v.id, v.participant, v.user_uuid, v.created_at " +
        "order by v.created_at", nativeQuery = true)
    List<IParticipantVoted> participantVotedMultipleChoiceByPollId(String pollId);

    @Query(value = "select new com.example.api.payload.response.ChoiceVoteCount(answer.id, answer.image.filename, answer.label, count(vc.id)) " +
            "from ImageAnswer answer " +
            "left join answer.voteChoices vc " +
            "where answer.poll.uuid = ?1 " +
            "group by answer.id")
    List<ChoiceVoteCount> countImageChoiceByPollId(String pollId);

    @Query(value = "select v.id as voteId, v.participant, v.user_uuid as userUuid, group_concat(ia.id separator ',') as choiceIds " +
        "from vote v " +
        "left join vote_choice vc on v.id = vc.vote_id " +
        "left join image_answer ia on vc.image_answer_id = ia.id " +
        "where ia.poll_uuid = ?1 " +
        "group by v.id, v.participant, v.user_uuid, v.created_at " +
        "order by v.created_at", nativeQuery = true)
    List<IParticipantVoted> participantVotedImageChoiceByPollId(String pollId);

    @Query(value = "select new com.example.api.payload.response.ChoiceVoteCount(answer.id, answer.timeFrom, answer.timeTo, count(vc.id)) " +
            "from MeetingAnswer answer " +
            "left join answer.voteChoices vc " +
            "where answer.poll.uuid = ?1 " +
            "group by answer.id")
    List<ChoiceVoteCount> countMeetingChoiceByPollId(String pollId);

    @Query(value = "select v.id as voteId, v.participant, v.user_uuid as userUuid, group_concat(ma.id separator ',') as choiceIds " +
            "from vote v " +
            "left join vote_choice vc on v.id = vc.vote_id " +
            "left join meeting_answer ma on vc.meeting_answer_id = ma.id " +
            "where ma.poll_uuid = ?1 " +
            "group by v.id, v.participant, v.user_uuid, v.created_at " +
            "order by v.created_at", nativeQuery = true)
    List<IParticipantVoted> participantVotedMeetingChoiceByPollId(String pollId);

    @Query(value = "select count(v.id) != 0 from vote v " +
            "join vote_choice vc on v.id = vc.vote_id " +
            "join multiple_choice_answer mca on mca.id = vc.multiple_choice_answer_id " +
            "where mca.poll_uuid = ?1 and v.user_uuid = ?2", nativeQuery = true)
    int existUserVotedMultipleChoice(String pollUuid, String userUuid);

    @Query(value = "select count(v.id) != 0 from vote v " +
            "join vote_choice vc on v.id = vc.vote_id " +
            "join image_answer ia on ia.id = vc.image_answer_id " +
            "where ia.poll_uuid = ?1 and v.user_uuid = ?2", nativeQuery = true)
    int existUserVotedImageChoice(String pollUuid, String userUuid);

    @Query(value = "select count(v.id) != 0 from vote v " +
            "join vote_choice vc on v.id = vc.vote_id " +
            "join meeting_answer ma on ma.id = vc.meeting_answer_id " +
            "where ma.poll_uuid = ?1 and v.user_uuid = ?2", nativeQuery = true)
    int existUserVotedMeetingChoice(String pollUuid, String userUuid);

    @Query(value = "select v.id from vote v " +
            "join vote_choice vc on v.id = vc.vote_id " +
            "join multiple_choice_answer mca on vc.multiple_choice_answer_id = mca.id " +
            "where mca.poll_uuid = ?1 and v.user_uuid = ?2 " +
            "order by v.created_at desc limit 1",
            nativeQuery = true)
    Long getVoteIdMultipleLastVoted(String pollUuid, String userUuid);

    @Query(value = "select v.id from vote v " +
            "join vote_choice vc on v.id = vc.vote_id " +
            "join image_answer ia on vc.multiple_choice_answer_id = ia.id " +
            "where ia.poll_uuid = ?1 and v.user_uuid = ?2 " +
            "order by v.created_at desc limit 1",
            nativeQuery = true)
    Long getVoteIdImageLastVoted(String pollUuid, String userUuid);

    @Query(value = "select v.id from vote v " +
            "join vote_choice vc on v.id = vc.vote_id " +
            "join meeting_answer ma on vc.meeting_answer_id = ma.id " +
            "where ma.poll_uuid = ?1 and v.user_uuid = ?2 " +
            "order by v.created_at desc limit 1",
            nativeQuery = true)
    Long getVoteIdMeetingLastVoted(String pollUuid, String userUuid);

    @Query(value = "select mca.id from vote_choice vc " +
            "join multiple_choice_answer mca on vc.multiple_choice_answer_id = mca.id " +
            "where vc.vote_id = ?1",
            nativeQuery = true)
    List<Long> getLastChoiceIdsMultipleAnswerLastVoted(Long voteId);

    @Query(value = "select ia.id from vote_choice vc " +
            "join image_answer ia on vc.image_answer_id = ia.id " +
            "where vc.vote_id = ?1",
            nativeQuery = true)
    List<Long> getLastChoiceIdsImageAnswerLastVoted(Long voteId);

    @Query(value = "select ma.id from vote_choice vc " +
            "join meeting_answer ma on vc.meeting_answer_id = ma.id " +
            "where vc.vote_id = ?1",
            nativeQuery = true)
    List<Long> getLastChoiceIdsMeetingAnswerLastVoted(Long voteId);

    @Query(value = "select v.participant from Vote v join v.voteChoices vc on v.id = vc.vote.id " +
            "join vc.multipleChoiceAnswer mca on vc.multipleChoiceAnswer.id = mca.id " +
            "where mca.id = ?1 and v.id = ?2")
    String getParticipantMultipleLastVoted(Long choiceId, Long voteId);

    @Query(value = "select v.participant from Vote v join v.voteChoices vc on v.id = vc.vote.id " +
            "join vc.imageAnswer ia on vc.imageAnswer.id = ia.id " +
            "where ia.id = ?1 and v.id = ?2")
    String getParticipantImageLastVoted(Long choiceId, Long voteId);

    @Query(value = "select v.participant from Vote v join v.voteChoices vc on v.id = vc.vote.id " +
            "join vc.meetingAnswer ma on vc.meetingAnswer.id = ma.id " +
            "where ma.id = ?1 and v.id = ?2")
    String getParticipantMeetingLastVoted(Long choiceId, Long voteId);
}
