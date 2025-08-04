package com.example.springboot_education.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ClassUserId implements Serializable {
    private static final long serialVersionUID = 5119953932713976506L;
    @NotNull
    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @NotNull
    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ClassUserId entity = (ClassUserId) o;
        return Objects.equals(this.studentId, entity.studentId) &&
                Objects.equals(this.classId, entity.classId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, classId);
    }

}