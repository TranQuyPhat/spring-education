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
public class ClassMaterialTargetId implements Serializable {
    private static final long serialVersionUID = -7723309324754160324L;
    @NotNull
    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @NotNull
    @Column(name = "material_id", nullable = false)
    private Integer materialId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ClassMaterialTargetId entity = (ClassMaterialTargetId) o;
        return Objects.equals(this.classId, entity.classId) &&
                Objects.equals(this.materialId, entity.materialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, materialId);
    }

}