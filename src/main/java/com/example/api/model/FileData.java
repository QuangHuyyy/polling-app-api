package com.example.api.model;

import com.example.api.model.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "file_data")
public class FileData extends Auditable {
    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(length = 36, nullable = false, updatable = false)
    private String uuid;

    @Column(unique = true)
    private String filename;

    @Column(unique = true)
    private String url;
    private String type;
    private Long size;

    @OneToOne(mappedBy = "avatar", fetch = FetchType.LAZY)
    private User user;

    @OneToOne(mappedBy = "thumbnail", fetch = FetchType.LAZY)
    private Poll poll;

    @OneToOne(mappedBy = "image", fetch = FetchType.LAZY)
    private ImageAnswer imageAnswer;
}
