package com.aisg.devlogix.controller;

import com.aisg.devlogix.service.CommitStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/api/commits")
public class CommitStatsController {

    private final CommitStatsService commitStatsService;

    public CommitStatsController(CommitStatsService commitStatsService) {
        this.commitStatsService = commitStatsService;
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Integer>> getTodayCommitStats() {
        Map<String, Integer> stats = commitStatsService.getTodayCommitStats();
        return ResponseEntity.ok(stats);
    }
}