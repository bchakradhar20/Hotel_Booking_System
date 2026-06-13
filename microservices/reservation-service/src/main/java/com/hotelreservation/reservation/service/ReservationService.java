package com.hotelreservation.reservation.service;

import com.hotelreservation.reservation.client.RoomClient;
import com.hotelreservation.reservation.dto.*;
import com.hotelreservation.reservation.entity.Reservation;
import com.hotelreservation.reservation.exception.*;
import com.hotelreservation.reservation.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;

    public ReservationService(ReservationRepository reservationRepository, RoomClient roomClient) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
    }

    @Transactional
    public ReservationDTO createReservation(ReservationDTO dto, Long userId, String username) {
        RoomDTO room = roomClient.getRoomById(dto.getRoomId());

        if (!dto.getCheckInDate().isBefore(dto.getCheckOutDate()))
            throw new APIException(HttpStatus.BAD_REQUEST, "Check-in date must be before check-out date");
        if (dto.getCheckInDate().isBefore(LocalDate.now()))
            throw new APIException(HttpStatus.BAD_REQUEST, "Check-in date cannot be in the past");
        if (reservationRepository.existsOverlappingReservation(dto.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate()))
            throw new APIException(HttpStatus.CONFLICT, "Room is not available for the selected dates. Please choose different dates.");

        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = new Reservation();
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        reservation.setTotalAmount(totalAmount);
        reservation.setUserId(userId);
        reservation.setUsername(username);
        reservation.setRoomId(room.getRoomId());
        reservation.setRoomNumber(room.getRoomNumber());

        return toDTO(reservationRepository.save(reservation));
    }

    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ReservationDTO> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCheckInDateDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void cancelMyReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "reservationId", reservationId));
        if (!reservation.getUserId().equals(userId))
            throw new APIException(HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        reservationRepository.delete(reservation);
    }

    @Transactional
    public void deleteReservation(Long reservationId) {
        reservationRepository.delete(reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "reservationId", reservationId)));
    }

    private ReservationDTO toDTO(Reservation r) {
        return new ReservationDTO(r.getReservationId(), r.getCheckInDate(), r.getCheckOutDate(),
                r.getTotalAmount(), r.getRoomId(), r.getRoomNumber(), r.getUsername(), r.getUserId());
    }
}
