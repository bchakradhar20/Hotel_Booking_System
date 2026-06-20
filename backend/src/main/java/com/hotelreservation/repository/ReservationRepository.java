package com.hotelreservation.repository;

import com.hotelreservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Reservation entity database operations.
 * Includes custom JPQL queries for overlap detection and user-specific lookups.
 *
 * JOIN strategy:
 * - Reservation JOIN User: used for user-specific reservation queries
 * - Reservation JOIN Room: used for overlap detection scoped to a specific room
 * These are ManyToOne relationships, so no JOIN TABLE is needed.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Retrieves all reservations made by a specific user, ordered by check-in date descending.
     * Uses the ManyToOne join between Reservation and User (user_id FK).
     *
     * @param userId the ID of the user whose reservations to fetch
     * @return list of the user's reservations
     */
    List<Reservation> findByUserUserIdOrderByCheckInDateDesc(Long userId);

    /**
     * Checks whether an overlapping reservation exists for a given room and date range.
     *
     * Overlap detection logic:
     * Two date ranges [A_start, A_end] and [B_start, B_end] OVERLAP if:
     *   A_start < B_end AND A_end > B_start
     *
     * Applied to reservation context:
     *   - requested checkInDate  < existing checkOutDate (new booking starts before existing ends)
     *   - requested checkOutDate > existing checkInDate  (new booking ends after existing starts)
     * Both conditions must be true for an overlap to exist.
     *
     * This query JOINs Reservation with Room using the ManyToOne room_id foreign key.
     *
     * @param roomId       the ID of the room to check for conflicts
     * @param checkInDate  the requested check-in date
     * @param checkOutDate the requested check-out date
     * @return true if a conflicting reservation exists; false if the room is available
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.room.roomId = :roomId " +
            "AND r.checkInDate < :checkOutDate " +
            "AND r.checkOutDate > :checkInDate")
    boolean existsOverlappingReservation(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    /**
     * Checks for overlapping reservations while excluding a specific reservation (for updates).
     * Same overlap logic as above but ignores the given reservationId.
     *
     * @param roomId          the ID of the room to check
     * @param checkInDate     the requested check-in date
     * @param checkOutDate    the requested check-out date
     * @param reservationId   the reservation to exclude from the check
     * @return true if a conflict exists (excluding the given reservation); false otherwise
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.room.roomId = :roomId " +
            "AND r.checkInDate < :checkOutDate " +
            "AND r.checkOutDate > :checkInDate " +
            "AND r.reservationId != :reservationId")
    boolean existsOverlappingReservationExcluding(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("reservationId") Long reservationId
    );
}
