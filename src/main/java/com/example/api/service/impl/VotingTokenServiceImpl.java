package com.example.api.service.impl;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.EmailDetail;
import com.example.api.model.Poll;
import com.example.api.model.VotingToken;
import com.example.api.payload.response.VotingTokenResponse;
import com.example.api.repository.IVotingTokenRepository;
import com.example.api.service.IEmailService;
import com.example.api.service.IPollService;
import com.example.api.service.IVotingTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VotingTokenServiceImpl implements IVotingTokenService {
    private final IVotingTokenRepository votingTokenRepository;
    private final IPollService pollService;
    private final IEmailService emailService;

    @Override
    public String generateToken(String userUuid, String pollUuid, List<String> emails, String ownerEmail, String ownerName) {
        Poll poll = pollService.getByUuid(pollUuid);
        if (!poll.getCreatedBy().equals(userUuid)){
            throw new BadRequestException("Sorry! You are not the owner of this poll!");
        }

        for (String email: emails){
            VotingToken votingToken = VotingToken.builder()
                .email(email)
                .poll(poll)
                .build();
            if (!votingTokenRepository.existsByEmailAndPoll(email, poll)){
                votingToken.setToken(UUID.randomUUID().toString());
            }

            List<VotingToken> votingTokens = poll.getVotingTokens();
            votingTokens.add(votingToken);
            poll.setVotingTokens(votingTokens);

            VotingToken votingTokenDB = votingTokenRepository.save(votingToken);
            String tokenDb = votingTokenDB.getToken();
            if (tokenDb != null){
                EmailDetail info = new EmailDetail(email, ownerName, ownerEmail);
                emailService.sendInviteVotePollMail(info, tokenDb, pollUuid);
            }
        }

        return "Successfully created email invitations.";
    }

    @Override
    public List<VotingTokenResponse> getAllTokenOfPoll(String pollUuid) {
        List<VotingTokenResponse> votingTokenResponses = new ArrayList<>();
        Poll poll = pollService.getByUuid(pollUuid);
        votingTokenRepository.findAllByPoll(poll).forEach(votingToken -> votingTokenResponses.add(VotingTokenResponse.builder()
                        .id(votingToken.getId())
                        .email(votingToken.getEmail())
                        .token(votingToken.getToken())
                        .used(votingToken.isUsed())
                .build()));
        return votingTokenResponses;
    }

    @Override
    @Transactional
    public void deleteTokenById(Long id) {
        votingTokenRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voting token", "id", id.toString()));
        votingTokenRepository.deleteToken(id);
    }
}
