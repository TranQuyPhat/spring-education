package com.example.springboot_education.repositories.otp;

import com.example.springboot_education.entities.OtpVerification;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmailAndOtpAndIsUsedFalse(String email, String otp);

    List<OtpVerification> findByEmailOrderByCreatedAtDesc(String email);

    Optional<OtpVerification> findFirstByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);

    void deleteByEmailAndExpiresAtBefore(String email, LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}