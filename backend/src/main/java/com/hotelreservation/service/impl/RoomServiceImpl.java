package com.hotelreservation.service.impl;

import com.hotelreservation.dto.RoomDTO;
import com.hotelreservation.entity.Room;
import com.hotelreservation.exception.*;
import com.hotelreservation.mapper.RoomMapper;
import com.hotelreservation.repository.RoomRepository;
import com.hotelreservation.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of RoomService providing room management business logic.
 */
@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    /**
     * Constructor injection for room repository and mapper dependencies.
     *
     * @param roomRepository repository for room database operations
     * @param roomMapper     mapper for Room entity / DTO conversions
     */
    public RoomServiceImpl(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    /**
     * Retrieves all rooms from the database and maps them to DTOs.
     *
     * @return list of all room DTOs
     */
    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(roomMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single room by ID.
     *
     * @param roomId the unique identifier of the room
     * @return the room DTO if found
     * @throws ResourceNotFoundException if no room exists with the given ID
     */
    @Override
    public RoomDTO getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomId", roomId));
        return roomMapper.toDTO(room);
    }

    /**
     * Creates a new room after validating that the room number is unique.
     *
     * @param roomDTO the room data for creation
     * @return the persisted room as a DTO
     * @throws APIException if the room number is already in use
     */
    @Override
    @Transactional
    public RoomDTO createRoom(RoomDTO roomDTO) {
        // Validate room number uniqueness before persisting
        if (roomRepository.existsByRoomNumber(roomDTO.getRoomNumber())) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                    "Room number '" + roomDTO.getRoomNumber() + "' already exists");
        }
        Room room = roomMapper.toEntity(roomDTO);
        Room savedRoom = roomRepository.save(room);
        return roomMapper.toDTO(savedRoom);
    }

    /**
     * Updates an existing room's details.
     * Validates room number uniqueness while excluding the current room from the check.
     *
     * @param roomId  the ID of the room to update
     * @param roomDTO the new room data
     * @return the updated room as a DTO
     * @throws ResourceNotFoundException if the room does not exist
     * @throws APIException              if the new room number conflicts with another room
     */
    @Override
    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomDTO roomDTO) {
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomId", roomId));

        // Check if the updated room number conflicts with any other room (not this one)
        if (roomRepository.existsByRoomNumberAndRoomIdNot(roomDTO.getRoomNumber(), roomId)) {
            throw new APIException(HttpStatus.BAD_REQUEST,
                    "Room number '" + roomDTO.getRoomNumber() + "' is already used by another room");
        }

        // Update each field explicitly to prevent unintended overwrites
        existingRoom.setRoomNumber(roomDTO.getRoomNumber());
        existingRoom.setRoomType(roomDTO.getRoomType());
        existingRoom.setPricePerNight(roomDTO.getPricePerNight());
        existingRoom.setCapacity(roomDTO.getCapacity());
        existingRoom.setDescription(roomDTO.getDescription());

        Room updatedRoom = roomRepository.save(existingRoom);
        return roomMapper.toDTO(updatedRoom);
    }

    /**
     * Deletes a room from the system.
     *
     * @param roomId the ID of the room to delete
     * @throws ResourceNotFoundException if the room does not exist
     */
    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "roomId", roomId));
        roomRepository.delete(room);
    }
}
