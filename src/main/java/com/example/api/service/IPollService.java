package com.example.api.service;

import com.example.api.model.EPollStatus;
import com.example.api.model.Poll;
import com.example.api.payload.request.PollRequest;
import com.example.api.payload.request.VoteRequest;
import com.example.api.payload.response.LastVoted;
import com.example.api.payload.response.PagedResponse;
import com.example.api.payload.response.PollResponse;
import com.example.api.payload.response.VoteResultResponse;

import java.util.List;

public interface IPollService {
    Poll save(Poll poll);

    PagedResponse<PollResponse> getAllPollByUser(String userUuid, String filter, int page, int size, String[] sort);

    Poll getByUuid(String pollUuid/*, String userUuid*/);

    void updateStatusByUuid(String uuid, EPollStatus status);

    Poll updatePoll(String uuid, Poll poll, String thumbnailStatus, List<Long> imageAnswerIdsNoChange, List<String> labelsImageAnswerNoChange);
    Poll duplicatePoll(Poll poll, String thumbnailFilename, List<String> imageAnswersFilename, List<String> labels);

    Poll publicPoll(String pollUuid, String userUuid);

    void resetPoll(String uuid, String userUuid);

    void deleteByUuid(String pollUuid, String userUuid);

    List<Object> searchPoll(String query, String userUuid);

    Poll convertToEntity(PollRequest pollRequest);

    PollResponse convertToResponse(Poll poll);

    Long castVote(String pollUuid, VoteRequest voteRequest);
    LastVoted getLastVotedByUser(String pollUuid, String userUuid);
    void editVote(String pollUuid, VoteRequest voteRequest);

    VoteResultResponse getResultVotePoll(String pollUuid);

    Boolean isAllowShowResult(String pollUuid, String userUuid);
}
