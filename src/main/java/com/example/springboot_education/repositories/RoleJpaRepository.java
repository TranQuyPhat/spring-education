package com.example.springboot_education.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.springboot_education.entities.Role;

public interface RoleJpaRepository extends JpaRepository<Role, Integer> {
    // Role save(Role role);

    // Role findByName(String name);
    Optional<Role> findByName(String name);


    boolean existsByName(String name);

    Optional<Role> findById(Integer id);

    // List<Role> findAll();
    @Query("SELECT r FROM Role r")
    List<Role> findAllRoles();
}
