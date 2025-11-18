package com.chatapp.repository;

import com.chatapp.model.OneOnOneMeeting;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OneOnOneMeetingRepository extends JpaRepository<OneOnOneMeeting, Long> {

    @EntityGraph(attributePaths = {"employee", "manager", "tenant"})
    Page<OneOnOneMeeting> findByEmployeeAndTenantOrderByScheduledAtDesc(
            User employee, Tenant tenant, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "manager", "tenant"})
    Page<OneOnOneMeeting> findByManagerAndTenantOrderByScheduledAtDesc(
            User manager, Tenant tenant, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "manager", "tenant"})
    @Query("SELECT m FROM OneOnOneMeeting m WHERE m.tenant = :tenant " +
           "AND (m.employee = :user OR m.manager = :user) " +
           "AND m.scheduledAt >= :startDate " +
           "ORDER BY m.scheduledAt DESC")
    List<OneOnOneMeeting> findUpcomingMeetings(
            @Param("tenant") Tenant tenant,
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate);

    @EntityGraph(attributePaths = {"employee", "manager", "tenant"})
    @Query("SELECT m FROM OneOnOneMeeting m WHERE m.tenant = :tenant " +
           "AND m.employee = :employee AND m.manager = :manager " +
           "AND m.scheduledAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.scheduledAt DESC")
    List<OneOnOneMeeting> findMeetingsBetween(
            @Param("tenant") Tenant tenant,
            @Param("employee") User employee,
            @Param("manager") User manager,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @EntityGraph(attributePaths = {"employee", "manager", "tenant"})
    List<OneOnOneMeeting> findByTenantAndStatusOrderByScheduledAtDesc(
            Tenant tenant, OneOnOneMeeting.MeetingStatus status);
}
