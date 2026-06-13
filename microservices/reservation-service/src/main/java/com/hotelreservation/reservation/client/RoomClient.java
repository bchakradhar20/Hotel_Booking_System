package com.hotelreservation.reservation.client;

import com.hotelreservation.reservation.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service")
public interface RoomClient {
    @GetMapping("/api/rooms/{roomId}")
    RoomDTO getRoomById(@PathVariable Long roomId);
}
