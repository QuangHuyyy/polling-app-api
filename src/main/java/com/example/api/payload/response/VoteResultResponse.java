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
public class VoteResultResponse {
    private String pollUserUuid;
    private String pollTitle;
    private String votingType;
    private String createdBy;
    private String createdAt;
    private String resultsVisibility;
    private String deadline;
    private boolean requiredParticipant;
    private List<ChoiceVoteCount> choices;
    private List<ParticipantVoted> participantVotes;
    private Long totalVotes;
}
