package com.hotelreservation.room.service;

import com.hotelreservation.room.dto.*;
import com.hotelreservation.room.entity.Room;
import com.hotelreservation.room.exception.*;
import com.hotelreservation.room.mapper.RoomMapper;
import com.hotelreservation.room.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public RoomService(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream().map(roomMapper::toDTO).collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long roomId) {
        return roomMapper.toDTO(roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomId", roomId)));
    }

    @Transactional
    public RoomDTO createRoom(RoomDTO roomDTO) {
        if (roomRepository.existsByRoomNumber(roomDTO.getRoomNumber()))
            throw new APIException(HttpStatus.BAD_REQUEST, "Room number '" + roomDTO.getRoomNumber() + "' already exists");
        return roomMapper.toDTO(roomRepository.save(roomMapper.toEntity(roomDTO)));
    }

    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomDTO roomDTO) {
        Room existing = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomId", roomId));
        if (roomRepository.existsByRoomNumberAndRoomIdNot(roomDTO.getRoomNumber(), roomId))
            throw new APIException(HttpStatus.BAD_REQUEST, "Room number '" + roomDTO.getRoomNumber() + "' is already used by another room");
        existing.setRoomNumber(roomDTO.getRoomNumber());
        existing.setRoomType(roomDTO.getRoomType());
        existing.setPricePerNight(roomDTO.getPricePerNight());
        existing.setCapacity(roomDTO.getCapacity());
        existing.setDescription(roomDTO.getDescription());
        return roomMapper.toDTO(roomRepository.save(existing));
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        roomRepository.delete(roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomId", roomId)));
    }
}
