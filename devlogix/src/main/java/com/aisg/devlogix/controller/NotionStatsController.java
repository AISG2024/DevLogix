package com.aisg.devlogix.controller;

import com.aisg.devlogix.service.NotionStatsService;
import com.aisg.devlogix.dto.NotionStatsDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notion")
public class NotionStatsController {

    @Autowired
    private NotionStatsService notionStatsService;

    @GetMapping("/getAll")
    public List<NotionStatsDTO> getAllNotionData() {
        return notionStatsService.getAllNotionData();
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Integer>> getTodayNotionStats() {
        Map<String, Integer> stats = notionStatsService.getTodayNotionStats();
        return ResponseEntity.ok(stats);
    }
}