package com.example.springboot_education.repositories.assignment;

import com.example.springboot_education.entities.AssignmentComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentCommentRepository extends JpaRepository<AssignmentComment, Integer> {

    // Danh sách root comments của một assignment
    Page<AssignmentComment> findByAssignmentIdAndParentIsNullOrderByCreatedAtDesc(
            Integer assignmentId, Pageable pageable);

    Page<AssignmentComment> findByParentIdOrderByCreatedAtDesc(Integer parentId, Pageable pageable);

    int countByParentId(Integer parentId);

    int countByAssignmentIdAndParentIsNull(Integer assignmentId);

    // (Tuỳ chọn) lấy theo thread
    Page<AssignmentComment> findByAssignmentIdAndRootIdOrderByCreatedAtAsc(
            Integer assignmentId, Integer rootId, Pageable pageable);
}