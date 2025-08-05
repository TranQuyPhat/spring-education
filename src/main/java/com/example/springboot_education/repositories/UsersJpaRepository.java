package com.example.springboot_education.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.springboot_education.entities.Users;

@Repository
public interface UsersJpaRepository extends JpaRepository<Users, Integer> {

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  Optional<Users> findByEmail(String email);

  @Query("SELECT u FROM Users u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.email = :email")
  Optional<Users> findByEmailWithRoles(@Param("email") String email);

  @Query("SELECT u FROM Users u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.username = :username")
  Optional<Users> findByUsernameWithRoles(@Param("username") String username);

  Optional<Users> findById(Integer id);

  boolean existsById(Integer id);

  Users save(Users user);

  void deleteById(Integer id);

  @Query("SELECT DISTINCT u FROM Users u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role")
  List<Users> findAllUsersWithRoles();

  List<Users> findByIdIn(List<Integer> ids);
}