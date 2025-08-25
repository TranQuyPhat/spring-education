package com.example.springboot_education.repositories.schedules;

import com.example.springboot_education.entities.Location;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    Optional<Location> findByRoomName(String roomName);
    boolean existsByRoomName(String roomName);
}
