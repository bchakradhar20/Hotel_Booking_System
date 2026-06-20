package com.hotelreservation.service;

import com.hotelreservation.dto.ReservationDTO;

import java.util.List;

/**
 * Service interface defining reservation management operations.
 */
public interface ReservationService {

    /**
     * Creates a new reservation after validating room availability and date conflicts.
     * Calculates the total amount server-side.
     *
     * @param reservationDTO reservation details from the client
     * @param username       the authenticated user's username
     * @return the created reservation as a DTO
     */
    ReservationDTO createReservation(ReservationDTO reservationDTO, String username);

    /**
     * Retrieves all reservations in the system (admin only).
     *
     * @return list of all reservations as DTOs
     */
    List<ReservationDTO> getAllReservations();

    /**
     * Retrieves all reservations belonging to a specific user.
     *
     * @param username the authenticated user's username
     * @return list of the user's reservations as DTOs
     */
    List<ReservationDTO> getMyReservations(String username);

    /**
     * Cancels (deletes) a reservation owned by the authenticated user.
     *
     * @param reservationId the ID of the reservation to cancel
     * @param username      the authenticated user's username (for ownership check)
     */
    void cancelMyReservation(Long reservationId, String username);

    /**
     * Deletes any reservation by ID (admin only).
     *
     * @param reservationId the ID of the reservation to delete
     */
    void deleteReservation(Long reservationId);
}
