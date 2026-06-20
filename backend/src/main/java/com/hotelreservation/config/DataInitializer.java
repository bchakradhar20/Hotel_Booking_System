package com.hotelreservation.config;

import com.hotelreservation.entity.Role;
import com.hotelreservation.entity.Room;
import com.hotelreservation.repository.RoleRepository;
import com.hotelreservation.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with required roles and default rooms on application startup.
 * All inserts are guarded with existence checks — safe to run on every restart.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;

    /**
     * Constructor injection for required repositories.
     *
     * @param roleRepository repository used to seed roles
     * @param roomRepository repository used to seed default rooms
     */
    public DataInitializer(RoleRepository roleRepository, RoomRepository roomRepository) {
        this.roleRepository = roleRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Seeds roles and default rooms if they do not already exist.
     * Runs automatically after the Spring context is fully loaded.
     *
     * @param args command-line arguments (not used)
     */
    @Override
    public void run(String... args) {
        seedRoles();
        seedRooms();
    }

    /**
     * Inserts ROLE_USER and ROLE_ADMIN if not already present.
     */
    private void seedRoles() {
        if (roleRepository.findByRoleName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_USER"));
        }
        if (roleRepository.findByRoleName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
        }
    }

    /**
     * Inserts 9 default rooms (3 STANDARD, 3 DELUXE, 3 SUITE) if they do not already exist.
     * Each room is checked by room number before inserting to avoid duplicates on restart.
     */
    private void seedRooms() {
        List<Room> defaultRooms = List.of(
                // STANDARD rooms — budget-friendly, suitable for solo or couple stays
                makeRoom("101", Room.RoomType.STANDARD, "79.00",  2, "Cozy standard room with queen bed, WiFi and city view"),
                makeRoom("102", Room.RoomType.STANDARD, "79.00",  2, "Comfortable standard room with twin beds and garden view"),
                makeRoom("103", Room.RoomType.STANDARD, "89.00",  3, "Spacious standard room with extra bed, ideal for families"),

                // DELUXE rooms — upgraded amenities and larger space
                makeRoom("201", Room.RoomType.DELUXE,   "139.00", 2, "Deluxe room with king bed, mini bar and pool view"),
                makeRoom("202", Room.RoomType.DELUXE,   "149.00", 2, "Deluxe room with premium bedding, bathtub and balcony"),
                makeRoom("203", Room.RoomType.DELUXE,   "159.00", 4, "Deluxe family room with two queen beds and ocean view"),

                // SUITE rooms — luxury experience with premium facilities
                makeRoom("301", Room.RoomType.SUITE,    "249.00", 2, "Junior suite with separate living area, jacuzzi and panoramic view"),
                makeRoom("302", Room.RoomType.SUITE,    "299.00", 2, "Executive suite with private lounge, king bed and butler service"),
                makeRoom("303", Room.RoomType.SUITE,    "399.00", 4, "Presidential suite with two bedrooms, private pool and full kitchen")
        );

        // Only insert rooms whose room number is not already in the database
        for (Room room : defaultRooms) {
            if (!roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                roomRepository.save(room);
            }
        }
    }

    /**
     * Helper to build a Room entity without an ID (for initial seeding).
     *
     * @param number      unique room number
     * @param type        room category (STANDARD, DELUXE, SUITE)
     * @param price       nightly rate as string (e.g., "99.00")
     * @param capacity    maximum number of guests
     * @param description brief room description
     * @return unsaved Room entity
     */
    private Room makeRoom(String number, Room.RoomType type, String price, int capacity, String description) {
        return new Room(null, number, type, new BigDecimal(price), capacity, description);
    }
}
