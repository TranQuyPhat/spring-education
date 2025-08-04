package com.example.springboot_education.repositories;

import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassUserRepository extends JpaRepository<ClassUser, ClassUserId> {
    int countByClassField_Id(Integer classId);
}
