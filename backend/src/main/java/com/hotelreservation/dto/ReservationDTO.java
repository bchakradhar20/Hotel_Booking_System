package com.hotelreservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for transferring reservation data between layers.
 * Used for booking requests (incoming) and reservation responses (outgoing).
 * The totalAmount field is always calculated server-side and never accepted from the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    /** Reservation's unique identifier (null for creation, set for responses) */
    private Long reservationId;

    /** Check-in date for the reservation — must be present or future */
    @NotNull(message = "Check-in date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    /** Check-out date — must be after check-in date */
    @NotNull(message = "Check-out date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    /**
     * Total amount for the reservation.
     * Always set by the server (numberOfNights * pricePerNight).
     * Ignored if provided in the request body.
     */
    private BigDecimal totalAmount;

    /** ID of the room being reserved — required on booking request */
    @NotNull(message = "Room ID is required")
    private Long roomId;

    /** Room number for display purposes in response */
    private String roomNumber;

    /** Username of the user who made the reservation — set in response */
    private String username;

    /** User ID of the person who made the reservation */
    private Long userId;
}
