package com.aisg.devlogix.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"pageId", "lastEditedTime"}))
public class NotionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pageId;

    @Column(nullable = false)
    private LocalDateTime lastEditedTime;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String personNames;

    @Column(nullable = false)
    private String receivedAt;

    @Column(nullable = false)
    private String fieldNames;
}