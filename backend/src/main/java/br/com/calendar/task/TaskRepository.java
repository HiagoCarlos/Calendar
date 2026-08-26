package br.com.calendar.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, String> {


    Optional<Task> findByIdAndDeletedAtIsNull(String id);

    List<Task> findAllByUser_Id(String userId);


    @Query("SELECT t FROM Task t" +
            " WHERE t.user.id = :userId AND t.deletedAt IS NULL AND t.startsAt BETWEEN :start AND :end" +
            " ORDER BY t.startsAt")
    List<Task> findActiveTasksForDay(@Param("userId") String userId, @Param("start") Instant start, @Param("end") Instant end);
}
