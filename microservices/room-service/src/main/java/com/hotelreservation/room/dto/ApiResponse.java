package com.hotelreservation.room.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApiResponse {
    private String message;
    private boolean success;
}
