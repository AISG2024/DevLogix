package com.aisg.devlogix.service;

import com.aisg.devlogix.repository.MattermostRepository;
import com.aisg.devlogix.model.MattermostEntity;
import com.aisg.devlogix.dto.CommitStatsDTO;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommitStatsService {
    @Autowired
    private MattermostRepository mattermostRepository;

    public List<CommitStatsDTO> getAllCommits() {
        List<MattermostEntity> entities = mattermostRepository.findAll();

        return entities.stream()
                .map(entity -> new CommitStatsDTO(
                        entity.getRepositoryName(),
                        entity.getCommitMessage(),
                        entity.getCommitId(),
                        entity.getUserName(),
                        entity.getChannelName(),
                        entity.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getTodayCommitStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> results = mattermostRepository.findCommitCountsByUser(startOfDay, endOfDay);

        Map<String, Integer> commitStats = new HashMap<>();
        for (Object[] result : results) {
            String userName = (String) result[0];
            Long count = (Long) result[1];
            commitStats.put(userName, count.intValue());
        }

        return commitStats;
    }
}