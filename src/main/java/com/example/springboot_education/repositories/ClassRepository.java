package com.example.springboot_education.repositories;

import com.example.springboot_education.entities.ClassEntity;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {
    List<ClassEntity> findByTeacher_Id(Integer teacherId);
    Page<ClassEntity> findByTeacher_Id(Integer teacherId, Pageable pageable);
    @Query("select c.slackChannelId from ClassEntity c where c.id = :classId")
    String findSlackChannelIdById(@Param("classId") Integer classId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT MAX(c.id) FROM ClassEntity c WHERE c.id >= :prefixStart")
    Integer findMaxIdByPrefixForUpdate(@Param("prefixStart") Integer prefixStart);
    List<ClassEntity> findByClassNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    List<ClassEntity> findTop10ByOrderByCreatedAtDesc();
}