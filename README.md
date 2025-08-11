# 📚 LMS API – Endpoint Checklist (With Descriptions & Status)

## **Auth & Profile**

✅ **POST /api/auth/login** – Đăng nhập và nhận token xác thực.  
✅ **POST /api/auth/register** – Đăng ký tài khoản mới.  
✅ **POST /api/auth/logout** – Đăng xuất và hủy token hiện tại.  
❌ **POST /api/auth/refresh** – Cấp lại token mới khi token cũ gần hết hạn.  
❌ **GET /api/me** – Lấy thông tin hồ sơ người dùng hiện tại.  
❌ **PATCH /api/me** – Cập nhật thông tin cá nhân của người dùng.

---

## **Users & Roles**

✅ **GET /api/users** – Lấy danh sách tất cả người dùng.  
✅ **GET /api/users/:id** – Lấy thông tin chi tiết một người dùng.  
✅ **POST /api/users** – Tạo mới người dùng.  
✅ **PATCH /api/users/:id** – Cập nhật thông tin người dùng.  
✅ **DELETE /api/users/:id** – Xóa người dùng.  
✅ **GET /api/security/roles** – Lấy danh sách các vai trò (role).  
✅ **POST /api/security/roles** – Tạo mới vai trò.  
✅ **PATCH /api/security/roles/:id** – Cập nhật vai trò.  
✅ **DELETE /api/security/roles/:id** – Xóa vai trò.  
✅ **GET /api/security/roles/:id** – Lấy chi tiết một vai trò.  
✅ **PATCH /api/security/roles/:id/add-users-to-role** – Thêm người dùng vào vai trò.  
✅ **DELETE /api/security/roles/:id/remove-users-from-role** – Gỡ người dùng ra khỏi vai trò.

---

## **Classes & Members**

✅ **GET /api/auth/classes** – Lấy danh sách lớp học.  
✅ **POST /api/auth/classes** – Tạo lớp học mới.  
✅ **GET /api/auth/classes/:id/students** – Lấy danh sách học sinh của lớp.  
✅ **POST /api/auth/classes/add-student** – Thêm học sinh vào lớp.  
✅ **GET /api/auth/classes/students/:id/classes** – Lấy danh sách lớp mà học sinh tham gia.  
✅ **GET /api/auth/classes/teacher/:id** – Lấy danh sách lớp do giáo viên phụ trách.  
✅ **PUT /api/auth/classes/:id** – Cập nhật thông tin lớp.  
✅ **DELETE /api/auth/classes/:id** – Xóa lớp học.

---

## **Attendance**

✅ **POST /api/attendances** – Ghi nhận điểm danh mới.  
✅ **GET /api/attendances/class-schedule/:id** – Lấy danh sách điểm danh theo lịch học.  
✅ **PATCH /api/attendances/:id** – Cập nhật trạng thái điểm danh.

---

## **Class Schedules**

✅ **GET /api/class-schedules** – Lấy danh sách lịch học.  
✅ **POST /api/class-schedules** – Tạo lịch học mới.  
✅ **PATCH /api/class-schedules/:id** – Cập nhật lịch học.

---

## **Materials**

✅ **POST /api/materials** – Tải lên tài liệu học tập.  
❌ **GET /api/materials/:id** – Lấy thông tin hoặc tải tài liệu.  
❌ **DELETE /api/materials/:id** – Xóa tài liệu.

---

## **Assignments**

✅ **GET /api/assignments** – Lấy danh sách bài tập.  
✅ **POST /api/assignments** – Tạo bài tập mới.  
✅ **GET /api/assignments/:id** – Lấy chi tiết bài tập.  
✅ **PATCH /api/assignments/:id** – Cập nhật bài tập.  
✅ **DELETE /api/assignments/:id** – Xóa bài tập.  
✅ **GET /api/assignments/:id/download** – Tải bài tập về.

---

## **Assignment Submissions**

✅ **GET /api/submissions** – Lấy danh sách bài nộp.  
✅ **POST /api/submissions** – Nộp bài tập.  
✅ **GET /api/submissions/:id/download** – Tải bài nộp.  
✅ **PATCH /api/submissions/:id/grade** – Chấm điểm bài nộp.  
✅ **DELETE /api/submissions/:id** – Xóa bài nộp.

---

## **Assignment Comments**

