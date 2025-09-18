package com.example.springboot_education.schedulers;

import com.example.springboot_education.repositories.assignment.AssignmentJpaRepository;
import com.example.springboot_education.repositories.assignment.SubmissionJpaRepository;
import com.example.springboot_education.repositories.classes.ClassesJpaRepository;
import com.example.springboot_education.services.mail.EmailService;
import com.example.springboot_education.entities.Assignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentReportScheduler {

    private final AssignmentJpaRepository assignmentRepo;
    private final SubmissionJpaRepository submissionRepo;
    private final ClassesJpaRepository classRepo;
    private final EmailService emailService;

    // chạy lúc ?h? mỗi ngày
    @Transactional
    @Scheduled(cron = "0 00 09 * * *")
    // @Scheduled(fixedRate = 60000) chạy 1 lần mỗi 1 phút
    public void sendDailyAssignmentReports() {
        log.info("🔔 Bắt đầu job gửi báo cáo bài tập hết hạn...");

        // lấy tất cả bài tập hết hạn hôm nay
        List<Assignment> assignments = assignmentRepo.findAssignmentsDueToday();

        for (Assignment assignment : assignments) {
            Integer classId = assignment.getClassField().getId();

            // danh sách học sinh đã nộp
            List<String> submissions = submissionRepo.findSubmittedStudentsByAssignment(assignment.getId());

            // thông tin giáo viên
            String teacherEmail = classRepo.findTeacherEmailByClassId(classId);
            String teacherName = classRepo.findTeacherNameByClassId(classId);

            if (teacherEmail == null) {
                log.warn("❌ Không tìm thấy email giáo viên cho lớp {}", classId);
                continue;
            }

            String html = emailService.buildAssignmentDueReportTemplate(
                    teacherName,
                    assignment.getClassField().getClassName(),
                    submissions);

            if (html != null) {
                emailService.sendEmail(teacherEmail, "Báo cáo bài tập hết hạn hôm nay", html);
                log.info("✅ Gửi báo cáo cho lớp {} tới {}", assignment.getClassField().getClassName(), teacherEmail);
            }
        }

        log.info("🎉 Job gửi báo cáo hoàn tất!");
    }
}
