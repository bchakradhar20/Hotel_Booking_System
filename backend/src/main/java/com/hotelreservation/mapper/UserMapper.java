package com.hotelreservation.mapper;

import com.hotelreservation.dto.UserDTO;
import com.hotelreservation.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting between User entity and UserDTO.
 * Handles explicit field mappings that ModelMapper cannot automatically resolve.
 */
@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    /**
     * Constructor injection for ModelMapper dependency.
     *
     * @param modelMapper the shared ModelMapper bean
     */
    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts a User entity to a UserDTO.
     * Password and roles are intentionally excluded — the DTO only exposes safe profile fields.
     *
     * @param user the User entity to convert
     * @return the mapped UserDTO containing safe profile fields
     */
    public UserDTO toDTO(User user) {
        // ModelMapper maps matching field names automatically (userId, username, email, phoneNumber)
        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Converts a UserDTO to a User entity.
     * Only updates the mutable profile fields (email, phoneNumber).
     * Username, password, and roles are never set from this mapper.
     *
     * @param userDTO the UserDTO to convert
     * @return the mapped User entity with profile fields only
     */
    public User toEntity(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}
