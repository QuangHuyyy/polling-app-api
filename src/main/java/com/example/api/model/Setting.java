package com.example.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "setting")
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "boolean default false")
    private boolean allowMultipleOptions;

    @Column(columnDefinition = "boolean default false")
    private boolean isRequireParticipantName;

    @OneToOne
    @JoinColumn(name = "voting_restrictions", referencedColumnName = "id")
    private SettingSelectOption votingRestrictions;

    private LocalDateTime deadline;

    @Column(columnDefinition = "boolean default false")
    private boolean allowComment;

    @OneToOne
    @JoinColumn(name = "results_visibility", referencedColumnName = "id")
    private SettingSelectOption resultsVisibility;

//    @OneToOne
//    @JoinColumn(name = "edit_vote_permissions", referencedColumnName = "id")
//    private SettingSelectOption editVotePermissions;

    @Column(columnDefinition = "boolean default false")
    private boolean allowEditVote;

    @OneToOne(mappedBy = "setting")
    private Poll poll;

}
