package com.example.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "voting_type")
public class VotingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String icon;
    private String label;
    private String value;

    @OneToMany(mappedBy = "votingType")
    private Set<Poll> polls;
}
