package com.aisg.devlogix.repository;

import com.aisg.devlogix.model.MattermostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MattermostRepository extends JpaRepository<MattermostEntity, Long> {
    @Query("SELECT COUNT(m) > 0 FROM MattermostEntity m WHERE m.repositoryName = :repositoryName AND m.commitId = :commitId")
    boolean existsByRepositoryNameAndCommitId(@Param("repositoryName") String repositoryName, @Param("commitId") String commitId);
}