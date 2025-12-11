package com.example.demo.controllers;

import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.services.PersonService;
import com.example.demo.entities.Person;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(PersonService personService, PasswordEncoder passwordEncoder) {
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
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

    @Operation(summary = "Login",
            description = "Validates username and password, returns user info if successful.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Invalid credentials)")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        LOGGER.info("Login attempt for username: {}", username);

        if (username == null || password == null) {
            LOGGER.warn("Username or password is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            PersonDetailsDTO person = personService.findPersonByUsername(username);
            
            String dbPassword = person.getPassword();
            LOGGER.info("Found user: {}, password from DB length: {}, hash prefix: {}", username, 
                dbPassword != null ? dbPassword.length() : 0,
                dbPassword != null && dbPassword.length() > 10 ? dbPassword.substring(0, 10) : dbPassword);
            
            // Verify password using BCrypt
            boolean matches = false;
            if (dbPassword != null && dbPassword.startsWith("$2")) {
                matches = passwordEncoder.matches(password, dbPassword);
            } else {
                // Plaintext password comparison (TEMPORARY DEBUG ONLY)
                matches = password.equals(dbPassword);
                LOGGER.warn("Using plaintext password comparison - INSECURE!");
            }
            LOGGER.info("Password matches: {}, input password: {}", matches, password);
            
            if (!matches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", person.getId());
            response.put("username", person.getUsername());
            response.put("name", person.getName());
            response.put("role", person.getRole());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("Login failed for user: {}, error: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Get current user info",
            description = "Returns the details of a user by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
