package com.hotelreservation.service;

import com.hotelreservation.dto.RoomDTO;

import java.util.List;

/**
 * Service interface defining room management operations.
 */
public interface RoomService {

    /**
     * Retrieves all rooms available in the system.
     *
     * @return list of all rooms as DTOs
     */
    List<RoomDTO> getAllRooms();

    /**
     * Retrieves a single room by its unique identifier.
     *
     * @param roomId the ID of the room to retrieve
     * @return the room data as a DTO
     */
    RoomDTO getRoomById(Long roomId);

    /**
     * Creates a new room after validating that the room number is unique.
     *
     * @param roomDTO the room data to create
     * @return the created room as a DTO
     */
    RoomDTO createRoom(RoomDTO roomDTO);

    /**
     * Updates an existing room's details.
     *
     * @param roomId  the ID of the room to update
     * @param roomDTO the updated room data
     * @return the updated room as a DTO
     */
    RoomDTO updateRoom(Long roomId, RoomDTO roomDTO);

    /**
     * Deletes a room from the system by its ID.
     *
     * @param roomId the ID of the room to delete
     */
    void deleteRoom(Long roomId);
}
