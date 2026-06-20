package com.hotelreservation.controller;

import com.hotelreservation.dto.*;
import com.hotelreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for reservation management.
 * Users can book and manage their own reservations.
 * Admins can view and delete all reservations.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Constructor injection for ReservationService.
     *
     * @param reservationService service handling reservation business logic
     */
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Books a hotel room for the authenticated user.
     * Server validates dates and calculates the total amount.
     *
     * @param reservationDTO  reservation details including room ID and dates
     * @param userDetails     the currently authenticated user
     * @return 201 Created with the created reservation
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody ReservationDTO reservationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReservationDTO created = reservationService.createReservation(
                reservationDTO, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves all reservations in the system. Requires ADMIN role.
     *
     * @return 200 OK with list of all reservations
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    /**
     * Retrieves all reservations made by the currently authenticated user.
     *
     * @param userDetails the currently authenticated user
     * @return 200 OK with the user's reservations
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservationService.getMyReservations(userDetails.getUsername()));
    }

    /**
     * Cancels a specific reservation owned by the authenticated user.
     *
     * @param reservationId the ID of the reservation to cancel
     * @param userDetails   the currently authenticated user
     * @return 200 OK with success message
     */
    @DeleteMapping("/my/{reservationId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse> cancelMyReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        reservationService.cancelMyReservation(reservationId, userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse("Reservation cancelled successfully", true));
    }

    /**
     * Deletes any reservation by ID. Requires ADMIN role.
     *
     * @param reservationId the ID of the reservation to delete
     * @return 200 OK with success message
     */
    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteReservation(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.ok(new ApiResponse("Reservation deleted successfully", true));
    }
}
