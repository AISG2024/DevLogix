package com.aisg.devlogix.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mattermost_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MattermostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelName;

    private String userName;

    @Column(name = "repository_name")
    private String repositoryName;

    @Column(name = "commit_message", columnDefinition = "TEXT")
    private String commitMessage;

    private String commitId;

    private LocalDateTime createdAt;
}