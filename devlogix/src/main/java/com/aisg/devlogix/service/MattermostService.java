package com.aisg.devlogix.service;

import com.aisg.devlogix.model.MattermostEntity;
import com.aisg.devlogix.repository.MattermostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MattermostService {

    private final MattermostRepository mattermostRepository;

    public MattermostService(MattermostRepository mattermostRepository) {
        this.mattermostRepository = mattermostRepository;
    }

    public void saveParsedData(String channelName, String text) {
        text = text.replaceAll("\\r?\\n", " ");
        text = text.trim();

        MattermostEntity entity = parseText(channelName, text);

        if (entity.getRepositoryName() != null && entity.getCommitId() != null) {
            boolean exists = mattermostRepository.existsByRepositoryNameAndCommitId(entity.getRepositoryName(), entity.getCommitId());

            if (exists) {
                System.out.println("Duplicate entry: RepositoryName = " + entity.getRepositoryName() + ", CommitId = " + entity.getCommitId());
                return;
            }
        }

        entity.setCreatedAt(LocalDateTime.now());

        mattermostRepository.save(entity);
    }

    private MattermostEntity parseText(String channelName, String text) {
        String repoRegex = "\\[\\\\\\[(.*?)\\\\\\]\\]";     
        String commitIdRegex = "\\[`(.*?)`]";               
        String authorRegex = "-\\s+(\\S+)$";                  

        Pattern repoPattern   = Pattern.compile(repoRegex);
        Pattern commitIdPattern = Pattern.compile(commitIdRegex);
        Pattern authorPattern = Pattern.compile(authorRegex);

        Matcher repoMatcher   = repoPattern.matcher(text);
        Matcher commitIdMatcher = commitIdPattern.matcher(text);
        Matcher authorMatcher = authorPattern.matcher(text);

        MattermostEntity entity = MattermostEntity.builder()
                .channelName(channelName)
                .build();

        System.out.println("Raw Text: " + text);

        if (repoMatcher.find()) {
            System.out.println("RepositoryName => " + repoMatcher.group(1));
            entity.setRepositoryName(repoMatcher.group(1));
        } else {
            System.out.println("RepositoryName NOT FOUND");
        }

        String commitId = null;

        if (commitIdMatcher.find()) {
            commitId = commitIdMatcher.group(1);
            System.out.println("CommitId => " + commitId);
            entity.setCommitId(commitId);
        } else {
            System.out.println("CommitId NOT FOUND");
        }

        String author = null;

        if (authorMatcher.find()) {
            author = authorMatcher.group(1);
            System.out.println("Author => " + author);
            entity.setUserName(author);
        } else {
            System.out.println("Author NOT FOUND");
        }

        String commitMessage = null;
        if (commitId != null) {
            int commitIdEnd = commitIdMatcher.end();

            String afterCommitId = text.substring(commitIdEnd).trim();

            if (author != null) {
                int dashIndex = afterCommitId.lastIndexOf("-");
                if (dashIndex != -1) {
                    commitMessage = afterCommitId.substring(0, dashIndex).trim();
                } else {
                    commitMessage = afterCommitId;
                }
            } else {
                commitMessage = afterCommitId;
            }
        }

        commitMessage = commitMessage.substring(commitMessage.indexOf(")") + 1).trim().split(" - ")[0];

        if (commitMessage != null && !commitMessage.isEmpty()) {
            System.out.println("CommitMessage => " + commitMessage);
            entity.setCommitMessage(commitMessage);
        } else {
            System.out.println("CommitMessage NOT FOUND");
        }

        return entity;
    }
}