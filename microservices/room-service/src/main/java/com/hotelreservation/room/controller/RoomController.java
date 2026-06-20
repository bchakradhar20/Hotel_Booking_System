package com.hotelreservation.room.controller;

import com.hotelreservation.room.dto.ApiResponse;
import com.hotelreservation.room.dto.RoomDTO;
import com.hotelreservation.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for hotel room management.
 *
 * <p>Follows the thin-controller principle: all business logic is delegated to
 * {@link RoomService}. This class handles only HTTP concerns:
 * request mapping, validation delegation, response wrapping, and HTTP status codes.
 *
 * <p>Access policy:
 * <ul>
 *   <li>GET endpoints — public, no authentication required.</li>
 *   <li>POST, PUT, DELETE endpoints — require {@code ROLE_ADMIN}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "Hotel room management — GET public, write operations require ADMIN")
public class RoomController {

    private final RoomService roomService;

    /**
     * Constructs the controller with the required room service.
     *
     * @param roomService service handling all room business logic
     */
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Retrieves all hotel rooms. Public endpoint — no authentication required.
     *
     * @return {@code 200 OK} with a list of all rooms; empty list if none exist
     */
    @GetMapping
    @Operation(summary = "Get all rooms (public)")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    /**
     * Retrieves a single room by its unique ID. Public endpoint.
     *
     * @param roomId the primary key of the room to retrieve
     * @return {@code 200 OK} with the room details, or {@code 404} if not found
     */
    @GetMapping("/{roomId}")
    @Operation(summary = "Get a room by ID (public)")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    /**
     * Creates a new hotel room. Requires {@code ROLE_ADMIN}.
     *
     * @param roomDTO the validated room data to persist
     * @return {@code 201 Created} with the new room (including generated ID)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a room (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(roomDTO));
    }

    /**
     * Updates an existing room's details. Requires {@code ROLE_ADMIN}.
     *
     * @param roomId  the primary key of the room to update
     * @param roomDTO the validated updated room data
     * @return {@code 200 OK} with the updated room
     */
    @PutMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a room (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long roomId,
                                               @Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, roomDTO));
    }

    /**
     * Deletes a room from the system. Requires {@code ROLE_ADMIN}.
     *
     * @param roomId the primary key of the room to delete
     * @return {@code 200 OK} with a success message
     */
    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a room (ADMIN only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok(new ApiResponse("Room deleted successfully", true));
    }
}
