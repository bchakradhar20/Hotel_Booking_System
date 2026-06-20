package com.hotelreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a room reservation made by a user.
 * Each reservation belongs to exactly one User and one Room.
 *
 * Relationships:
 *   - ManyToOne with User: a user can have many reservations
 *   - ManyToOne with Room: a room can be reserved many times (different dates)
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    /** Primary key for the reservation entity */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    /** Date the guest checks into the room (inclusive) */
    @Column(nullable = false)
    private LocalDate checkInDate;

    /** Date the guest checks out of the room (exclusive in overlap logic) */
    @Column(nullable = false)
    private LocalDate checkOutDate;

    /**
     * Total amount charged for this reservation.
     * Calculated in the backend as: numberOfNights * room.pricePerNight.
     * Never accepted from or trusted from the frontend.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * The user who made this reservation.
     * ManyToOne: multiple reservations can belong to one user.
     * LAZY fetch to avoid loading user data unless explicitly needed.
     * Foreign key: user_id in reservations table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The room being reserved.
     * ManyToOne: a room can appear in many reservations.
     * LAZY fetch to avoid unnecessary room loading.
     * Foreign key: room_id in reservations table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