✅ **POST /api/assignment-comments** – Thêm bình luận vào bài tập.  
✅ **GET /api/assignment-comments/assignment/:id** – Lấy bình luận của bài tập.  
✅ **DELETE /api/assignment-comments/:id** – Xóa bình luận.

---

## **Quizzes**

✅ **GET /api/quizzes** – Lấy danh sách quiz.  
✅ **POST /api/quizzes** – Tạo quiz mới.  
✅ **GET /api/quizzes/:id?role=teacher** – Lấy thông tin quiz cho giáo viên.  
✅ **GET /api/quizzes/:id?role=student** – Lấy thông tin quiz cho học sinh.  
✅ **PATCH /api/quizzes/:id/content** – Cập nhật một phần nội dung quiz.  
✅ **PUT /api/quizzes/:id/content** – Cập nhật toàn bộ nội dung quiz.  
✅ **DELETE /api/quizzes/:id** – Xóa quiz.  
✅ **DELETE /api/quizzes/:quizId/questions/:questionId** – Xóa câu hỏi trong quiz.

---

## **Quiz Submissions**

✅ **POST /api/quiz-submissions** – Nộp bài quiz.  
✅ **GET /api/quiz-submissions/by-quiz/:id** – Lấy danh sách bài nộp theo quiz.

---

## **AI & Question Bank (Suggested)**

❌ **POST /api/ai/code/generate** – AI sinh mã nguồn từ yêu cầu.  
❌ **POST /api/ai/code/explain** – AI giải thích đoạn mã.  
❌ **POST /api/ai/quiz/generate** – AI tạo quiz tự động.  
❌ **POST /api/ai/quiz/validate** – AI kiểm tra và xác thực quiz.  
❌ **POST /api/ai/quiz/rewrite** – AI viết lại câu hỏi quiz.  
❌ **POST /api/ai/quiz/explain** – AI giải thích quiz.  
❌ **POST /api/question-bank/questions** – Thêm câu hỏi vào ngân hàng câu hỏi.  
❌ **GET /api/question-bank/questions** – Lấy danh sách câu hỏi trong ngân hàng.  
❌ **POST /api/quizzes/from-bank** – Tạo quiz từ ngân hàng câu hỏi.

---

## **Export / Import (Suggested)**

❌ **GET /api/quizzes/:id/export** – Xuất quiz ra file.  
❌ **POST /api/quizzes/import** – Nhập quiz từ file.  
❌ **GET /api/exports/:type** – Xuất dữ liệu theo loại.  
❌ **POST /api/imports/:type** – Nhập dữ liệu theo loại.

---

## **Grade & Analytics**

✅ **GET /api/student/grades/overview** – Xem tổng quan điểm của học sinh.  
✅ **GET /api/grades/overview** – Xem tổng quan điểm của tất cả học sinh.  
✅ **GET /api/grades/rankings** – Xem bảng xếp hạng.  
✅ **GET /api/grades/student/:id/rank** – Xem thứ hạng của học sinh.  
✅ **GET /api/grades/student/:id/details** – Xem chi tiết điểm của học sinh.  
✅ **GET /api/grades/subject-analysis** – Phân tích điểm theo môn học.  
✅ **GET /api/grades/trends** – Xem xu hướng điểm.  
❌ **GET /api/analytics/admin** – Báo cáo tổng hợp cho admin.  
❌ **GET /api/analytics/admin/users** – Thống kê người dùng.  
❌ **GET /api/analytics/admin/classes** – Thống kê lớp học.  
❌ **GET /api/analytics/admin/quizzes** – Thống kê quiz.  
❌ **GET /api/analytics/admin/assignments** – Thống kê bài tập.  
❌ **GET /api/analytics/admin/attendance** – Thống kê điểm danh.  
❌ **GET /api/analytics/admin/subjects** – Thống kê môn học.

---

## **Activity Log**

✅ **GET /api/activity-log** – Lấy nhật ký hoạt động.  
✅ **POST /api/activity-logs** – Ghi nhật ký hoạt động mới.

---

## **Subjects**

✅ **GET /api/auth/subjects** – Lấy danh sách môn học.  
✅ **POST /api/auth/subjects** – Tạo môn học mới.  
✅ **PUT /api/auth/subjects/:id** – Cập nhật môn học.  
✅ **DELETE /api/auth/subjects/:id** – Xóa môn học.  
