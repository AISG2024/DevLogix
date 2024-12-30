package com.aisg.devlogix.repository;

import com.aisg.devlogix.model.NotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotionRepository extends JpaRepository<NotionEntity, Long> {
    boolean existsByPageIdAndLastEditedTime(String pageId, String lastEditedTime);
}