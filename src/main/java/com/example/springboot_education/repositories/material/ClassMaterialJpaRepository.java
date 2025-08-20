package com.example.springboot_education.repositories.material;

import com.example.springboot_education.entities.ClassMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassMaterialJpaRepository extends JpaRepository<ClassMaterial, Integer> {
    List<ClassMaterial> findByClassField_Id(Integer classId);
}
