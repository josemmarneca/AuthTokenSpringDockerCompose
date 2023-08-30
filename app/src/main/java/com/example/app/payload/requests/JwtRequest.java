package com.example.app.payload.requests;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequest {
    private String type;
    private String refreshToken;
    private Long jwtRefreshExpirationMs ;


}

