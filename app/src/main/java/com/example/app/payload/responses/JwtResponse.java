package com.example.app.payload.responses;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";

    private String refreshToken;

    private Date tokenExpired;


    public JwtResponse(String token, String refreshToken, Date tokenExpired) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenExpired = tokenExpired;
    }

    public JwtResponse(String token) {
        this.token = token;
    }
}

