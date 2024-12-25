package com.aisg.devlogix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommitStatsDTO {
    private String repositoryName;
    private String commitMessage;
    private String commitId;
    private String userName;
    private String channelName;
    private LocalDateTime createdAt;
}