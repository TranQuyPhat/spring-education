package com.example.springboot_education.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.springboot_education.entities.UserRole;
import com.example.springboot_education.entities.UserRoleId;

public interface UserRoleRepository  extends JpaRepository<UserRole, UserRoleId> {
      boolean existsById(UserRoleId userRoleId);

    // void deleteById(UserRoleId userRoleId);

    @Modifying
@Query("DELETE FROM UserRole ur WHERE ur.id.userId = :userId AND ur.id.roleId = :roleId")
void deleteByUserIdAndRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);


       @Modifying
@Query("DELETE FROM UserRole ur WHERE ur.id.userId = :userId")
void deleteByUserId(@Param("userId") Integer userId);

    // void saveAllUsersRoles(Iterable<UserRole> userRoles);

    // UserRole save(UserRole userRole);
}
