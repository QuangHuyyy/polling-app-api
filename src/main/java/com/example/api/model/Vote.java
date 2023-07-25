package com.example.api.model;

import com.example.api.model.audit.DateAudit;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vote")
public class Vote extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid")
    private User user;

    private String participant;

    private String ipAddress;

    @OneToMany(mappedBy = "vote", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<VoteChoice> voteChoices = new ArrayList<>();

    public void addVoteChoice(MultipleChoiceAnswer multipleChoiceAnswer){
        VoteChoice voteChoice = new VoteChoice();
        voteChoice.setVote(this);
        voteChoice.setMultipleChoiceAnswer(multipleChoiceAnswer);
        this.voteChoices.add(voteChoice);
    }

    public void addVoteChoice(ImageAnswer imageAnswer){
        VoteChoice voteChoice = new VoteChoice();
        voteChoice.setVote(this);
        voteChoice.setImageAnswer(imageAnswer);
        this.voteChoices.add(voteChoice);
    }

    public void addVoteChoice(MeetingAnswer meetingAnswer){
        VoteChoice voteChoice = new VoteChoice();
        voteChoice.setVote(this);
        voteChoice.setMeetingAnswer(meetingAnswer);
        this.voteChoices.add(voteChoice);
    }

    public void updateChoiceMultiple(List<MultipleChoiceAnswer> choiceAnswers){
        int quantityDifference = this.voteChoices.size() - choiceAnswers.size();
        if (quantityDifference < 0){
            for (int i = 0; i < this.voteChoices.size(); i++) {
                this.voteChoices.get(i).setMultipleChoiceAnswer(choiceAnswers.get(i));
            }

            for (int i = (quantityDifference * -1); i < choiceAnswers.size(); i++) {
                this.addVoteChoice(choiceAnswers.get(i));
            }
        } else if (quantityDifference == 0){
            for (int i = 0; i < this.voteChoices.size(); i++) {
                this.voteChoices.get(i).setMultipleChoiceAnswer(choiceAnswers.get(i));
            }
        } else {
            for (int i = 0; i < voteChoices.size(); i++) {
                if (i < choiceAnswers.size()) {
                    this.voteChoices.get(i).setMultipleChoiceAnswer(choiceAnswers.get(i));
                } else {
                    removeChoice(voteChoices.get(i).getId());
                }
            }
        }
    }

    public void updateChoiceImage(List<ImageAnswer> choiceAnswers){
        int quantityDifference = this.voteChoices.size() - choiceAnswers.size();
        if (quantityDifference < 0){
            for (int i = 0; i < this.voteChoices.size(); i++) {
                this.voteChoices.get(i).setImageAnswer(choiceAnswers.get(i));
            }

            for (int i = (quantityDifference * -1); i < choiceAnswers.size(); i++) {
                this.addVoteChoice(choiceAnswers.get(i));
            }
        } else if (quantityDifference == 0){
            for (int i = 0; i < this.voteChoices.size(); i++) {
                this.voteChoices.get(i).setImageAnswer(choiceAnswers.get(i));
            }
        } else {
            for (int i = 0; i < voteChoices.size(); i++) {
                if (i < choiceAnswers.size()) {
                    this.voteChoices.get(i).setImageAnswer(choiceAnswers.get(i));
                } else {
                    removeChoice(voteChoices.get(i).getId());
                }
            }

        }
    }

    public void updateChoiceMeeting(List<MeetingAnswer> choiceAnswers){
        int quantityDifference = this.voteChoices.size() - choiceAnswers.size();
        if (quantityDifference < 0){
            for (int i = 0; i < this.voteChoices.size(); i++) {
                this.voteChoices.get(i).setMeetingAnswer(choiceAnswers.get(i));
            }

            for (int i = (quantityDifference * -1); i < choiceAnswers.size(); i++) {
                this.addVoteChoice(choiceAnswers.get(i));
            }
        } else if (quantityDifference == 0){
            for (int i = 0; i < this.voteChoices.size(); i++) {
                this.voteChoices.get(i).setMeetingAnswer(choiceAnswers.get(i));
            }
        } else {
            for (int i = 0; i < voteChoices.size(); i++) {
                if (i < choiceAnswers.size()) {
                    this.voteChoices.get(i).setMeetingAnswer(choiceAnswers.get(i));
                } else {
                    removeChoice(voteChoices.get(i).getId());
                }
            }

        }
    }

    public void removeChoice(Long voteChoiceId){
        voteChoices.removeIf(voteChoice -> voteChoice.getId().equals(voteChoiceId));
    }
}
