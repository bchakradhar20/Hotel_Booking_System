package com.hotelreservation.service.impl;

import com.hotelreservation.dto.ReservationDTO;
import com.hotelreservation.entity.*;
import com.hotelreservation.exception.*;
import com.hotelreservation.mapper.ReservationMapper;
import com.hotelreservation.repository.*;
import com.hotelreservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ReservationService providing reservation management business logic.
 * Handles date validation, overlap detection, billing calculation, and ownership verification.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ReservationMapper reservationMapper;

    /**
     * Constructor injection for all required dependencies.
     *
     * @param reservationRepository repository for reservation operations
     * @param userRepository        repository for user lookups
     * @param roomRepository        repository for room lookups
     * @param reservationMapper     mapper for Reservation entity / DTO conversions
     */
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  UserRepository userRepository,
                                  RoomRepository roomRepository,
                                  ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.reservationMapper = reservationMapper;
    }

    /**
     * Creates a new reservation after validating room availability and date integrity.
     * Billing amount is always calculated server-side using: numberOfNights * pricePerNight.
     * Any totalAmount value from the client is ignored.
     *
     * Validation steps:
     * 1. Verify check-in is before check-out.
     * 2. Verify check-in is not in the past.
     * 3. Check for date overlap with existing reservations for the same room.
     * 4. Calculate total amount on the server.
     *
     * @param reservationDTO reservation details provided by the client
     * @param username       the authenticated user making the reservation
     * @return the created reservation as a DTO
     */
    @Override
    @Transactional
    public ReservationDTO createReservation(ReservationDTO reservationDTO, String username) {
        // Fetch and validate the requesting user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Fetch and validate the requested room
        Room room = roomRepository.findById(reservationDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomId", reservationDTO.getRoomId()));

        // Step 1: Validate that check-in date is strictly before check-out date
        if (!reservationDTO.getCheckInDate().isBefore(reservationDTO.getCheckOutDate())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Check-in date must be before check-out date");
        }

        // Step 2: Validate that check-in date is not in the past
        if (reservationDTO.getCheckInDate().isBefore(java.time.LocalDate.now())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Check-in date cannot be in the past");
        }

        // Step 3: Check whether requested reservation dates overlap
        // with any existing reservation for the selected room.
        // If overlap exists, booking should be rejected.
        // Overlap condition: existing.checkIn < requested.checkOut AND existing.checkOut > requested.checkIn
        boolean hasOverlap = reservationRepository.existsOverlappingReservation(
                room.getRoomId(),
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate()
        );

        if (hasOverlap) {
            throw new APIException(HttpStatus.CONFLICT,
                    "Room is not available for the selected dates. Please choose different dates.");
        }

        // Step 4: Calculate total reservation amount server-side only.
        // Formula: numberOfNights * room.pricePerNight
        // ChronoUnit.DAYS.between returns the number of nights between the dates.
        long numberOfNights = ChronoUnit.DAYS.between(
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate()
        );
        // Multiply the nightly rate by the number of nights to get the total charge
        BigDecimal totalAmount = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(numberOfNights));

        // Build the Reservation entity with all computed values
        Reservation reservation = new Reservation();
        reservation.setCheckInDate(reservationDTO.getCheckInDate());
        reservation.setCheckOutDate(reservationDTO.getCheckOutDate());
        reservation.setTotalAmount(totalAmount);
        reservation.setUser(user);
        reservation.setRoom(room);

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toDTO(saved);
    }

    /**
     * Retrieves all reservations in the system (admin access only).
     *
     * @return list of all reservation DTOs
     */
    @Override
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all reservations belonging to the authenticated user.
     *
     * @param username the authenticated user's username
     * @return list of the user's reservations as DTOs
     */
    @Override
    public List<ReservationDTO> getMyReservations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return reservationRepository.findByUserUserIdOrderByCheckInDateDesc(user.getUserId())
                .stream()
                .map(reservationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cancels a reservation owned by the authenticated user.
     * Verifies that the reservation belongs to the requesting user before deletion.
     *
     * @param reservationId the ID of the reservation to cancel
     * @param username      the authenticated user's username
     * @throws APIException if the reservation does not belong to this user
     */
    @Override
    @Transactional
    public void cancelMyReservation(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "reservationId", reservationId));

        // Verify ownership — only the reservation owner can cancel it
        if (!reservation.getUser().getUsername().equals(username)) {
            throw new APIException(HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        }

        reservationRepository.delete(reservation);
    }

    /**
     * Deletes any reservation by ID — for admin use only.
     *
     * @param reservationId the ID of the reservation to delete
     * @throws ResourceNotFoundException if no reservation with the given ID exists
     */
    @Override
    @Transactional
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "reservationId", reservationId));
        reservationRepository.delete(reservation);
    }
}
