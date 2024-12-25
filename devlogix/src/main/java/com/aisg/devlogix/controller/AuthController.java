package com.aisg.devlogix.controller;

import com.aisg.devlogix.model.AuthenticationRequest;
import com.aisg.devlogix.model.User;
import com.aisg.devlogix.repository.UserRepository;
import com.aisg.devlogix.service.CustomUserDetailsService;
import com.aisg.devlogix.util.JwtUtil;

import com.aisg.devlogix.exception.GlobalExceptionHandler;
import com.aisg.devlogix.exception.UserAlreadyExistsException;

import java.util.HashMap;
import java.util.Map;

import javax.smartcardio.ResponseAPDU;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public Map<String, String> loginUser(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String accessToken = jwtUtil.generateToken(userDetails);
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        Map<String, String> tokens = new HashMap<>();

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    @PostMapping("/refresh-token")
    public Map<String, String> refreshToken(@RequestHeader("Authorization") String refreshToken) throws Exception {
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new Exception("Invalid Refresh Token");
        }

        String username = jwtUtil.extractUsername(refreshToken);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String newAccessToken = jwtUtil.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();

        response.put("accessToken", newAccessToken);

        return response;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String refreshToken) {
        
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Invalid or expired refresh token");
        }
        
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test");
        return response;
    }
}
