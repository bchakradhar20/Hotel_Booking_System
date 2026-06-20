package com.hotelreservation.dto;

import com.hotelreservation.entity.Room;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for transferring room data between layers.
 * Used for both room creation/update requests and response payloads.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    /** Room's unique identifier (null for creation, set for responses) */
    private Long roomId;

    /** Unique room number identifier (e.g., "101", "202A") */
    @NotBlank(message = "Room number is required")
    private String roomNumber;

    /** Room category: STANDARD, DELUXE, or SUITE */
    @NotNull(message = "Room type is required")
    private Room.RoomType roomType;

    /** Nightly rate for this room — must be a positive value */
    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal pricePerNight;

    /** Maximum number of guests allowed in this room */
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    /** Optional text description providing more details about the room */
    private String description;
}
