package com.example.api.service.impl;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.helper.ExcelHelper;
import com.example.api.model.ResultVoteExport;
import com.example.api.payload.response.ImageAnswerResponse;
import com.example.api.payload.response.MultipleChoiceAnswerResponse;
import com.example.api.payload.response.VoteResultResponse;
import com.example.api.service.IExcelService;
import com.example.api.service.IPollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements IExcelService {
    private final IPollService pollService;

    @Override
    public ByteArrayInputStream load(String pollUuid, String userUuid) {
        VoteResultResponse result = pollService.getResultVotePoll(pollUuid);
        List<ResultVoteExport> resultVoteExports = new ArrayList<>();
        if (result.getTotalVotes() == 0){
            throw new BadRequestException("Request failed. It requires at least one participant to export a poll.");
        }

        if (!result.getPollUserUuid().equals(userUuid)){
            throw new PermissionException("Sorry! You don't have permission to export result votes this poll!");
        }

        result.getChoices().forEach(vote -> {
            switch (result.getVotingType()) {
                case "multiple_choice" -> {
                    MultipleChoiceAnswerResponse answer = (MultipleChoiceAnswerResponse) vote.getChoice();
                    resultVoteExports.add(ResultVoteExport.builder()
                                    .choice(answer.getValue() == null ? "Other": answer.getValue())
                            .voteCount(vote.getVoteCount())
//                                    .poll(poll)
                            .build());
                }
                case "image" -> {
                    ImageAnswerResponse answer = (ImageAnswerResponse) vote.getChoice();
                    resultVoteExports.add(ResultVoteExport.builder()
                            .choice(answer.getLabel())
                            .voteCount(vote.getVoteCount())
//                            .poll(poll)
                            .build());
                }
            }
        });

        ExcelHelper excelHelper = new ExcelHelper();
        return excelHelper.exportMultipleImageAnswer(resultVoteExports, result.getPollTitle(), pollUuid, result.getTotalVotes());
    }
}
