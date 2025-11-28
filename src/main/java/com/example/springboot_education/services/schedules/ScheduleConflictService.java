package com.example.springboot_education.services.schedules;

import com.example.springboot_education.dtos.schedule.ScheduleConflictDTO;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.classes.ClassUserRepository;
import com.example.springboot_education.repositories.schedules.ClassScheduleSessionRepository;
import com.example.springboot_education.untils.RoleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleConflictService {
    private final ClassScheduleSessionRepository sessionRepository;
    private final ClassUserRepository classUserRepository;
    private final ClassRepository classRepository;

    /**
     * Kiểm tra xem một lớp học có trùng lịch với các lớp hiện tại của user không
     * 
     * @param user          User cần kiểm tra
     * @param targetClassId Class ID mà user muốn tham gia/tìm kiếm
     * @return ScheduleConflictDTO chứa thông tin chi tiết về các xung đột lịch
     */
    public ScheduleConflictDTO checkScheduleConflict(Users user, Integer targetClassId) {
        log.info("🔍 Checking schedule conflict for user {} and class {}", user.getId(), targetClassId);

        // Step 1: Lấy danh sách lớp hiện tại của user
        List<ClassEntity> currentClasses = getUserCurrentClasses(user);
        log.debug("👤 User has {} current class(es)", currentClasses.size());

        // Step 2: Lấy thông tin lớp đích
        ClassEntity targetClass = classRepository.findById(targetClassId)
                .orElseThrow(() -> new RuntimeException("Target class not found: " + targetClassId));
        log.debug("🎯 Target class: {}", targetClass.getClassName());

        // Step 3: Lấy danh sách sessions của user (từ các lớp hiện tại)
        List<ClassScheduleSession> userSessions = currentClasses.stream()
                .flatMap(c -> sessionRepository.findByClassEntity_Id(c.getId()).stream())
                .collect(Collectors.toList());
        log.debug("📅 User has {} total sessions", userSessions.size());

        // Step 4: Lấy danh sách sessions của lớp đích
        List<ClassScheduleSession> targetSessions = sessionRepository.findByClassEntity_Id(targetClassId);
        log.debug("📅 Target class has {} total sessions", targetSessions.size());

        // Step 5: Tìm các sessions trùng lịch
        List<ScheduleConflictDTO.ConflictDetail> conflicts = findConflictingSessions(userSessions, targetSessions);

        log.info("⚠️  Found {} conflicting session(s)", conflicts.size());

        boolean hasConflict = !conflicts.isEmpty();
        return ScheduleConflictDTO.builder()
                .hasConflict(hasConflict)
                .targetClassId(targetClassId)
                .targetClassName(targetClass.getClassName())
                .conflictCount(conflicts.size())
                .conflicts(conflicts)
                .message(hasConflict
                        ? String.format("Lớp %s có %d buổi học trùng lịch", targetClass.getClassName(),
                                conflicts.size())
                        : "Không có xung đột lịch học")
                .build();
    }

    /**
     * Kiểm tra xem một student có thể join vào lớp không (strict check)
     * Nếu có trùng lịch → reject
     * 
     * @param studentId     Student ID
     * @param targetClassId Class ID muốn join
     * @return true nếu có trùng lịch (không được phép join)
     */
    public boolean cannotJoinClass(Integer studentId, Integer targetClassId) {
        Users student = new Users();
        student.setId(studentId);

        ScheduleConflictDTO conflict = checkScheduleConflict(student, targetClassId);
        return conflict.isHasConflict();
    }

    /**
     * Lấy danh sách lớp hiện tại của user dựa trên role
     * - TEACHER: Tất cả lớp mà giáo viên dạy
     * - STUDENT: Tất cả lớp mà học sinh đang tham gia
     */
    private List<ClassEntity> getUserCurrentClasses(Users user) {
        boolean isTeacher = RoleUtil.isTeacher(user);
        boolean isStudent = RoleUtil.isStudent(user);

        if (isTeacher) {
            return classRepository.findByTeacher_Id(user.getId());
        } else if (isStudent) {
            List<ClassUser> classUsers = classUserRepository.findByStudent_Id(user.getId());
            return classUsers.stream()
                    .map(ClassUser::getClassField)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * Tìm các sessions từ 2 danh sách session mà có xung đột lịch
     * 
     * Xung đột xảy ra khi:
     * - Cùng ngày (DayOfWeek)
     * - Cùng tuần (nếu cùng ngày trong tuần pattern)
     * - Cùng khoảng tiết học (overlapping periods)
     * 
     * @param userSessions   Sessions của các lớp hiện tại của user
     * @param targetSessions Sessions của lớp đích
     * @return Danh sách các xung đột
     */
    private List<ScheduleConflictDTO.ConflictDetail> findConflictingSessions(
            List<ClassScheduleSession> userSessions,
            List<ClassScheduleSession> targetSessions) {

        List<ScheduleConflictDTO.ConflictDetail> conflicts = new ArrayList<>();

        // Nhóm user sessions theo (ngày trong tuần, khoảng tiết)
        Map<String, List<ClassScheduleSession>> userSessionsByDayAndPeriod = groupSessionsByDayAndPeriod(userSessions);

        // Với mỗi session của lớp đích
        for (ClassScheduleSession targetSession : targetSessions) {
            String targetKey = getSessionKey(targetSession);
            log.debug("  🔎 Checking target session: {} - {}", targetSession.getSessionDate(), targetKey);

            // Tìm các user sessions có cùng ngày và tiết
            List<ClassScheduleSession> potentialConflicts = userSessionsByDayAndPeriod.getOrDefault(targetKey,
                    new ArrayList<>());

            for (ClassScheduleSession userSession : potentialConflicts) {
                // Kiểm tra thêm overlap periods
                if (isPeriodsOverlap(userSession.getStartPeriod(), userSession.getEndPeriod(),
                        targetSession.getStartPeriod(), targetSession.getEndPeriod())) {

                    log.warn("⚠️  Conflict found: {} vs {}",
                            userSession.getSessionDate() + " " + userSession.getStartPeriod() + "-"
                                    + userSession.getEndPeriod(),
                            targetSession.getSessionDate() + " " + targetSession.getStartPeriod() + "-"
                                    + targetSession.getEndPeriod());

                    conflicts.add(ScheduleConflictDTO.ConflictDetail.builder()
                            .userSessionId(userSession.getId())
                            .userSessionDate(userSession.getSessionDate())
                            .userStartPeriod(userSession.getStartPeriod())
                            .userEndPeriod(userSession.getEndPeriod())
                            .userClassName(userSession.getClassEntity().getClassName())
                            .targetSessionId(targetSession.getId())
                            .targetSessionDate(targetSession.getSessionDate())
                            .targetStartPeriod(targetSession.getStartPeriod())
                            .targetEndPeriod(targetSession.getEndPeriod())
                            .conflictReason("Cùng ngày (" + targetSession.getSessionDate().getDayOfWeek().name()
                                    + ") và cùng tiết học (" + targetSession.getStartPeriod() + "-"
                                    + targetSession.getEndPeriod() + ")")
                            .build());
                }
            }
        }

        return conflicts;
    }

    /**
     * Nhóm sessions theo (ngày trong tuần + khoảng tiết)
     * Key format: "MONDAY_1_3" (ngày_tiết bắt đầu_tiết kết thúc)
     */
    private Map<String, List<ClassScheduleSession>> groupSessionsByDayAndPeriod(List<ClassScheduleSession> sessions) {
        Map<String, List<ClassScheduleSession>> grouped = new HashMap<>();

        for (ClassScheduleSession session : sessions) {
            String key = getSessionKey(session);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(session);
        }

        return grouped;
    }

    /**
     * Tạo key để nhóm sessions
     * Format: "2025-11-28_MONDAY_1_3" (date_DayOfWeek_startPeriod_endPeriod)
     * 
     * Lưu ý: Thêm date vào để chỉ so sánh sessions cùng ngày, không phải cùng ngày
     * trong tuần
     */
    private String getSessionKey(ClassScheduleSession session) {
        DayOfWeek dayOfWeek = session.getSessionDate().getDayOfWeek();
        return String.format("%s_%s_%d_%d",
                session.getSessionDate(), // Thêm ngày cụ thể để phân biệt các tuần khác nhau
                dayOfWeek.name(),
                session.getStartPeriod(),
                session.getEndPeriod());
    }

    /**
     * Kiểm tra xem hai khoảng thời gian (tiết) có overlap không
     * 
     * @param start1 Tiết bắt đầu khoảng 1
     * @param end1   Tiết kết thúc khoảng 1
     * @param start2 Tiết bắt đầu khoảng 2
     * @param end2   Tiết kết thúc khoảng 2
     * @return true nếu có overlap
     */
    private boolean isPeriodsOverlap(Integer start1, Integer end1, Integer start2, Integer end2) {
        // Hai khoảng [start1, end1] và [start2, end2] overlap nếu:
        // start1 <= end2 AND start2 <= end1
        return start1 <= end2 && start2 <= end1;
    }
}
