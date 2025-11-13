package com.example.demo.controllers;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.services.PersonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/people")
@Validated
@Tag(name = "Person Management", description = "Endpoints for managing people (Admin only)")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @Operation(summary = "Get all people",
            description = "Returns a list of all people. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersonDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @GetMapping
    public ResponseEntity<List<PersonDTO>> getPeople() {
        return ResponseEntity.ok(personService.findPersons());
    }

    @Operation(summary = "Get a person by ID",
            description = "Returns details of a single person. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonDetailsDTO.class))),

            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailsDTO> getPerson(
            @Parameter(description = "UUID of the person to retrieve", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(personService.findPersonById(id));
    }

    @Operation(summary = "Create a new person",
            description = "Creates a new person record. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Person successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @Parameter(description = "Details of the person to create", required = true)
            @Valid @RequestBody PersonDetailsDTO person) {

        UUID id = personService.insert(person);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "Update a person",
            description = "Updates the details of an existing person. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UUID> updatePerson(
            @Parameter(description = "UUID of the person to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated person details", required = true)
            @Valid @RequestBody PersonDetailsDTO person) {

        UUID updated = personService.updateById(id, person);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a person",
            description = "Deletes a person by ID. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(
            @Parameter(description = "UUID of the person to delete", required = true)
            @PathVariable UUID id) {

        personService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
