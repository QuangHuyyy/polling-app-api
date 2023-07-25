package com.example.api.payload.response;

import lombok.*;

@Data
@Builder
public class VotingTokenResponse {
    private Long id;
    private String email;
    private String token;
    private boolean used;
}
