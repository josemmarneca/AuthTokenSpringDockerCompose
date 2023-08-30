package com.example.app.services;

import com.example.app.payload.dtos.EmployeeDto;
import com.example.app.payload.dtos.UserDto;
import com.example.app.payload.requests.LoginRequest;
import com.example.app.payload.responses.JwtResponse;

import java.util.List;

public interface UserService {
    JwtResponse authenticateUser(LoginRequest loginRequest);

    UserDto registerUser(UserDto userDto);

}
