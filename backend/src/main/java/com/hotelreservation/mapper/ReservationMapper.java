package com.hotelreservation.mapper;

import com.hotelreservation.dto.ReservationDTO;
import com.hotelreservation.entity.Reservation;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting between Reservation entity and ReservationDTO.
 * Handles custom field mapping for nested User and Room objects in the entity.
 */
@Component
public class ReservationMapper {

    private final ModelMapper modelMapper;

    /**
     * Constructor injection for ModelMapper dependency.
     *
     * @param modelMapper the shared ModelMapper bean
     */
    public ReservationMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts a Reservation entity to a ReservationDTO.
     * Manually maps the nested User and Room fields because ModelMapper's
     * STRICT strategy does not resolve nested property paths automatically.
     *
     * Mapping details:
     * - reservationId, checkInDate, checkOutDate, totalAmount → direct mapping
     * - room.roomId   → roomId
     * - room.roomNumber → roomNumber
     * - user.username → username
     * - user.userId   → userId
     *
     * @param reservation the Reservation entity to convert
     * @return the mapped ReservationDTO with flattened user and room fields
     */
    public ReservationDTO toDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setReservationId(reservation.getReservationId());
        dto.setCheckInDate(reservation.getCheckInDate());
        dto.setCheckOutDate(reservation.getCheckOutDate());
        dto.setTotalAmount(reservation.getTotalAmount());

        // Flatten nested Room fields into the DTO
        if (reservation.getRoom() != null) {
            dto.setRoomId(reservation.getRoom().getRoomId());
            dto.setRoomNumber(reservation.getRoom().getRoomNumber());
        }

        // Flatten nested User fields into the DTO
        if (reservation.getUser() != null) {
            dto.setUsername(reservation.getUser().getUsername());
            dto.setUserId(reservation.getUser().getUserId());
        }

        return dto;
    }

    /**
     * Converts a ReservationDTO to a Reservation entity.
     * Note: The User and Room associations must be set separately in the service layer
     * since they require database lookups that cannot be performed in the mapper.
     *
     * @param reservationDTO the ReservationDTO to convert
     * @return a partial Reservation entity (without User/Room associations)
     */
    public Reservation toEntity(ReservationDTO reservationDTO) {
        Reservation reservation = new Reservation();
        reservation.setCheckInDate(reservationDTO.getCheckInDate());
        reservation.setCheckOutDate(reservationDTO.getCheckOutDate());
        // totalAmount is always calculated server-side and not mapped from DTO
        return reservation;
    }
}
