package com.example.app.services.impl;

import com.example.app.database.entities.Role;
import com.example.app.database.entities.User;
import com.example.app.database.repositories.RoleRepository;
import com.example.app.database.repositories.UserRepository;
import com.example.app.enums.RoleEnum;
import com.example.app.payload.dtos.UserDto;
import com.example.app.payload.requests.LoginRequest;
import com.example.app.payload.responses.JwtResponse;
import com.example.app.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    PasswordEncoder encoder;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public UserDto registerUser(UserDto userDto) {

        // Create new user's account
        User user = new User(userDto.getUsername(),
                userDto.getEmail(),
                encoder.encode(userDto.getPassword()));
        Set<String> stringRoles = userDto.getRole();
        Set<Role> roles = new HashSet<>();

        if (stringRoles == null) {
            Role userRole = roleRepository.findByName(RoleEnum.USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            stringRoles.forEach(role -> {
                RoleEnum roleEnum = RoleEnum.valueOf(role.toUpperCase());
                if(Objects.isNull(roleEnum)){
                    throw new RuntimeException("Error: Role is not found.");
                }
                Role dbRole = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(dbRole);

            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return userDto;
    }


}