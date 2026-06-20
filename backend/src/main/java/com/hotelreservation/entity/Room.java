package com.hotelreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing a hotel room available for reservation.
 * A room can have many reservations over time (OneToMany with Reservation).
 */
@Entity
@Table(name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "roomNumber")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    /** Primary key for the room entity */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    /** Unique room number used for identification (e.g., "101", "202") */
    @Column(nullable = false, unique = true, length = 20)
    private String roomNumber;

    /**
     * Type of room: STANDARD, DELUXE, or SUITE.
     * Stored as a string enum in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType roomType;

    /** Price charged per night for this room */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    /** Maximum number of guests this room can accommodate */
    @Column(nullable = false)
    private Integer capacity;

    /** Optional description providing additional room details */
    @Column(length = 500)
    private String description;

    /**
     * Enum defining the available room types.
     */
    public enum RoomType {
        STANDARD, DELUXE, SUITE
    }
}
