package com.example.api.model;

import com.example.api.model.audit.DateAudit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comment")
public class Comment extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid")
    private User user;

    @NotNull
    private String participantName;

    private String message;

    @Column(columnDefinition = "boolean default false")
    private boolean isDeleted;

    @ManyToOne()
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Comment parent;

    @Column(columnDefinition = "BIGINT DEFAULT -1")
    private Long oldParentId;

//    @Column(columnDefinition = "BIGINT DEFAULT 0")
//    private Long parentId;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private List<Comment> replies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_uuid")
    private Poll poll;

    public void addCommentReplies(Comment comment){
        if (replies == null){
            replies = new ArrayList<>();
        }
        replies.add(comment);
    }
}
