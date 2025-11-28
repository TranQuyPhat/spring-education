package com.example.springboot_education.services.schedules;

import com.example.springboot_education.dtos.schedule.DayScheduleDTO;
import com.example.springboot_education.dtos.schedule.ScheduleSessionDTO;
import com.example.springboot_education.dtos.schedule.UserScheduleDTO;
import com.example.springboot_education.entities.*;
import com.example.springboot_education.repositories.ClassRepository;
import com.example.springboot_education.repositories.classes.ClassUserRepository;
import com.example.springboot_education.repositories.schedules.ClassScheduleSessionRepository;
import com.example.springboot_education.untils.RoleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j

@Service
@RequiredArgsConstructor
public class UserScheduleService {
    private final ClassScheduleSessionRepository sessionRepository;
    private final ClassUserRepository classUserRepository;
    private final ClassRepository classRepository;

    /**
     * Lấy thời khoá biểu của user cho một tuần cụ thể
     * 
     * Flow:
     * 1. Xác định tuần (weekStart: Thứ 2, weekEnd: Chủ nhật)
     * 2. Lấy tất cả lớp học của user dựa trên role:
     * - TEACHER: Tất cả lớp mà giáo viên dạy
     * - STUDENT: Tất cả lớp mà học sinh tham gia (qua ClassUser)
     * 3. Lặp qua từng lớp → fetch tất cả sessions của lớp đó
     * 4. Filter sessions nằm trong tuần được query
     * 5. Gộp tất cả sessions từ nhiều lớp, nhóm theo ngày trong tuần
     * 6. Trả về schedule đầy đủ 7 ngày (kể cả ngày không có lớp)
     * 
     * @param user     User hiện tại (teacher hoặc student)
     * @param weekDate Ngày bất kỳ trong tuần cần lấy (để xác định tuần)
     * @return UserScheduleDTO chứa thông tin lịch của tuần từ tất cả lớp
     */
    public UserScheduleDTO getScheduleByWeek(Users user, LocalDate weekDate) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        log.info("📅 Fetching schedule for user ID: {} on date: {}", user.getId(), weekDate);

        // Tính toán ngày đầu tuần (Thứ 2) và cuối tuần (Chủ nhật)
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate weekStart = weekDate.with(weekFields.dayOfWeek(), 1); // Thứ 2
        LocalDate weekEnd = weekStart.plusDays(6); // Chủ nhật

        log.debug("📍 Week range: {} → {}", weekStart, weekEnd);

        // Step 1: Lấy danh sách lớp học dựa trên role
        List<ClassEntity> classes = getClassesByUserRole(user);
        log.info("📚 User has {} class(es): {}",
                classes.size(),
                classes.stream().map(c -> c.getId() + ":" + c.getClassName()).collect(Collectors.joining(", ")));

        // Step 2: Lấy tất cả session của tất cả lớp trong tuần
        List<ClassScheduleSession> sessions = new ArrayList<>();
        for (ClassEntity classEntity : classes) {
            List<ClassScheduleSession> classSessions = sessionRepository.findByClassEntity_Id(classEntity.getId());

            log.debug("  📖 Class {} has {} session(s) total", classEntity.getClassName(), classSessions.size());

            // Filter sessions nằm trong tuần được query
            List<ClassScheduleSession> filteredSessions = classSessions.stream()
                    .filter(s -> !s.getSessionDate().isBefore(weekStart) && !s.getSessionDate().isAfter(weekEnd))
                    .collect(Collectors.toList());

            log.debug("    ✅ {} session(s) in week {}", filteredSessions.size(), weekStart.getYear());
            sessions.addAll(filteredSessions);
        }

        log.info("🎯 Total sessions for week: {}", sessions.size());

        // Step 3: Nhóm session theo ngày trong tuần
        Map<Integer, List<ScheduleSessionDTO>> sessionsByDay = groupSessionsByDay(sessions);

        // Step 4: Tạo DayScheduleDTO cho mỗi ngày trong tuần
        List<DayScheduleDTO> daySchedules = new ArrayList<>();
        String[] dayNames = { "", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        for (int day = 1; day <= 7; day++) {
            LocalDate dayDate = weekStart.plusDays(day - 1);
            List<ScheduleSessionDTO> daySessions = sessionsByDay.getOrDefault(day, new ArrayList<>());

            // Sắp xếp theo start period
            daySessions.sort(Comparator.comparing(ScheduleSessionDTO::getStartPeriod));

            log.debug("  📅 {} ({}) → {} lesson(s)", dayNames[day], dayDate, daySessions.size());

            DayScheduleDTO daySchedule = DayScheduleDTO.builder()
                    .day(dayNames[day])
                    .date(dayDate)
                    .lessons(daySessions)
                    .build();
            daySchedules.add(daySchedule);
        }

        // Tính tuần thứ bao nhiêu trong năm
        int weekNumber = weekDate.get(weekFields.weekOfYear());

        // Tính ngày đầu tuần trước và tuần sau (để FE navigate dễ hơn)
        LocalDate previousWeekStart = weekStart.minusWeeks(1);
        LocalDate nextWeekStart = weekStart.plusWeeks(1);

        log.info("✨ Schedule ready - Week #{} ({} sessions)", weekNumber, sessions.size());

        return UserScheduleDTO.builder()
                .weekNumber(weekNumber)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .previousWeekStartDate(previousWeekStart)
                .nextWeekStartDate(nextWeekStart)
                .schedules(daySchedules)
                .build();
    }

