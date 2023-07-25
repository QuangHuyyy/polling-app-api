package com.example.api.payload.response;
import com.example.api.controller.FileController;
import com.example.api.service.IFileDataService;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChoiceVoteCount {
    private Object choice;
    private Long voteCount;

    public ChoiceVoteCount(Long answerId, String value, boolean isOther, Long voteCount) {
        this.choice = new MultipleChoiceAnswerResponse(answerId, value, isOther);
        this.voteCount = voteCount;
    }

    public ChoiceVoteCount(Long answerId, String imageFilename, String label, Long voteCount) {
        String imageUrl = MvcUriComponentsBuilder
                .fromMethodName(FileController.class, "loadFile", imageFilename)
                .build().toString();
        this.choice = new ImageAnswerResponse(answerId, imageUrl, label);
        this.voteCount = voteCount;
    }

    public ChoiceVoteCount(Long answerId, LocalDateTime timeFrom, LocalDateTime timeTo, Long voteCount) {
        this.choice = new MeetingAnswerResponse(answerId, timeFrom.toString(), timeTo.toString());
        this.voteCount = voteCount;
    }
}
