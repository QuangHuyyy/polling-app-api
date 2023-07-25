package com.example.api.service;

import com.example.api.payload.response.VotingTokenResponse;

import java.util.List;

public interface IVotingTokenService {
    String generateToken(String userUuid, String pollUuid, List<String> emails, String ownerEmail, String ownerName);

    List<VotingTokenResponse> getAllTokenOfPoll(String pollUuid);

    void deleteTokenById(Long id);
}
