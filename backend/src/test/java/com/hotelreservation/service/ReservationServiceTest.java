package com.hotelreservation.service;

import com.hotelreservation.dto.ReservationDTO;
import com.hotelreservation.entity.*;
import com.hotelreservation.exception.*;
import com.hotelreservation.mapper.ReservationMapper;
import com.hotelreservation.repository.*;
import com.hotelreservation.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationServiceImpl covering booking scenarios,
 * date validation, overlap detection, and ownership verification.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User user;
    private Room room;
    private ReservationDTO reservationDTO;

    @BeforeEach
    void setUp() {
        user = new User(1L, "john", "john@email.com", "encoded", "123", Set.of());
        room = new Room(1L, "101", Room.RoomType.STANDARD, new BigDecimal("100.00"), 2, "Standard");

        reservationDTO = new ReservationDTO();
        reservationDTO.setRoomId(1L);
        reservationDTO.setCheckInDate(LocalDate.now().plusDays(1));
        reservationDTO.setCheckOutDate(LocalDate.now().plusDays(3));
    }

    /**
     * Tests successful reservation creation with no date conflicts.
     * Verifies totalAmount is calculated as numberOfNights * pricePerNight.
     */
    @Test
    void createReservation_NoConflict_Success() {
        Reservation saved = new Reservation(1L,
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate(),
                new BigDecimal("200.00"), user, room);
        ReservationDTO expectedDTO = new ReservationDTO(1L,
                reservationDTO.getCheckInDate(),
                reservationDTO.getCheckOutDate(),
                new BigDecimal("200.00"), 1L, "101", "john", 1L);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(eq(1L),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);
        when(reservationMapper.toDTO(saved)).thenReturn(expectedDTO);

        ReservationDTO result = reservationService.createReservation(reservationDTO, "john");

        // Verify total = 2 nights * $100 = $200
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(reservationRepository).save(any(Reservation.class));
    }

    /**
     * Tests that booking is rejected when date overlap exists with an existing reservation.
     */
    @Test
    void createReservation_DateOverlap_ThrowsException() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(eq(1L),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(reservationDTO, "john"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("not available");
    }

    /**
     * Tests that booking is rejected when check-in date equals or is after check-out date.
     */
    @Test
    void createReservation_InvalidDates_ThrowsException() {
        reservationDTO.setCheckInDate(LocalDate.now().plusDays(3));
        reservationDTO.setCheckOutDate(LocalDate.now().plusDays(1));

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> reservationService.createReservation(reservationDTO, "john"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("before check-out");
    }

    /**
     * Tests that a user cannot cancel another user's reservation.
     */
    @Test
    void cancelMyReservation_NotOwner_ThrowsException() {
        User otherUser = new User(2L, "jane", "jane@email.com", "encoded", "456", Set.of());
        Reservation reservation = new Reservation(1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                new BigDecimal("200.00"), otherUser, room);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelMyReservation(1L, "john"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("own reservations");
    }

    /**
     * Tests that a user can successfully cancel their own reservation.
     */
    @Test
    void cancelMyReservation_Owner_Success() {
        Reservation reservation = new Reservation(1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                new BigDecimal("200.00"), user, room);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatCode(() -> reservationService.cancelMyReservation(1L, "john"))
                .doesNotThrowAnyException();
        verify(reservationRepository).delete(reservation);
    }
}
