package com.hotelreservation.room.repository;

import com.hotelreservation.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomNumber(String roomNumber);
    boolean existsByRoomNumberAndRoomIdNot(String roomNumber, Long roomId);
}
