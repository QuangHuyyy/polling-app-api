package com.example.api.payload.response;

import com.example.api.model.EPollStatus;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PollResponse {
    private String uuid;
    private String title;
    private String description;
    private String thumbnail;
    private String votingTypeValue;
    private List<?> choices;
    private SettingResponse setting;
    private String ownerName;
    private String createdBy;
    private String createdAt;
    private int participants;

    private EPollStatus status;
}

