package com.hotelreservation.mapper;

import com.hotelreservation.dto.RoomDTO;
import com.hotelreservation.entity.Room;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting between Room entity and RoomDTO.
 * Uses ModelMapper for automatic field matching.
 */
@Component
public class RoomMapper {

    private final ModelMapper modelMapper;

    /**
     * Constructor injection for ModelMapper dependency.
     *
     * @param modelMapper the shared ModelMapper bean
     */
    public RoomMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts a Room entity to a RoomDTO.
     * All fields (roomId, roomNumber, roomType, pricePerNight, capacity, description)
     * are automatically matched by ModelMapper due to identical naming.
     *
     * @param room the Room entity to convert
     * @return the mapped RoomDTO
     */
    public RoomDTO toDTO(Room room) {
        return modelMapper.map(room, RoomDTO.class);
    }

    /**
     * Converts a RoomDTO to a Room entity.
     * Used for creation and update operations.
     * The roomId field is typically null for new rooms and set for updates.
     *
     * @param roomDTO the RoomDTO to convert
     * @return the mapped Room entity
     */
    public Room toEntity(RoomDTO roomDTO) {
        return modelMapper.map(roomDTO, Room.class);
    }
}
