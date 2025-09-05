package com.example.springboot_education.controllers.schedules;



import com.example.springboot_education.dtos.classschedules.locations.*;
import com.example.springboot_education.services.schedules.LocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDTO> create(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.ok(locationService.createLocation(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> update(
            @PathVariable Integer id,
            @Valid
            @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAll() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
}

