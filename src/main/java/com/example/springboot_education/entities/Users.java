package com.example.springboot_education.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 50)
    @Column(length = 50)
    private String username;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false)
    private String password;

    @Size(max = 100)
    private String fullName;

    // @NotNull
    // @Size(max = 100)
    // @jakarta.validation.constraints.Email
    // @Column(nullable = false)
    @Size(max = 100)
@Column(nullable = false, unique = true)
    private String email;
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;
    @Lob
    @Column(name = "avatar", columnDefinition = "LONGBLOB")
    private byte[] avatar;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UserRole> userRoles = new ArrayList<>();

}