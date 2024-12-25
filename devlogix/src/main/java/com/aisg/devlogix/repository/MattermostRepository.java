package com.aisg.devlogix.repository;

import com.aisg.devlogix.model.MattermostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MattermostRepository extends JpaRepository<MattermostEntity, Long> {
    @Query("SELECT COUNT(m) > 0 FROM MattermostEntity m WHERE m.repositoryName = :repositoryName AND m.commitId = :commitId")
    boolean existsByRepositoryNameAndCommitId(@Param("repositoryName") String repositoryName, @Param("commitId") String commitId);

    @Query("SELECT m.userName, COUNT(m) " +
           "FROM MattermostEntity m " +
           "WHERE m.createdAt BETWEEN :startOfDay AND :endOfDay " +
           "GROUP BY m.userName")
    List<Object[]> findCommitCountsByUser(@Param("startOfDay") LocalDateTime startOfDay,
                                          @Param("endOfDay") LocalDateTime endOfDay);
}