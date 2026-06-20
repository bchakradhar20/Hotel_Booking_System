package com.hotelreservation.reservation.controller;

import com.hotelreservation.reservation.dto.ApiResponse;
import com.hotelreservation.reservation.dto.ReservationDTO;
import com.hotelreservation.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for reservation management.
 *
 * <p>Follows the thin-controller principle: all business logic is delegated to
 * {@link ReservationService}. This class handles only HTTP concerns.
 *
 * <p>User identity is supplied via request headers injected by the API Gateway
 * after JWT validation — {@code X-User-Id} and {@code X-Username} — rather than
 * re-parsing the JWT token in this service.
 *
 * <p>Access policy:
 * <ul>
 *   <li>Booking and personal reservation operations — require {@code ROLE_USER} or {@code ROLE_ADMIN}.</li>
 *   <li>Admin-only operations — require {@code ROLE_ADMIN}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation booking and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Constructs the controller with the required reservation service.
     *
     * @param reservationService service handling all reservation business logic
     */
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Books a hotel room for the authenticated user.
     *
     * <p>The {@code X-User-Id} and {@code X-Username} headers are injected automatically
     * by the API Gateway from the validated JWT — the client does not send these directly.
     *
     * @param dto      the validated reservation request containing roomId and dates
     * @param userId   the authenticated user's ID, forwarded by the API Gateway
     * @param username the authenticated user's username, forwarded by the API Gateway
     * @return {@code 201 Created} with the confirmed reservation details
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Book a room")
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody ReservationDTO dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username) {
        // Parse userId from the String header (gateway serialises the Long claim as a String)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(dto, Long.parseLong(userId), username));
    }

    /**
     * Retrieves all reservations in the system. Requires {@code ROLE_ADMIN}.
     *
     * @return {@code 200 OK} with a list of every reservation
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reservations (ADMIN only)")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    /**
     * Retrieves all reservations belonging to the currently authenticated user.
     *
     * @param userId the authenticated user's ID, forwarded by the API Gateway
     * @return {@code 200 OK} with the user's reservations ordered by check-in date descending
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get my reservations")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(reservationService.getMyReservations(Long.parseLong(userId)));
    }

    /**
     * Cancels a specific reservation owned by the authenticated user.
     *
     * <p>The service layer enforces ownership — a user cannot cancel another user's reservation.
     *
     * @param reservationId the ID of the reservation to cancel
     * @param userId        the authenticated user's ID, forwarded by the API Gateway
     * @return {@code 200 OK} with a success message
     */
    @DeleteMapping("/my/{reservationId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Cancel my reservation")
    public ResponseEntity<ApiResponse> cancelMyReservation(
            @PathVariable Long reservationId,
            @RequestHeader("X-User-Id") String userId) {
        reservationService.cancelMyReservation(reservationId, Long.parseLong(userId));
        return ResponseEntity.ok(new ApiResponse("Reservation cancelled successfully", true));
    }

    /**
     * Deletes any reservation by ID regardless of ownership. Requires {@code ROLE_ADMIN}.
     *
     * @param reservationId the ID of the reservation to delete
     * @return {@code 200 OK} with a success message
     */
    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete any reservation (ADMIN only)")
    public ResponseEntity<ApiResponse> deleteReservation(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.ok(new ApiResponse("Reservation deleted successfully", true));
    }

    @PutMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update any reservation dates (ADMIN only)")
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationDTO dto) {
        return ResponseEntity.ok(reservationService.updateReservation(reservationId, dto));
    }
}
