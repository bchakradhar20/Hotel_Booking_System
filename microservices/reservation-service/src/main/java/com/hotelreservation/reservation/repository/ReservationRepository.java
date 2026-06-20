package com.hotelreservation.reservation.repository;

import com.hotelreservation.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA repository for {@link Reservation} entity database operations.
 *
 * <p>Extends {@link JpaRepository} for standard CRUD operations and provides
 * custom JPQL queries for user-specific retrieval and date-overlap detection.
 *
 * <p>This service stores room and user identity as plain columns ({@code roomId},
 * {@code userId}, etc.) rather than JPA relationships, since room and user data
 * live in separate microservice databases and cannot be joined across services.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Retrieves all reservations for a given user, ordered by check-in date descending
     * (most recent first).
     *
     * @param userId the primary key of the user whose reservations to fetch
     * @return ordered list of the user's reservations; empty if none exist
     */
    List<Reservation> findByUserIdOrderByCheckInDateDesc(Long userId);

    /**
     * Checks whether a conflicting reservation exists for the given room and date range.
     *
     * <p>Overlap detection logic — two date ranges {@code [A_start, A_end]} and
     * {@code [B_start, B_end]} overlap if and only if:
     * <pre>
     *   A_start &lt; B_end  AND  A_end &gt; B_start
     * </pre>
     * This single condition covers all overlap scenarios:
     * <ul>
     *   <li>Partial overlap from either direction</li>
     *   <li>Full containment (one range inside the other)</li>
     *   <li>Exact date match</li>
     * </ul>
     * Adjacent ranges (e.g. check-out on day X, check-in on day X) do NOT overlap,
     * allowing back-to-back bookings for the same room.
     *
     * @param roomId       the ID of the room to check for booking conflicts
     * @param checkInDate  the requested check-in date (inclusive start)
     * @param checkOutDate the requested check-out date (exclusive end)
     * @return {@code true} if a conflicting reservation exists; {@code false} if the room is free
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.roomId = :roomId " +
            "AND r.checkInDate < :checkOutDate " +
            "AND r.checkOutDate > :checkInDate")
    boolean existsOverlappingReservation(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.roomId = :roomId " +
            "AND r.reservationId <> :excludeId " +
            "AND r.checkInDate < :checkOutDate " +
            "AND r.checkOutDate > :checkInDate")
    boolean existsOverlappingReservationExcluding(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludeId") Long excludeId);
}
