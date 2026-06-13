package com.hotelreservation.room.mapper;

import com.hotelreservation.room.dto.RoomDTO;
import com.hotelreservation.room.entity.Room;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {
    private final ModelMapper modelMapper;
    public RoomMapper(ModelMapper modelMapper) { this.modelMapper = modelMapper; }
    public RoomDTO toDTO(Room room) { return modelMapper.map(room, RoomDTO.class); }
    public Room toEntity(RoomDTO dto) { return modelMapper.map(dto, Room.class); }
}
