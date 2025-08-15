package com.example.springboot_education.aspects;

import com.example.springboot_education.annotations.LoggableAction;
import com.example.springboot_education.entities.ActivityLog;
import com.example.springboot_education.entities.Users;
import com.example.springboot_education.repositories.ActivityLogRepository;
import com.example.springboot_education.repositories.UsersJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private final ActivityLogRepository activityLogRepository;
    private final UsersJpaRepository usersJpaRepository;

    public LoggingAspect(ActivityLogRepository activityLogRepository, UsersJpaRepository usersJpaRepository) {
        this.activityLogRepository = activityLogRepository;
        this.usersJpaRepository = usersJpaRepository;
    }

    @AfterReturning(pointcut = "@annotation(loggableAction)", returning = "result")
    public void logActivity(JoinPoint joinPoint, LoggableAction loggableAction, Object result) {
        log.info("AOP: Bắt đầu log hoạt động cho phương thức: {}", joinPoint.getSignature().getName());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("AOP: Không có người dùng được xác thực. Bỏ qua việc ghi log.");
            return;
        }
        
        // Sửa lỗi `Principal is not of type Users`
        Users authenticatedUser = getAuthenticatedUserFromDatabase(authentication);
        if (authenticatedUser == null) {
            log.error("AOP: Không thể lấy đối tượng Users từ UserDetails. Kiểm tra UserDetailsService.");
            return;
        }

        // Sửa lỗi `joinPoint is never read` và `Dereferencing possible null pointer`
        Integer targetId = getTargetId(result);
        if (targetId == null) {
            log.warn("AOP: Không thể lấy TargetId từ kết quả của phương thức. TargetId sẽ là null.");
        }

        try {
            ActivityLog logEntry = new ActivityLog();
            logEntry.setUser(authenticatedUser);
            logEntry.setActionType(loggableAction.value());
            logEntry.setTargetTable(loggableAction.entity());
            logEntry.setTargetId(targetId);
            logEntry.setDescription(loggableAction.description());
            logEntry.setCreatedAt(Instant.now());

            activityLogRepository.save(logEntry);
            log.info("AOP: Ghi log thành công. Action: {}, Target: {}", loggableAction.value(), loggableAction.entity());
        } catch (Exception e) {
            log.error("AOP: Đã xảy ra lỗi khi tạo hoặc lưu log: {}", e.getMessage(), e);
        }
    }

    private Users getAuthenticatedUserFromDatabase(Authentication authentication) {
        // ⭐ Sử dụng instanceof pattern matching để code gọn hơn
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            // ⭐ Đã sửa lỗi `findByUsername`
            return usersJpaRepository.findByUsername(username).orElse(null);
        }
        log.error("Principal type is not UserDetails. Found: {}", authentication.getPrincipal().getClass().getName());
        return null;
    }

    private Integer getTargetId(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            if (result instanceof Optional<?> optionalResult) {
                // Sửa lỗi Dereferencing possible null pointer
                if (optionalResult.isPresent()) {
                    Object entity = optionalResult.get();
                    Method getIdMethod = entity.getClass().getMethod("getId");
                    return (Integer) getIdMethod.invoke(entity);
                }
            } else if (result instanceof List<?> resultList) {
                if (!resultList.isEmpty()) {
                    Object firstElement = resultList.get(0);
                    Method getIdMethod = firstElement.getClass().getMethod("getId");
                    return (Integer) getIdMethod.invoke(firstElement);
                }
            } else {
                // Trường hợp trả về một đối tượng duy nhất
                Method getIdMethod = result.getClass().getMethod("getId");
                return (Integer) getIdMethod.invoke(result);
            }
        } catch (NoSuchMethodException e) {
            log.error("Lỗi: Không tìm thấy phương thức getId() trên đối tượng trả về.", e);
        } catch (IllegalAccessException e) {
            log.error("Lỗi: Không có quyền truy cập phương thức getId().", e);
        } catch (InvocationTargetException e) {
            log.error("Lỗi: Phương thức getId() đã ném ra một ngoại lệ.", e);
        }
        return null;
    }
}