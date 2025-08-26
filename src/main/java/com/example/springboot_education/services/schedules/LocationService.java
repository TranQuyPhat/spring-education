package com.example.springboot_education.services.schedules;


import com.example.springboot_education.dtos.classschedules.locations.*;
import com.example.springboot_education.entities.Location;
import com.example.springboot_education.repositories.schedules.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;


    public LocationDTO createLocation(CreateLocationRequest request) {
        if (locationRepository.existsByRoomName(request.getRoomName())) {
            throw new RuntimeException("Room name already exists");
        }

        Location location = Location.builder()
                .roomName(request.getRoomName())
                .description(request.getDescription())
                .build();

        return mapToDTO(locationRepository.save(location));
    }


    public LocationDTO updateLocation(Integer id, UpdateLocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        location.setRoomName(request.getRoomName());
        location.setDescription(request.getDescription());

        return mapToDTO(locationRepository.save(location));
    }

    public void deleteLocation(Integer id) {
        if (!locationRepository.existsById(id)) {
            throw new RuntimeException("Location not found");
        }
        locationRepository.deleteById(id);
    }


    public LocationDTO getLocationById(Integer id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        return mapToDTO(location);
    }


    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private LocationDTO mapToDTO(Location location) {
        return LocationDTO.builder()
                .id(location.getId())
                .roomName(location.getRoomName())
                .description(location.getDescription())
                .build();
    }
}
