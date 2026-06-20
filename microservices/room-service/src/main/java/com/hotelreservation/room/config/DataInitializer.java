package com.hotelreservation.room.config;

import com.hotelreservation.room.entity.Room;
import com.hotelreservation.room.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application startup component that seeds the rooms table with default hotel rooms.
 *
 * <p>Implements {@link CommandLineRunner} so Spring Boot executes the {@link #run} method
 * automatically after the application context is fully loaded.
 *
 * <p>Pre-populates the database with a representative set of rooms across all supported
 * room types ({@code STANDARD}, {@code DELUXE}, {@code SUITE}, {@code FAMILY}) so the
 * application is immediately usable after a fresh start without requiring manual data entry.
 *
 * <p>Each room is only inserted if its room number does not already exist in the database.
 * This makes the seeding idempotent — safe to run on every application restart without
 * creating duplicate records.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;

    /**
     * Constructs the initializer with the room repository.
     *
     * @param roomRepository JPA repository for room persistence and existence checks
     */
    public DataInitializer(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Seeds the rooms table with default hotel rooms on application startup.
     *
     * <p>Room numbering convention:
     * <ul>
     *   <li>100s (101–103) — Standard rooms</li>
     *   <li>200s (201–203) — Deluxe rooms</li>
     *   <li>300s (301–303) — Suite rooms</li>
     *   <li>400s (401–402) — Family rooms</li>
     * </ul>
     *
     * <p>Each room is checked by room number before insertion to prevent duplicates
     * on repeated application restarts (idempotent seeding).
     *
     * @param args command-line arguments provided by Spring Boot (not used)
     */
    @Override
    public void run(String... args) {
        // Define the full set of seed rooms — covers all room types and price tiers
        List<Room> rooms = List.of(
                // --- Standard Rooms (Floor 1: rooms 101–103) ---
                new Room(null, "101", Room.RoomType.STANDARD, new BigDecimal("79.00"),  2, "Cozy standard room with queen bed, WiFi and city view"),
                new Room(null, "102", Room.RoomType.STANDARD, new BigDecimal("79.00"),  2, "Comfortable standard room with twin beds and garden view"),
                new Room(null, "103", Room.RoomType.STANDARD, new BigDecimal("89.00"),  3, "Spacious standard room with extra bed, ideal for families"),

                // --- Deluxe Rooms (Floor 2: rooms 201–203) ---
                new Room(null, "201", Room.RoomType.DELUXE,   new BigDecimal("139.00"), 2, "Deluxe room with king bed, mini bar and pool view"),
                new Room(null, "202", Room.RoomType.DELUXE,   new BigDecimal("149.00"), 2, "Deluxe room with premium bedding, bathtub and balcony"),
                new Room(null, "203", Room.RoomType.DELUXE,   new BigDecimal("159.00"), 4, "Deluxe family room with two queen beds and ocean view"),

                // --- Suite Rooms (Floor 3: rooms 301–303) ---
                new Room(null, "301", Room.RoomType.SUITE,    new BigDecimal("249.00"), 2, "Junior suite with separate living area, jacuzzi and panoramic view"),
                new Room(null, "302", Room.RoomType.SUITE,    new BigDecimal("299.00"), 2, "Executive suite with private lounge, king bed and butler service"),
                new Room(null, "303", Room.RoomType.SUITE,    new BigDecimal("399.00"), 4, "Presidential suite with two bedrooms, private pool and full kitchen"),

                // --- Family Rooms (Floor 4: rooms 401–402) ---
                new Room(null, "401", Room.RoomType.FAMILY,   new BigDecimal("299.00"), 6, "Large family room with three beds, kids corner and garden view"),
                new Room(null, "402", Room.RoomType.FAMILY,   new BigDecimal("319.00"), 6, "Spacious family suite with bunk beds, living room and kitchenette")
        );

        // Insert only rooms whose room numbers are not already in the database
        for (Room room : rooms) {
            if (!roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                roomRepository.save(room);
            }
        }
    }
}
