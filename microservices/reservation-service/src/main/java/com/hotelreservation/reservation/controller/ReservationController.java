package com.hotelreservation.reservation.controller;

import com.hotelreservation.reservation.dto.*;
import com.hotelreservation.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Book a room")
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody ReservationDTO dto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(dto, Long.parseLong(userId), username));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reservations (ADMIN only)")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get my reservations")
    public ResponseEntity<List<ReservationDTO>> getMyReservations(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(reservationService.getMyReservations(Long.parseLong(userId)));
    }

    @DeleteMapping("/my/{reservationId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Cancel my reservation")
    public ResponseEntity<ApiResponse> cancelMyReservation(
            @PathVariable Long reservationId,
            @RequestHeader("X-User-Id") String userId) {
        reservationService.cancelMyReservation(reservationId, Long.parseLong(userId));
        return ResponseEntity.ok(new ApiResponse("Reservation cancelled successfully", true));
    }

    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete any reservation (ADMIN only)")
    public ResponseEntity<ApiResponse> deleteReservation(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.ok(new ApiResponse("Reservation deleted successfully", true));
    }
}
