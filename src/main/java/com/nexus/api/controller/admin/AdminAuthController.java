package com.nexus.api.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.api.dto.LoginRequestDTO;
import com.nexus.api.dto.LoginResponseDTO;
import com.nexus.shared.security.AdminJwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Auth", description = "Admin panel authentication")
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final AdminJwtUtil adminJwtUtil;

    public AdminAuthController(AuthenticationManager authenticationManager, AdminJwtUtil adminJwtUtil) {
        this.authenticationManager = authenticationManager;
        this.adminJwtUtil = adminJwtUtil;
    }

    @Operation(summary = "Admin login", description = "Authenticates with username/password and returns a JWT for subsequent admin API calls")
    @ApiResponse(responseCode = "200", description = "Login successful; JWT returned")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = adminJwtUtil.generateToken(auth.getName());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}

