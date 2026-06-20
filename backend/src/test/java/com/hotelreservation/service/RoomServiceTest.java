package com.hotelreservation.service;

import com.hotelreservation.dto.RoomDTO;
import com.hotelreservation.entity.Room;
import com.hotelreservation.exception.*;
import com.hotelreservation.mapper.RoomMapper;
import com.hotelreservation.repository.RoomRepository;
import com.hotelreservation.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoomServiceImpl covering CRUD operations and validation scenarios.
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private RoomRepository roomRepository;
    @Mock private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room room;
    private RoomDTO roomDTO;

    @BeforeEach
    void setUp() {
        room = new Room(1L, "101", Room.RoomType.STANDARD, new BigDecimal("100.00"), 2, "Standard room");
        roomDTO = new RoomDTO(1L, "101", Room.RoomType.STANDARD, new BigDecimal("100.00"), 2, "Standard room");
    }

    /**
     * Tests successful retrieval of all rooms.
     */
    @Test
    void getAllRooms_ReturnsAllRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);

        List<RoomDTO> result = roomService.getAllRooms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomNumber()).isEqualTo("101");
    }

    /**
     * Tests successful retrieval of a room by ID.
     */
    @Test
    void getRoomById_ExistingRoom_ReturnsRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);

        RoomDTO result = roomService.getRoomById(1L);

        assertThat(result.getRoomId()).isEqualTo(1L);
        assertThat(result.getRoomNumber()).isEqualTo("101");
    }

    /**
     * Tests that getRoomById throws ResourceNotFoundException for non-existent room.
     */
    @Test
    void getRoomById_NonExistentRoom_ThrowsException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room");
    }

    /**
     * Tests successful room creation with a unique room number.
     */
    @Test
    void createRoom_UniqueRoomNumber_ReturnsCreatedRoom() {
        when(roomRepository.existsByRoomNumber("101")).thenReturn(false);
        when(roomMapper.toEntity(roomDTO)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);

        RoomDTO result = roomService.createRoom(roomDTO);

        assertThat(result.getRoomNumber()).isEqualTo("101");
        verify(roomRepository).save(room);
    }

    /**
     * Tests that createRoom throws APIException when room number already exists.
     */
    @Test
    void createRoom_DuplicateRoomNumber_ThrowsException() {
        when(roomRepository.existsByRoomNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(roomDTO))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("already exists");
    }

    /**
     * Tests successful room deletion for an existing room.
     */
    @Test
    void deleteRoom_ExistingRoom_DeletesSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThatCode(() -> roomService.deleteRoom(1L)).doesNotThrowAnyException();
        verify(roomRepository).delete(room);
    }

    /**
     * Tests that deleteRoom throws ResourceNotFoundException for a non-existent room.
     */
    @Test
    void deleteRoom_NonExistentRoom_ThrowsException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.deleteRoom(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
