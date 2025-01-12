package com.aisg.devlogix.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotionStatsDTO {
    private Long id;
    private String name;
    private String personNames;
    private String receivedAt;
    private LocalDateTime lastEditedTime;
    private String pageId;
    private String fieldNames;
}