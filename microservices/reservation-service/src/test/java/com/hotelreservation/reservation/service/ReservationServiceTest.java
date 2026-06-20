package com.hotelreservation.reservation.service;

import com.hotelreservation.reservation.client.RoomClient;
import com.hotelreservation.reservation.dto.ReservationDTO;
import com.hotelreservation.reservation.dto.RoomDTO;
import com.hotelreservation.reservation.entity.Reservation;
import com.hotelreservation.reservation.exception.APIException;
import com.hotelreservation.reservation.exception.ResourceNotFoundException;
import com.hotelreservation.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReservationService}.
 * All dependencies are mocked — no Spring context, database, or Feign network required.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private RoomClient roomClient;

    @InjectMocks
    private ReservationService reservationService;

    private RoomDTO roomDTO;
    private ReservationDTO reservationDTO;
    private Reservation savedReservation;

    // Fixed future dates used across all tests
    private static final LocalDate TOMORROW       = LocalDate.now().plusDays(1);
    private static final LocalDate IN_THREE_DAYS  = LocalDate.now().plusDays(3);
    private static final LocalDate YESTERDAY      = LocalDate.now().minusDays(1);

    @BeforeEach
    void setUp() {
        roomDTO = new RoomDTO(1L, "101", "STANDARD", new BigDecimal("100.00"), 2, "Nice room");

        reservationDTO = new ReservationDTO();
        reservationDTO.setRoomId(1L);
        reservationDTO.setCheckInDate(TOMORROW);
        reservationDTO.setCheckOutDate(IN_THREE_DAYS);

        // 2 nights × $100 = $200
        savedReservation = new Reservation(
                10L, TOMORROW, IN_THREE_DAYS,
                new BigDecimal("200.00"), 1L, "john", 1L, "101");
    }

    // ── createReservation ─────────────────────────────────────────────────────

    @Test
    void createReservation_success_persistsAndReturnsDTO() {
        when(roomClient.getRoomById(1L)).thenReturn(roomDTO);
        when(reservationRepository.existsOverlappingReservation(1L, TOMORROW, IN_THREE_DAYS))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationDTO result = reservationService.createReservation(reservationDTO, 1L, "john");

        assertThat(result.getTotalAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getRoomNumber()).isEqualTo("101");
        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getReservationId()).isEqualTo(10L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_calculatesTotalCorrectly_forFiveNights() {
        // 5 nights × $100 = $500
        LocalDate checkOut = TOMORROW.plusDays(5);
        reservationDTO.setCheckOutDate(checkOut);
        Reservation fiveNights = new Reservation(
                11L, TOMORROW, checkOut, new BigDecimal("500.00"), 1L, "john", 1L, "101");

        when(roomClient.getRoomById(1L)).thenReturn(roomDTO);
        when(reservationRepository.existsOverlappingReservation(1L, TOMORROW, checkOut))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(fiveNights);

        ReservationDTO result = reservationService.createReservation(reservationDTO, 1L, "john");

        assertThat(result.getTotalAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void createReservation_throwsBadRequest_whenCheckInIsAfterCheckOut() {
        reservationDTO.setCheckInDate(IN_THREE_DAYS);
        reservationDTO.setCheckOutDate(TOMORROW);
        when(roomClient.getRoomById(1L)).thenReturn(roomDTO);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDTO, 1L, "john"))
                .isInstanceOf(APIException.class)
                .hasMessage("Check-in date must be before check-out date")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_throwsBadRequest_whenCheckInEqualsCheckOut() {
        reservationDTO.setCheckInDate(TOMORROW);
        reservationDTO.setCheckOutDate(TOMORROW);
        when(roomClient.getRoomById(1L)).thenReturn(roomDTO);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDTO, 1L, "john"))
                .isInstanceOf(APIException.class)
                .hasMessage("Check-in date must be before check-out date");
    }

    @Test
    void createReservation_throwsBadRequest_whenCheckInIsInPast() {
        reservationDTO.setCheckInDate(YESTERDAY);
        reservationDTO.setCheckOutDate(TOMORROW);
        when(roomClient.getRoomById(1L)).thenReturn(roomDTO);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDTO, 1L, "john"))
                .isInstanceOf(APIException.class)
                .hasMessage("Check-in date cannot be in the past")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createReservation_throwsConflict_whenRoomAlreadyBooked() {
        when(roomClient.getRoomById(1L)).thenReturn(roomDTO);
        when(reservationRepository.existsOverlappingReservation(1L, TOMORROW, IN_THREE_DAYS))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDTO, 1L, "john"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("not available")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(reservationRepository, never()).save(any());
    }

    // ── getAllReservations ─────────────────────────────────────────────────────

    @Test
    void getAllReservations_returnsAllReservations() {
        when(reservationRepository.findAll()).thenReturn(List.of(savedReservation));

        List<ReservationDTO> result = reservationService.getAllReservations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationId()).isEqualTo(10L);
    }

    @Test
    void getAllReservations_returnsEmptyListWhenNoneExist() {
        when(reservationRepository.findAll()).thenReturn(List.of());

        assertThat(reservationService.getAllReservations()).isEmpty();
    }

    // ── getMyReservations ─────────────────────────────────────────────────────

    @Test
    void getMyReservations_returnsReservationsForGivenUser() {
        when(reservationRepository.findByUserIdOrderByCheckInDateDesc(1L))
                .thenReturn(List.of(savedReservation));

        List<ReservationDTO> result = reservationService.getMyReservations(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getUsername()).isEqualTo("john");
    }

    @Test
    void getMyReservations_returnsEmptyListWhenUserHasNone() {
        when(reservationRepository.findByUserIdOrderByCheckInDateDesc(99L))
                .thenReturn(List.of());

        assertThat(reservationService.getMyReservations(99L)).isEmpty();
    }

    // ── cancelMyReservation ───────────────────────────────────────────────────

    @Test
    void cancelMyReservation_success_deletesReservation() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(savedReservation));

        reservationService.cancelMyReservation(10L, 1L);

        verify(reservationRepository).delete(savedReservation);
    }

    @Test
    void cancelMyReservation_throwsNotFound_whenReservationMissing() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelMyReservation(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation");

        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void cancelMyReservation_throwsForbidden_whenCallerIsNotOwner() {
        // savedReservation belongs to userId=1, but caller is userId=2
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(savedReservation));

        assertThatThrownBy(() -> reservationService.cancelMyReservation(10L, 2L))
                .isInstanceOf(APIException.class)
                .hasMessage("You can only cancel your own reservations")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(reservationRepository, never()).delete(any());
    }

    // ── deleteReservation ─────────────────────────────────────────────────────

    @Test
    void deleteReservation_success_deletesAnyReservation() {
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(savedReservation));

        reservationService.deleteReservation(10L);

        verify(reservationRepository).delete(savedReservation);
    }

    @Test
    void deleteReservation_throwsNotFound_whenReservationMissing() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteReservation(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reservation");

        verify(reservationRepository, never()).delete(any());
    }
}
