package com.aisg.devlogix.controller;

import com.aisg.devlogix.service.CommitStatsService;
import com.aisg.devlogix.dto.CommitStatsDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commits")
public class CommitStatsController {

    @Autowired
    private CommitStatsService commitStatsService;

    @GetMapping("/getAll")
    public List<CommitStatsDTO> getAllCommits() {
        return commitStatsService.getAllCommits();
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Integer>> getTodayCommitStats() {
        Map<String, Integer> stats = commitStatsService.getTodayCommitStats();
        return ResponseEntity.ok(stats);
    }
}