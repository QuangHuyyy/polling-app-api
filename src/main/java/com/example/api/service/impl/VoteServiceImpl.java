package com.example.api.service.impl;

import com.example.api.model.Vote;
import com.example.api.repository.IVoteRepository;
import com.example.api.service.IVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements IVoteService {
    private final IVoteRepository voteRepository;

    @Override
    public void save(Vote vote) {
        voteRepository.save(vote);
    }
}
