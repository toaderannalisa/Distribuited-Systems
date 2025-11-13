package com.example.demo.controllers;

import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.services.PersonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final PersonService personService;

    public AuthController(PersonService personService) {
        this.personService = personService;
    }

    @Operation(summary = "Register a new user",
            description = "Creates a new user. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Parameter(description = "Details of the user to register", required = true)
            @Valid @RequestBody PersonDetailsDTO person) {

        UUID id = personService.insert(person);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/../people/{id}")
                .buildAndExpand(id)
                .toUri();

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("username", person.getUsername());
        response.put("role", person.getRole());
        response.put("message", "User registered successfully");

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Login (Basic Auth)",
            description = "Validates Basic Auth credentials and returns a success message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Invalid credentials)")
    })
    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(hidden = true) Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("role", role);
        response.put("message", "Login successful");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user info",
            description = "Returns the details of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @Parameter(hidden = true) Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        PersonDetailsDTO person = personService.findPersonByUsername(username);

        Map<String, Object> response = new HashMap<>();
        response.put("id", person.getId());
        response.put("username", person.getUsername());
        response.put("name", person.getName());
        response.put("role", person.getRole());
        response.put("age", person.getAge());

        return ResponseEntity.ok(response);
    }
}
