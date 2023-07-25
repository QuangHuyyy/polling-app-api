package com.example.api.payload.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingResponse {
    private boolean allowMultipleOptions;
    private boolean requireParticipantName;
    private String votingRestrictionValue;
    private boolean hasIpAddressVote;

    private String deadlineTime;
    private boolean allowComment;
    private String resultsVisibilityValue;
//    private String editVotePermissionValue;
    private boolean allowEditVote;
}
