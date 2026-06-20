package com.hotelreservation.controller;

import com.hotelreservation.dto.*;
import com.hotelreservation.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for hotel room management.
 * GET endpoints are public. POST/PUT/DELETE require ADMIN role.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    /**
     * Constructor injection for RoomService.
     *
     * @param roomService service handling room business logic
     */
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Retrieves all available hotel rooms. Public endpoint.
     *
     * @return 200 OK with list of all rooms
     */
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    /**
     * Retrieves a single room by its ID. Public endpoint.
     *
     * @param roomId the room's unique identifier
     * @return 200 OK with room details, or 404 if not found
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    /**
     * Creates a new hotel room. Requires ADMIN role.
     *
     * @param roomDTO the room data for creation
     * @return 201 Created with the newly created room
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(roomDTO));
    }

    /**
     * Updates an existing room's details. Requires ADMIN role.
     *
     * @param roomId  the ID of the room to update
     * @param roomDTO the updated room data
     * @return 200 OK with the updated room
     */
    @PutMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long roomId, @Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, roomDTO));
    }

    /**
     * Deletes a room from the system. Requires ADMIN role.
     *
     * @param roomId the ID of the room to delete
     * @return 200 OK with success message
     */
    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok(new ApiResponse("Room deleted successfully", true));
    }
}
