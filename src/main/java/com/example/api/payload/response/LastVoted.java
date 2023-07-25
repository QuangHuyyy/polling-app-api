package com.example.api.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastVoted {
    private List<Long> choiceIds = new ArrayList<>();
    private Long voteId;
    private String participant;
}
