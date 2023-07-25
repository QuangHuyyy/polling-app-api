package com.example.api.model;

import com.example.api.model.audit.DateAudit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "voting_token")
public class VotingToken extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String email;

    @Column(columnDefinition = "boolean default 0")
    private boolean isUsed;

    @ManyToOne
    @JoinColumn(name = "poll_uuid", referencedColumnName = "uuid")
    private Poll poll;
}
