package com.hotelreservation.auth.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserInfoResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private List<String> roles;
}
