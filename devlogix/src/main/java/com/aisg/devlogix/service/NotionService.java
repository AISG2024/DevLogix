package com.aisg.devlogix.service;

import com.aisg.devlogix.model.NotionEntity;
import com.aisg.devlogix.repository.NotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotionService {

    @Autowired
    private NotionRepository notionRepository;

    public void saveRecord(String id, String lastEditedTime, String name, String personName) {
        NotionEntity record = new NotionEntity();
        record.setPageId(id);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastEditedTime, formatter).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        record.setLastEditedTime(zonedDateTime.toLocalDateTime());

        record.setName(name);

        if (personName != null) {
            record.setPersonName(personName);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        record.setReceivedAt(timestamp);

        notionRepository.save(record);
    }

    public boolean isLastEditedToday(String lastEditedTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime lastEditedDateTime = LocalDateTime.parse(lastEditedTime, formatter);
            LocalDate today = LocalDate.now();
            return lastEditedDateTime.toLocalDate().isEqual(today);
        } catch (Exception e) {
            System.err.println("Error parsing last_edited_time: " + e.getMessage());
            return false;
        }
    }

    public boolean recordExists(String id, String lastEditedTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime lastEditedDateTime = LocalDateTime.parse(lastEditedTime, formatter);
        
        return notionRepository.existsByPageIdAndLastEditedTime(id, lastEditedDateTime);
    }
}