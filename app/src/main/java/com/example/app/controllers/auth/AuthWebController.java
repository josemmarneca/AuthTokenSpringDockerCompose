package com.example.app.controllers.auth;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.app.database.entities.User;
import com.example.app.database.repositories.UserRepository;
import com.example.app.database.entities.RefreshToken;
import com.example.app.exception.TokenRefreshException;
import com.example.app.payload.dtos.UserDto;
import com.example.app.payload.requests.LoginRequest;
import com.example.app.payload.responses.JwtResponse;
import com.example.app.payload.responses.MessageResponse;
import com.example.app.security.jwt.JwtUtils;
import com.example.app.security.services.UserDetailsImpl;
import com.example.app.services.impl.RefreshTokenServiceImpl;
import com.example.app.services.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth/web")
public class AuthWebController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    private final UserServiceImpl userService;
    private final RefreshTokenServiceImpl refreshTokenService;

    public AuthWebController(UserServiceImpl userService, RefreshTokenServiceImpl refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto signUpRequest) {
        logger.info("Start Web signup  ");
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        this.userService.registerUser(signUpRequest);
        logger.info("Finish Web signup  ");
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        logger.info("Start Web signin  ");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        String jwt = jwtCookie.getValue();

        RefreshToken refreshToken = refreshTokenService.createWebRefreshToken(userDetails.getId());

        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        httpHeaders.add(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString());
        logger.info("Finish Web signin  ");
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(new JwtResponse(jwt));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        logger.info("Start Web signout  ");
        Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!Objects.equals(principle.toString(), "anonymousUser")) {
            Long userId = ((UserDetailsImpl) principle).getId();
            refreshTokenService.deleteByUserId(userId);
        }

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();
        logger.info("Finish Web signout  ");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
        logger.info("Start Web Refreshtoken");
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);

        if ((refreshToken != null) && (refreshToken.length() > 0)) {
            return refreshTokenService.findByToken(refreshToken).map(
                    refreshTokenDb -> {
                        this.refreshTokenService.verifyExpiration(refreshTokenDb);
                        User userDb = refreshTokenDb.getUser();
                        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDb);
                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                                .body(new MessageResponse("Token is refreshed successfully!"));
                    }
            ).orElseThrow(() -> new TokenRefreshException(refreshToken,
                    "Refresh token is not in database!"));
        }
        logger.info("Finish Web Refreshtoken");
        return ResponseEntity.badRequest().body(new MessageResponse("Refresh Token is empty!"));
    }


}
