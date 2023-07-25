package com.example.api.payload.response;

public interface IParticipantVoted {
    Long getVoteId();
    String getParticipant();
    String getUserUuid();
    String getChoiceIds();
}
