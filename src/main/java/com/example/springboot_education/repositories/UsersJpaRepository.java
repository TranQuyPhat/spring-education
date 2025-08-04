package com.example.springboot_education.repositories;

import com.example.springboot_education.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersJpaRepository extends JpaRepository<Users, Integer> {
    
}
