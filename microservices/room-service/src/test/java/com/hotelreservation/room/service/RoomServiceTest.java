package com.hotelreservation.room.service;

import com.hotelreservation.room.dto.RoomDTO;
import com.hotelreservation.room.entity.Room;
import com.hotelreservation.room.exception.APIException;
import com.hotelreservation.room.exception.ResourceNotFoundException;
import com.hotelreservation.room.mapper.RoomMapper;
import com.hotelreservation.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RoomService}.
 * All dependencies are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)// manages mock life cycle
class RoomServiceTest {

    @Mock private RoomRepository roomRepository;
    @Mock private RoomMapper roomMapper;

    @InjectMocks
    private RoomService roomService;

    private Room room;
    private RoomDTO roomDTO;

    @BeforeEach
    void setUp() {
        room   = new Room(1L, "101", Room.RoomType.STANDARD, new BigDecimal("100.00"), 2, "Nice room");
        roomDTO = new RoomDTO(1L, "101", Room.RoomType.STANDARD, new BigDecimal("100.00"), 2, "Nice room");
    }

    // ── getAllRooms ────────────────────────────────────────────────────────────

    @Test
    void getAllRooms_returnsAllRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);

        List<RoomDTO> result = roomService.getAllRooms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomNumber()).isEqualTo("101");
        assertThat(result.get(0).getRoomType()).isEqualTo(Room.RoomType.STANDARD);
    }

    @Test
    void getAllRooms_returnsEmptyListWhenNoRooms() {
        when(roomRepository.findAll()).thenReturn(List.of());

        List<RoomDTO> result = roomService.getAllRooms();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllRooms_returnsMappedListOfMultipleRooms() {
        Room room2 = new Room(2L, "201", Room.RoomType.DELUXE, new BigDecimal("150.00"), 2, "Deluxe");
        RoomDTO dto2 = new RoomDTO(2L, "201", Room.RoomType.DELUXE, new BigDecimal("150.00"), 2, "Deluxe");
        when(roomRepository.findAll()).thenReturn(List.of(room, room2));
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);
        when(roomMapper.toDTO(room2)).thenReturn(dto2);

        List<RoomDTO> result = roomService.getAllRooms();

        assertThat(result).hasSize(2);
    }

    // ── getRoomById ───────────────────────────────────────────────────────────

    @Test
    void getRoomById_returnsMatchingRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);

        RoomDTO result = roomService.getRoomById(1L);

        assertThat(result.getRoomId()).isEqualTo(1L);
        assertThat(result.getRoomNumber()).isEqualTo("101");
    }

    @Test
    void getRoomById_throwsResourceNotFound_whenRoomMissing() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room")
                .hasMessageContaining("99");
    }

    // ── createRoom ────────────────────────────────────────────────────────────

    @Test
    void createRoom_success_persistsAndReturnsRoom() {
        when(roomRepository.existsByRoomNumber("101")).thenReturn(false);
        when(roomMapper.toEntity(roomDTO)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);

        RoomDTO result = roomService.createRoom(roomDTO);

        assertThat(result.getRoomNumber()).isEqualTo("101");
        assertThat(result.getPricePerNight()).isEqualByComparingTo("100.00");
        verify(roomRepository).save(room);
    }

    @Test
    void createRoom_throwsBadRequest_whenRoomNumberAlreadyExists() {
        when(roomRepository.existsByRoomNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(roomDTO))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("already exists")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(roomRepository, never()).save(any());
    }

    // ── updateRoom ────────────────────────────────────────────────────────────

    @Test
    void updateRoom_success_updatesAllFields() {
        RoomDTO updateRequest = new RoomDTO(1L, "101", Room.RoomType.DELUXE,
                new BigDecimal("150.00"), 3, "Updated description");
        Room updatedRoom = new Room(1L, "101", Room.RoomType.DELUXE,
                new BigDecimal("150.00"), 3, "Updated description");
        RoomDTO updatedDTO = new RoomDTO(1L, "101", Room.RoomType.DELUXE,
                new BigDecimal("150.00"), 3, "Updated description");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByRoomNumberAndRoomIdNot("101", 1L)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);
        when(roomMapper.toDTO(updatedRoom)).thenReturn(updatedDTO);

        RoomDTO result = roomService.updateRoom(1L, updateRequest);

        assertThat(result.getRoomType()).isEqualTo(Room.RoomType.DELUXE);
        assertThat(result.getPricePerNight()).isEqualByComparingTo("150.00");
        assertThat(result.getCapacity()).isEqualTo(3);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void updateRoom_success_allowsKeepingSameRoomNumber() {
        // Keeping the same room number must not trigger a conflict
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByRoomNumberAndRoomIdNot("101", 1L)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toDTO(room)).thenReturn(roomDTO);

        RoomDTO result = roomService.updateRoom(1L, roomDTO);

        assertThat(result.getRoomNumber()).isEqualTo("101");
    }

    @Test
    void updateRoom_throwsResourceNotFound_whenRoomMissing() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.updateRoom(99L, roomDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room");
    }

    @Test
    void updateRoom_throwsBadRequest_whenRoomNumberConflictsWithAnotherRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByRoomNumberAndRoomIdNot("101", 1L)).thenReturn(true);

        assertThatThrownBy(() -> roomService.updateRoom(1L, roomDTO))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("already used by another room")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(roomRepository, never()).save(any());
    }

    // ── deleteRoom ────────────────────────────────────────────────────────────

    @Test
    void deleteRoom_success_deletesRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.deleteRoom(1L);

        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_throwsResourceNotFound_whenRoomMissing() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.deleteRoom(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room")
                .hasMessageContaining("99");

        verify(roomRepository, never()).delete(any());
    }
}
