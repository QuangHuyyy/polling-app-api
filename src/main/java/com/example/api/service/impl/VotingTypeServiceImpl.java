package com.example.api.service.impl;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.VotingType;
import com.example.api.payload.response.VotingTypeResponse;
import com.example.api.repository.IVotingTypeRepository;
import com.example.api.service.IVotingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VotingTypeServiceImpl implements IVotingTypeService {
    private final IVotingTypeRepository votingTypeRepository;

    @Override
    public List<VotingTypeResponse> getAllVotingTypes() {
        List<VotingType> votingTypes = votingTypeRepository.findAll();
        List<VotingTypeResponse> votingTypeResponses = new ArrayList<>();
        votingTypes.forEach(votingType -> {
            VotingTypeResponse response = new VotingTypeResponse(votingType.getIcon(), votingType.getLabel(), votingType.getValue());
            votingTypeResponses.add(response);
        });
        return votingTypeResponses;
    }

    @Override
    public VotingType getVotingTypeByValue(String value) {
        return votingTypeRepository.findByValue(value).orElseThrow(() -> new ResourceNotFoundException("Voting type", "value", value));
    }
}
