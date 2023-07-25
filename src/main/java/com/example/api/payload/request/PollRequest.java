package com.example.api.payload.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PollRequest {
    String title;
    String description;
    MultipartFile thumbnail;
    String votingTypeValue;
    List<Object> choices = new ArrayList<>();
    SettingRequest setting;
    String status;

    public PollRequest(String title, String description, MultipartFile thumbnail, String votingTypeValue, List<MultipleChoiceAnswerRequest> multipleChoiceAnswers, List<MultipartFile> imageAnswers, List<String> labels, List<MeetingAnswerRequest> meetingAnswers, SettingRequest setting, String status) {
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.votingTypeValue = votingTypeValue;

        switch (votingTypeValue){
            case "multiple_choice" -> this.choices.addAll(multipleChoiceAnswers);
            case "image" -> {
                if (imageAnswers != null){
                    if (imageAnswers.size() != labels.size()){
                        labels = labels.subList(imageAnswers.size(), labels.size());
                    }
                    for (int i = 0; i < imageAnswers.size(); i++) {
                        this.choices.add(new ImageAnswerRequest(imageAnswers.get(i), labels.get(i)));
                    }
                }
            }
            case "meeting" -> this.choices.addAll(meetingAnswers);
        }

        this.setting = setting;
        this.status = status;
    }
}
