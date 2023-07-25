package com.example.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vote_choice")
public class VoteChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne
    @JoinColumn(name = "multiple_choice_answer_id")
    private MultipleChoiceAnswer multipleChoiceAnswer;

    @ManyToOne
    @JoinColumn(name = "image_answer_id")
    private ImageAnswer imageAnswer;

    @ManyToOne
    @JoinColumn(name = "meeting_answer_id")
    private MeetingAnswer meetingAnswer;
}
