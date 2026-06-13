package com.hotelreservation.user.service;

import com.hotelreservation.user.dto.UserDTO;
import com.hotelreservation.user.entity.User;
import com.hotelreservation.user.exception.*;
import com.hotelreservation.user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public UserDTO getProfile(String username) {
        return toDTO(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username)));
    }

    @Transactional
    public UserDTO updateProfile(String username, UserDTO userDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail()))
                throw new APIException(HttpStatus.BAD_REQUEST, "Email is already in use by another account");
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        return toDTO(userRepository.save(user));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private UserDTO toDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
}
