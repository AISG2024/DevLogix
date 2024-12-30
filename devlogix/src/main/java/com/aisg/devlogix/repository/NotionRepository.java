package com.aisg.devlogix.repository;

import com.aisg.devlogix.model.NotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotionRepository extends JpaRepository<NotionEntity, Long> {
    boolean existsByPageIdAndLastEditedTime(String pageId, LocalDateTime lastEditedTime);

    @Query("SELECT n.personName, COUNT(n) FROM NotionEntity n " +
       "WHERE n.lastEditedTime BETWEEN :startOfDay AND :endOfDay " +
       "GROUP BY n.personName")
    List<Object[]> findEntriesByPersonToday(@Param("startOfDay") LocalDateTime startOfDay,
                                        @Param("endOfDay") LocalDateTime endOfDay);
}