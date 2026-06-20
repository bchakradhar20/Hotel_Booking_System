package com.hotelreservation.room.service;

import com.hotelreservation.room.dto.RoomDTO;
import com.hotelreservation.room.entity.Room;
import com.hotelreservation.room.exception.APIException;
import com.hotelreservation.room.exception.ResourceNotFoundException;
import com.hotelreservation.room.mapper.RoomMapper;
import com.hotelreservation.room.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for hotel room management business logic.
 *
 * <p>Handles all CRUD operations for rooms: retrieval, creation, update, and deletion.
 * Business rules enforced here:
 * <ul>
 *   <li>Room numbers must be unique across all rooms.</li>
 *   <li>On update, the existing room's number may stay the same, but must not
 *       conflict with any <em>other</em> room's number.</li>
 * </ul>
 *
 * <p>Entity-to-DTO mapping is delegated to {@link RoomMapper}, keeping this class
 * focused on business logic (Single Responsibility Principle).
 */
@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);
    private static final String ROOM_ID_FIELD = "roomId";

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    /**
     * Constructs the service with required repository and mapper dependencies.
     *
     * @param roomRepository JPA repository for room persistence operations
     * @param roomMapper     mapper for converting between {@link Room} entity and {@link RoomDTO}
     */
    public RoomService(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    /**
     * Retrieves all hotel rooms from the database.
     *
     * @return a list of all rooms as {@link RoomDTO}; empty list if no rooms exist
     */
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(roomMapper::toDTO)
                .toList();
    }

    /**
     * Retrieves a single room by its unique identifier.
     *
     * @param roomId the primary key of the room to retrieve
     * @return the matching room as a {@link RoomDTO}
     * @throws ResourceNotFoundException if no room with the given ID exists
     */
    public RoomDTO getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", ROOM_ID_FIELD, roomId));
        return roomMapper.toDTO(room);
    }

    /**
     * Creates a new hotel room after validating the room number is unique.
     *
     * @param roomDTO the room data submitted by the admin
     * @return the persisted room as a {@link RoomDTO} (with the generated ID)
     * @throws APIException with {@code 400 BAD_REQUEST} if the room number already exists
     */
    @Transactional
    public RoomDTO createRoom(RoomDTO roomDTO) {
        log.info("Creating room with number: {}", roomDTO.getRoomNumber());
        if (roomRepository.existsByRoomNumber(roomDTO.getRoomNumber())) {
            log.warn("Room creation failed — room number already exists: {}", roomDTO.getRoomNumber());
            throw new APIException(HttpStatus.BAD_REQUEST,
                    "Room number '" + roomDTO.getRoomNumber() + "' already exists");
        }
        Room savedRoom = roomRepository.save(roomMapper.toEntity(roomDTO));
        log.info("Room created: id={}, number={}", savedRoom.getRoomId(), savedRoom.getRoomNumber());
        return roomMapper.toDTO(savedRoom);
    }

    /**
     * Updates an existing room's details.
     *
     * <p>Room number uniqueness is validated while excluding the room being updated —
     * this allows an admin to submit the same room number without triggering a conflict
     * (i.e. keeping the number unchanged is always valid).
     *
     * @param roomId  the primary key of the room to update
     * @param roomDTO the updated room data
     * @return the updated room as a {@link RoomDTO}
     * @throws ResourceNotFoundException if no room with the given ID exists
     * @throws APIException with {@code 400 BAD_REQUEST} if the new room number conflicts
     *                      with a different existing room
     */
    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomDTO roomDTO) {
        log.info("Updating room id={}", roomId);
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", ROOM_ID_FIELD, roomId));

        if (roomRepository.existsByRoomNumberAndRoomIdNot(roomDTO.getRoomNumber(), roomId)) {
            log.warn("Room update failed — number conflict: {}", roomDTO.getRoomNumber());
            throw new APIException(HttpStatus.BAD_REQUEST,
                    "Room number '" + roomDTO.getRoomNumber() + "' is already used by another room");
        }

        existingRoom.setRoomNumber(roomDTO.getRoomNumber());
        existingRoom.setRoomType(roomDTO.getRoomType());
        existingRoom.setPricePerNight(roomDTO.getPricePerNight());
        existingRoom.setCapacity(roomDTO.getCapacity());
        existingRoom.setDescription(roomDTO.getDescription());

        log.info("Room updated: id={}", roomId);
        return roomMapper.toDTO(roomRepository.save(existingRoom));
    }

    /**
     * Deletes a room from the system by its ID.
     *
     * @param roomId the primary key of the room to delete
     * @throws ResourceNotFoundException if no room with the given ID exists
     */
    @Transactional
    public void deleteRoom(Long roomId) {
        log.info("Deleting room id={}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", ROOM_ID_FIELD, roomId));
        roomRepository.delete(room);
        log.info("Room deleted: id={}", roomId);
    }
}
