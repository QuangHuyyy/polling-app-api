package com.example.api.payload.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingRequest {
    private boolean allowMultipleOptions;
    private boolean requireParticipantName;
    private String votingRestrictionValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadlineTime;
    private boolean allowComment;
    private String resultsVisibilityValue;
//    private String editVotePermissionValue;
    private boolean allowEditVote;
}
