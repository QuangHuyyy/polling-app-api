package com.example.api.payload.request;

import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoteRequest {
    private Long voteId;
    private List<Long> choiceIds;
    private String participant;
    private String userUuid;
    private String ipAddress;
    private String token;
}
