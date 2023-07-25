package com.example.api.payload.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingAnswerResponse {
    private Long id;
    private String timeFrom;
    private String timeTo;
}
