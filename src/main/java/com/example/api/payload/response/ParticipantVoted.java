package com.example.api.payload.response;

import lombok.*;

import java.util.List;

@Data
public class ParticipantVoted {
    public Long voteId;
    private String participant;
    private String userUuid;
    private List<Long> choiceIds;

    public ParticipantVoted(Long voteId, String participant, String userUuid, List<Long> choiceIds) {
        this.voteId = voteId;
        this.participant = participant;
        this.userUuid = userUuid;
        this.choiceIds = choiceIds;
    }
}
