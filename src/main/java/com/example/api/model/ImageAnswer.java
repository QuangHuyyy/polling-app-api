package com.example.api.model;

import com.example.api.model.audit.DateAudit;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "image_answer")
public class ImageAnswer extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;

    @OneToOne()
    @JoinColumn(name = "image", referencedColumnName = "filename")
    private FileData image;

    @ManyToOne
    @JoinColumn(name = "poll_uuid", referencedColumnName = "uuid")
    private Poll poll;

    @OneToMany(mappedBy = "imageAnswer", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<VoteChoice> voteChoices;
}
