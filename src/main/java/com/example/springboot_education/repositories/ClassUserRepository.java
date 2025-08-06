package com.example.springboot_education.repositories;

import com.example.springboot_education.entities.ClassUser;
import com.example.springboot_education.entities.ClassUserId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassUserRepository extends JpaRepository<ClassUser, ClassUserId> {
    int countByClassField_Id(Integer classId);
    List<ClassUser> findByClassField_Id(Integer classId);

    // Lấy danh sách class theo student
    List<ClassUser> findByStudent_Id(Integer studentId);

    // Kiểm tra xem student đã thuộc class chưa
    boolean existsByClassField_IdAndStudent_Id(Integer classId, Integer studentId);
}
