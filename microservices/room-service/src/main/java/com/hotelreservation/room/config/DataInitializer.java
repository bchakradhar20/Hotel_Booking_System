package com.hotelreservation.room.config;

import com.hotelreservation.room.entity.Room;
import com.hotelreservation.room.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoomRepository roomRepository;

    public DataInitializer(RoomRepository roomRepository) { this.roomRepository = roomRepository; }

    @Override
    public void run(String... args) {
        List<Room> rooms = List.of(
                new Room(null, "101", Room.RoomType.STANDARD, new BigDecimal("79.00"),  2, "Cozy standard room with queen bed, WiFi and city view"),
                new Room(null, "102", Room.RoomType.STANDARD, new BigDecimal("79.00"),  2, "Comfortable standard room with twin beds and garden view"),
                new Room(null, "103", Room.RoomType.STANDARD, new BigDecimal("89.00"),  3, "Spacious standard room with extra bed, ideal for families"),
                new Room(null, "201", Room.RoomType.DELUXE,   new BigDecimal("139.00"), 2, "Deluxe room with king bed, mini bar and pool view"),
                new Room(null, "202", Room.RoomType.DELUXE,   new BigDecimal("149.00"), 2, "Deluxe room with premium bedding, bathtub and balcony"),
                new Room(null, "203", Room.RoomType.DELUXE,   new BigDecimal("159.00"), 4, "Deluxe family room with two queen beds and ocean view"),
                new Room(null, "301", Room.RoomType.SUITE,    new BigDecimal("249.00"), 2, "Junior suite with separate living area, jacuzzi and panoramic view"),
                new Room(null, "302", Room.RoomType.SUITE,    new BigDecimal("299.00"), 2, "Executive suite with private lounge, king bed and butler service"),
                new Room(null, "303", Room.RoomType.SUITE,    new BigDecimal("399.00"), 4, "Presidential suite with two bedrooms, private pool and full kitchen"),
                new Room(null, "401", Room.RoomType.FAMILY,   new BigDecimal("299.00"), 6, "Large family room with three beds, kids corner and garden view"),
                new Room(null, "402", Room.RoomType.FAMILY,   new BigDecimal("319.00"), 6, "Spacious family suite with bunk beds, living room and kitchenette")
        );
        for (Room room : rooms) {
            if (!roomRepository.existsByRoomNumber(room.getRoomNumber()))
                roomRepository.save(room);
        }
    }
}
