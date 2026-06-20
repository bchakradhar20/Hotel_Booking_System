package com.hotelreservation.reservation.service;

import com.hotelreservation.reservation.client.RoomClient;
import com.hotelreservation.reservation.dto.ReservationDTO;
import com.hotelreservation.reservation.dto.RoomDTO;
import com.hotelreservation.reservation.entity.Reservation;
import com.hotelreservation.reservation.exception.APIException;
import com.hotelreservation.reservation.exception.ResourceNotFoundException;
import com.hotelreservation.reservation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for reservation management business logic.
 *
 * <p>Handles booking creation with date validation, availability checking, and
 * server-side billing calculation. Also provides retrieval and cancellation operations.
 *
 * <p>Room data is fetched from the Room Service via {@link RoomClient} (OpenFeign),
 * keeping each service's data isolated in its own database (microservice data independence).
 *
 * <p>Key business rules enforced:
 * <ul>
 *   <li>Check-in must be strictly before check-out.</li>
 *   <li>Check-in date cannot be in the past.</li>
 *   <li>No two reservations for the same room may overlap in dates.</li>
 *   <li>Total amount is always computed server-side — never accepted from the client.</li>
 * </ul>
 */
@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;

    /**
     * Constructs the service with the required repository and Feign client.
     *
     * @param reservationRepository JPA repository for reservation persistence
     * @param roomClient            Feign client for fetching room data from the Room Service
     */
    public ReservationService(ReservationRepository reservationRepository, RoomClient roomClient) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
    }

    /**
     * Creates a new reservation after passing all validation checks.
     *
     * <p>Validation and processing steps:
     * <ol>
     *   <li>Fetch room details from the Room Service via Feign.</li>
     *   <li>Validate that check-in is strictly before check-out.</li>
     *   <li>Validate that check-in is not in the past.</li>
     *   <li>Check for date overlap with existing reservations for the same room.</li>
     *   <li>Calculate total amount server-side: {@code nights × pricePerNight}.</li>
     *   <li>Persist and return the new reservation.</li>
     * </ol>
     *
     * @param dto      the reservation request containing roomId and date range
     * @param userId   the authenticated user's ID (extracted from JWT by the API Gateway)
     * @param username the authenticated user's username (forwarded by the API Gateway)
     * @return the persisted reservation as a {@link ReservationDTO}
     * @throws APIException with {@code 400 BAD_REQUEST} for invalid dates
     * @throws APIException with {@code 409 CONFLICT} if the room is already booked
     */
    @Transactional
    public ReservationDTO createReservation(ReservationDTO dto, Long userId, String username) {
        log.info("Creating reservation: userId={}, roomId={}, checkIn={}, checkOut={}",
                userId, dto.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate());
        RoomDTO room = roomClient.getRoomById(dto.getRoomId());

        if (!dto.getCheckInDate().isBefore(dto.getCheckOutDate())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Check-in date must be before check-out date");
        }
        if (dto.getCheckInDate().isBefore(LocalDate.now(java.time.ZoneId.systemDefault()))) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Check-in date cannot be in the past");
        }

        boolean isRoomUnavailable = reservationRepository.existsOverlappingReservation(
                dto.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate());
        if (isRoomUnavailable) {
            log.warn("Room {} not available for dates {} to {}", dto.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate());
            throw new APIException(HttpStatus.CONFLICT,
                    "Room is not available for the selected dates. Please choose different dates.");
        }

        long numberOfNights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

        Reservation reservation = new Reservation();
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        reservation.setTotalAmount(totalAmount);
        reservation.setUserId(userId);
        reservation.setUsername(username);
        reservation.setRoomId(room.getRoomId());
        reservation.setRoomNumber(room.getRoomNumber());

        ReservationDTO result = toDTO(reservationRepository.save(reservation));
        log.info("Reservation created: id={}", result.getReservationId());
        return result;
    }

    @Transactional
    public ReservationDTO updateReservation(Long reservationId, ReservationDTO dto) {
        log.info("Admin updating reservation id={}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "reservationId", reservationId));

        if (!dto.getCheckInDate().isBefore(dto.getCheckOutDate())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Check-in date must be before check-out date");
        }

        // Check overlap excluding the current reservation
        boolean hasConflict = reservationRepository.existsOverlappingReservationExcluding(
                reservation.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate(), reservationId);
        if (hasConflict) {
            log.warn("Reservation update conflict for room {} on dates {} to {}",
                    reservation.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate());
            throw new APIException(HttpStatus.CONFLICT,
                    "Room is not available for the selected dates.");
        }

        // Recalculate total using current room price from Room Service
        RoomDTO room = roomClient.getRoomById(reservation.getRoomId());
        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        reservation.setTotalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));

        ReservationDTO result = toDTO(reservationRepository.save(reservation));
        log.info("Reservation updated: id={}", reservationId);
        return result;
    }

    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Retrieves all reservations belonging to a specific user, ordered by check-in date descending.
     *
     * @param userId the ID of the authenticated user
     * @return the user's reservations as a list of {@link ReservationDTO}
     */
    public List<ReservationDTO> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCheckInDateDesc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Cancels a specific reservation owned by the requesting user.
     *
     * <p>Ownership is verified before deletion: a user may not cancel another
     * user's reservation through this method.
     *
     * @param reservationId the ID of the reservation to cancel
     * @param userId        the authenticated user's ID
     * @throws ResourceNotFoundException if the reservation does not exist
     * @throws APIException with {@code 403 FORBIDDEN} if the reservation belongs to a different user
     */
    @Transactional
    public void cancelMyReservation(Long reservationId, Long userId) {
        log.info("User {} cancelling reservation id={}", userId, reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation", "reservationId", reservationId));
        if (!reservation.getUserId().equals(userId)) {
            log.warn("Forbidden: user {} tried to cancel reservation {} owned by {}",
                    userId, reservationId, reservation.getUserId());
            throw new APIException(HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        }
        reservationRepository.delete(reservation);
        log.info("Reservation cancelled: id={}", reservationId);
    }

    /**
     * Deletes any reservation by ID regardless of ownership. Intended for admin use only.
     *
     * @param reservationId the ID of the reservation to delete
     * @throws ResourceNotFoundException if no reservation with the given ID exists
     */
    @Transactional
    public void deleteReservation(Long reservationId) {
        log.info("Admin deleting reservation id={}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation", "reservationId", reservationId));
        reservationRepository.delete(reservation);
        log.info("Reservation deleted: id={}", reservationId);
    }

    /**
     * Maps a {@link Reservation} entity to a {@link ReservationDTO}.
     *
     * <p>This private mapper keeps the entity fields flat in the DTO, avoiding
     * the need for a full ModelMapper dependency in this service (which has no nested
     * JPA relationships — room and user data are stored as plain columns).
     *
     * @param reservation the entity to convert
     * @return the mapped DTO
     */
    private ReservationDTO toDTO(Reservation reservation) {
        return new ReservationDTO(
                reservation.getReservationId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getTotalAmount(),
                reservation.getRoomId(),
                reservation.getRoomNumber(),
                reservation.getUsername(),
                reservation.getUserId()
        );
    }
}
