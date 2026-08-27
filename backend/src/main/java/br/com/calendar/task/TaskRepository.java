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

    /**
     * Candidates for a month view: non-recurring tasks overlapping the month
     * at all — starting in it, ending in it, or fully spanning it (starts
     * before, ends after) — plus every recurring task that could still have
     * an occurrence in the month (its own startsAt is before the month ends
     * — recurrence has no stored end, so it's not filterable any more
     * precisely at the query level). Occurrence expansion for the recurring
     * ones happens in the service.
     */
    @Query("SELECT t FROM Task t" +
            " WHERE t.user.id = :userId AND t.deletedAt IS NULL" +
            " AND (" +
            "   (t.repeatInterval IS NULL AND (" +
            "     t.startsAt BETWEEN :monthStart AND :monthEnd" +
            "     OR (t.endsAt IS NOT NULL AND t.startsAt <= :monthEnd AND t.endsAt >= :monthStart)" +
            "   ))" +
            "   OR (t.repeatInterval IS NOT NULL AND t.startsAt <= :monthEnd)" +
            " )" +
            " ORDER BY t.startsAt")
    List<Task> findActiveTasksForMonth(@Param("userId") String userId, @Param("monthStart") Instant monthStart, @Param("monthEnd") Instant monthEnd);
}
