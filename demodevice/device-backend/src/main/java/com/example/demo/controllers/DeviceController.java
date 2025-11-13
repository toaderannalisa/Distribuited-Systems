package com.example.demo.controllers;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.List;
import java.util.UUID;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.services.DeviceService;

@RestController
@RequestMapping("/devices")
@Validated
@Tag(name = "Device Management", description = "Endpoints for managing devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "Get all devices", description = "Returns all devices. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @Operation(summary = "Get device by ID", description = "Returns a device. Admin has full access; Clients can view only their own devices.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDetailsDTO.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(
            @Parameter(description = "Device UUID", required = true)
            @PathVariable UUID id,
            @Parameter(hidden = true) Authentication authentication) {

        DeviceDetailsDTO device = deviceService.findDeviceById(id);

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            if (device.getPersonId() == null) {
                return ResponseEntity.status(403).build();
            }
        }

        return ResponseEntity.ok(device);
    }

    @Operation(summary = "Create a new device", description = "Creates a new device entry. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @Parameter(description = "Device details", required = true)
            @Valid @RequestBody DeviceDetailsDTO device) {

        UUID id = deviceService.insert(device);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "Update device", description = "Updates a device by ID. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateDevice(
            @Parameter(description = "UUID of the device to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated device details", required = true)
            @Valid @RequestBody DeviceDetailsDTO device) {

        UUID updated = deviceService.updateById(id, device);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a device", description = "Deletes a device by ID. ADMIN ONLY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Admin only)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "UUID of the device to delete", required = true)
            @PathVariable UUID id) {

        deviceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get devices by person ID", description = "Returns a list of devices assigned to a person.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class))))
    })
    @GetMapping("/person/{personId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByPerson(
            @Parameter(description = "Person's UUID", required = true)
            @PathVariable UUID personId) {
        return ResponseEntity.ok(deviceService.findDevicesByPersonId(personId));
    }
}
