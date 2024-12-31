package com.aisg.devlogix.service;

import com.aisg.devlogix.repository.NotionRepository;
import com.aisg.devlogix.model.NotionEntity;
import com.aisg.devlogix.dto.NotionStatsDTO;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.sql.Timestamp;

@Service
public class NotionStatsService {

    @Autowired
    private NotionRepository notionRepository;

    public List<NotionStatsDTO> getAllNotionData() {
        List<NotionEntity> entities = notionRepository.findAll();

        return entities.stream()
                .map(entity -> new NotionStatsDTO(
                        entity.getId(),
                        entity.getName(),
                        entity.getPersonNames(),
                        entity.getReceivedAt(),
                        entity.getLastEditedTime(),
                        entity.getPageId(),
                        entity.getFieldNames()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getTodayNotionStats() {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        ZonedDateTime nowInSeoul = ZonedDateTime.now(seoulZone);

        LocalDateTime startOfDay = nowInSeoul.toLocalDate().atStartOfDay(seoulZone).toLocalDateTime();
        LocalDateTime endOfDay = nowInSeoul.toLocalDate().atTime(LocalTime.MAX).atZone(seoulZone).toLocalDateTime();

        List<Object[]> results = notionRepository.findEntriesByPersonToday(startOfDay, endOfDay);

        Map<String, Integer> stats = new HashMap<>();

        for (Object[] result : results) {
            String personNames = (String) result[0];
            Long count = (Long) result[1];

            String[] individualNames = personNames.split(",");
            for (String name : individualNames) {
                name = name.trim();
                stats.put(name, stats.getOrDefault(name, 0) + count.intValue());
            }
        }

        return stats;
    }
}