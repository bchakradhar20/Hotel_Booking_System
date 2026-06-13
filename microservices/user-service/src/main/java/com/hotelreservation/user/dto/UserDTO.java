package com.hotelreservation.user.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    @Email(message = "Invalid email format")
    private String email;
    private String phoneNumber;
}
