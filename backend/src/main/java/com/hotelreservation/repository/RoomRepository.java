package com.hotelreservation.repository;

import com.hotelreservation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Room entity database operations.
 * Extends JpaRepository to inherit standard CRUD operations.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Checks whether a room with the given room number already exists.
     * Used to enforce unique room number constraint during creation and update.
     *
     * @param roomNumber the room number to check
     * @return true if the room number is already in use; false otherwise
     */
    boolean existsByRoomNumber(String roomNumber);

    /**
     * Checks whether a room number is already taken by a different room.
     * Used during room update to allow keeping the same room number
     * while preventing conflicts with other rooms.
     *
     * @param roomNumber the room number to check
     * @param roomId     the ID of the room being updated (excluded from check)
     * @return true if another room has this number; false if it is free or belongs to this room
     */
    boolean existsByRoomNumberAndRoomIdNot(String roomNumber, Long roomId);
}