    /**
     * Lấy thời khoá biểu của user cho tuần hiện tại
     */
    public UserScheduleDTO getCurrentWeekSchedule(Users user) {
        return getScheduleByWeek(user, LocalDate.now());
    }

    /**
     * Lấy thời khoá biểu của user cho tuần tiếp theo
     */
    public UserScheduleDTO getNextWeekSchedule(Users user) {
        return getScheduleByWeek(user, LocalDate.now().plusWeeks(1));
    }

    /**
     * Lấy thời khoá biểu của user cho tuần trước
     */
    public UserScheduleDTO getPreviousWeekSchedule(Users user) {
        return getScheduleByWeek(user, LocalDate.now().minusWeeks(1));
    }

    /**
     * Lấy thời khoá biểu của user dựa vào offset tuần
     * (Để support legacy endpoints nếu cần)
     * 
     * @param user       User hiện tại
     * @param weekOffset Số tuần offset từ hiện tại (0=hiện tại, 1=tuần sau, -1=tuần
     *                   trước)
     * @return UserScheduleDTO chứa thông tin lịch của tuần
     */
    public UserScheduleDTO getScheduleByWeekOffset(Users user, int weekOffset) {
        return getScheduleByWeek(user, LocalDate.now().plusWeeks(weekOffset));
    }

    /**
     * Nhóm các session theo ngày trong tuần (1-7: Mon-Sun)
     * 
     * Java DayOfWeek enum: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
     * Chúng ta sử dụng đúng giá trị này để map sessions
     * 
     * @param sessions Danh sách tất cả sessions cần nhóm
     * @return Map<Day, List<Sessions>> - sessions được nhóm theo ngày (1-7)
     */
    private Map<Integer, List<ScheduleSessionDTO>> groupSessionsByDay(List<ClassScheduleSession> sessions) {
        Map<Integer, List<ScheduleSessionDTO>> result = new HashMap<>();

        for (ClassScheduleSession session : sessions) {
            // DayOfWeek.getValue() trả về: MONDAY=1, ..., SUNDAY=7
            int dayOfWeek = session.getSessionDate().getDayOfWeek().getValue();

            log.debug("    📍 Session {} on {} (day={})",
                    session.getId(),
                    session.getSessionDate(),
                    dayOfWeek);

            ScheduleSessionDTO dto = mapSessionToDTO(session);
            result.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(dto);
        }

        return result;
    }

    /**
     * Lấy danh sách lớp học dựa trên role của user
     * 
     * - TEACHER: Tất cả lớp mà giáo viên dạy (teacher_id = current user)
     * - STUDENT: Tất cả lớp mà học sinh tham gia (qua bảng class_user)
     * 
     * @param user User cần lấy lịch
     * @return Danh sách ClassEntity mà user có liên quan
     */
    private List<ClassEntity> getClassesByUserRole(Users user) {
        boolean isTeacher = RoleUtil.isTeacher(user);
        boolean isStudent = RoleUtil.isStudent(user);

        log.debug("  👤 User role - Teacher: {}, Student: {}", isTeacher, isStudent);

        if (isTeacher) {
            // Giáo viên: lấy các lớp mà họ dạy
            List<ClassEntity> teacherClasses = classRepository.findByTeacher_Id(user.getId());
            log.debug("  👨‍🏫 Teacher classes found: {}", teacherClasses.size());
            return teacherClasses;
        } else if (isStudent) {
            // Học sinh: lấy các lớp mà họ đang tham gia (qua ClassUser)
            List<ClassUser> classUsers = classUserRepository.findByStudent_Id(user.getId());
            log.debug("  👨‍🎓 Student enrolled in {} class(es)", classUsers.size());

            List<ClassEntity> studentClasses = classUsers.stream()
                    .map(ClassUser::getClassField)
                    .collect(Collectors.toList());

            return studentClasses;
        }

        log.warn("⚠️  User {} has no recognized role (not TEACHER or STUDENT)", user.getId());
        return new ArrayList<>();
    }

    /**
     * Map ClassScheduleSession entity sang ScheduleSessionDTO
     * 
     * @param session Entity từ database
     * @return DTO để trả về cho FE
     */
    private ScheduleSessionDTO mapSessionToDTO(ClassScheduleSession session) {
        ClassEntity classEntity = session.getClassEntity();

        return ScheduleSessionDTO.builder()
                .sessionId(session.getId())
                .className(classEntity.getClassName())
                .subjectName(classEntity.getSubject() != null ? classEntity.getSubject().getSubjectName() : "N/A")
                .teacherName(classEntity.getTeacher() != null ? classEntity.getTeacher().getFullName() : "N/A")
                .startPeriod(session.getStartPeriod())
                .endPeriod(session.getEndPeriod())
                .location(session.getLocation() != null ? session.getLocation() : "TBD")
                .sessionStatus(session.getStatus().name())
                .sessionDate(session.getSessionDate())
                .build();
    }
}
