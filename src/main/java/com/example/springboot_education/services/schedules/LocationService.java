package com.example.springboot_education.services.schedules;

import com.example.springboot_education.dtos.classschedules.locations.*;
import com.example.springboot_education.entities.Location;
import com.example.springboot_education.exceptions.EntityNotFoundException;
import com.example.springboot_education.exceptions.HttpException;
import com.example.springboot_education.repositories.schedules.ClassSchedulePatternRepository;
import com.example.springboot_education.repositories.schedules.LocationRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final ClassSchedulePatternRepository classSchedulePatternRepository;

    public LocationDTO createLocation(CreateLocationRequest request) {
        if (locationRepository.existsByRoomName(request.getRoomName())) {
            throw new HttpException("Room name already exists", HttpStatus.CONFLICT);
        }

        Location location = Location.builder()
                .roomName(request.getRoomName())
                .description(request.getDescription())
                .build();

        return mapToDTO(locationRepository.save(location));
    }

    public LocationDTO updateLocation(Integer id, UpdateLocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location"));

        // tìm xem roomName có tồn tại không
        Optional<Location> existing = locationRepository.findByRoomName(request.getRoomName());

        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new HttpException("Room name already exists", HttpStatus.CONFLICT);
        }

        location.setRoomName(request.getRoomName());
        location.setDescription(request.getDescription());

        return mapToDTO(locationRepository.save(location));
    }

   public void deleteLocation(Integer id) {
    Location location = locationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Location not found with id " + id));

    boolean inUse = classSchedulePatternRepository.existsByLocationId(id);
    if (inUse) {
        throw new HttpException("Cannot delete location because it is being used in schedule patterns", HttpStatus.CONFLICT);
    }

    locationRepository.delete(location);
}

    public LocationDTO getLocationById(Integer id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location"));
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
