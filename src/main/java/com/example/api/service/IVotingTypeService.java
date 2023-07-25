package com.example.api.service;

import com.example.api.model.VotingType;
import com.example.api.payload.response.VotingTypeResponse;

import java.util.List;

public interface IVotingTypeService {

    List<VotingTypeResponse> getAllVotingTypes();
    VotingType getVotingTypeByValue(String value);
}
