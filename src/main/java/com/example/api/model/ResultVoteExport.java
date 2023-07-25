package com.example.api.model;

import com.example.api.model.audit.Auditable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResultVoteExport extends Auditable {
    private String choice;
    private Long voteCount;
}
