

-- Insert roles
INSERT INTO roles (name) SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');
INSERT INTO roles (name) SELECT 'TEACHER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TEACHER');
INSERT INTO roles (name) SELECT 'STUDENT' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'STUDENT');

-- Insert users
INSERT INTO users (username, password, full_name, email, created_at, updated_at) SELECT 'admin@lms.com', 'adminpass', 'Admin User', 'admin@lms.com', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin@lms.com');
INSERT INTO users (username, password, full_name, email, created_at, updated_at) SELECT 'teacher@lms.com', 'teachpass', 'Teacher One', 'teacher@lms.com', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'teacher@lms.com');
INSERT INTO users (username, password, full_name, email, created_at, updated_at) SELECT 'student@lms.com', 'studentpass', 'Student One', 'student@lms.com', NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'student@lms.com');

-- Map users to roles
INSERT INTO user_roles (user_id, role_id, enabled)
SELECT u.id, r.id, 1
FROM (SELECT id FROM users WHERE username = 'admin@lms.com') u,
     (SELECT id FROM roles WHERE name = 'ADMIN') r
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id = u.id AND role_id = r.id
);

INSERT INTO user_roles (user_id, role_id, enabled)
SELECT u.id, r.id, 1
FROM (SELECT id FROM users WHERE username = 'teacher@lms.com') u,
     (SELECT id FROM roles WHERE name = 'TEACHER') r
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id = u.id AND role_id = r.id
);

INSERT INTO user_roles (user_id, role_id, enabled)
SELECT u.id, r.id, 1
FROM (SELECT id FROM users WHERE username = 'student@lms.com') u,
     (SELECT id FROM roles WHERE name = 'STUDENT') r
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id = u.id AND role_id = r.id
);
-- Insert subjects
INSERT INTO subjects (subject_name, description, created_by, created_at, updated_at) SELECT 'Mathematics', 'Basic math subject', u.id, NOW(), NOW() FROM users u WHERE u.username = 'teacher@lms.com' AND NOT EXISTS (SELECT 1 FROM subjects WHERE subject_name = 'Mathematics');
INSERT INTO subjects (subject_name, description, created_by, created_at, updated_at) SELECT 'Physics', 'Basic physics subject', u.id, NOW(), NOW() FROM users u WHERE u.username = 'teacher@lms.com' AND NOT EXISTS (SELECT 1 FROM subjects WHERE subject_name = 'Physics');

-- Insert class
INSERT INTO classes (class_name, school_year, semester, description, teacher_id, subject_id, created_at, updated_at) SELECT '10A1', 2025, 'Semester 1', 'Lớp chuyên Toán', (SELECT id FROM users WHERE username = 'teacher@lms.com'), (SELECT id FROM subjects WHERE subject_name = 'Mathematics'), NOW(), NOW() WHERE NOT EXISTS (SELECT 1 FROM classes WHERE class_name = '10A1');

-- Insert class_user
INSERT INTO class_user (class_id, student_id, joined_at) SELECT c.id, u.id, NOW() FROM classes c, users u WHERE c.class_name = '10A1' AND u.username = 'student@lms.com' AND NOT EXISTS (SELECT 1 FROM class_user WHERE class_id = c.id AND student_id = u.id);

-- Insert assignment
INSERT INTO assignments (title, description, class_id, due_date, max_score, file_path, file_type, created_at, updated_at) SELECT 'Math Homework 1', 'Solve equations', c.id, DATE_ADD(NOW(), INTERVAL 7 DAY), 10.0, NULL, NULL, NOW(), NOW() FROM classes c WHERE c.class_name = '10A1' AND NOT EXISTS (SELECT 1 FROM assignments WHERE title = 'Math Homework 1');

-- Insert assignment_comment
INSERT INTO assignment_comments (assignment_id, user_id, comment, created_at, parent_id) SELECT a.id, u.id, 'This is my submission comment', NOW(), NULL FROM assignments a, users u WHERE a.title = 'Math Homework 1' AND u.username = 'student@lms.com' AND NOT EXISTS (SELECT 1 FROM assignment_comments WHERE assignment_id = a.id AND user_id = u.id);

-- Insert quiz
INSERT INTO quizzes (title, description, time_limit, start_date, end_date, class_id, created_by, created_at, updated_at, grade, subject) SELECT 'Quiz 1', 'Algebra basics', 30, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), c.id, u.id, NOW(), NOW(), '10', 'Math' FROM classes c, users u WHERE c.class_name = '10A1' AND u.username = 'teacher@lms.com' AND NOT EXISTS (SELECT 1 FROM quizzes WHERE title = 'Quiz 1');

-- Insert quiz_question
INSERT INTO quiz_questions (quiz_id, question_text, correct_option, score, created_at, updated_at) SELECT q.id, 'What is 2 + 2?', 'A', 1.0, NOW(), NOW() FROM quizzes q WHERE q.title = 'Quiz 1' AND NOT EXISTS (SELECT 1 FROM quiz_questions WHERE quiz_id = q.id);

-- Insert quiz_options
INSERT INTO quiz_options (question_id, option_label, option_text, created_at, updated_at)
SELECT qq.id, 'A', '4', NOW(), NOW()
FROM quiz_questions qq
WHERE qq.question_text = 'What is 2 + 2?'
  AND NOT EXISTS (
    SELECT 1 FROM quiz_options WHERE question_id = qq.id AND option_label = 'A'
);

INSERT INTO quiz_options (question_id, option_label, option_text, created_at, updated_at)
SELECT qq.id, 'B', '3', NOW(), NOW()
FROM quiz_questions qq
WHERE qq.question_text = 'What is 2 + 2?'
  AND NOT EXISTS (
    SELECT 1 FROM quiz_options WHERE question_id = qq.id AND option_label = 'B'
);

-- Insert quiz_submission
INSERT INTO quiz_submissions (quiz_id, student_id, submitted_at, score, graded_at, start_at, end_at) SELECT q.id, u.id, NOW(), 1.0, NOW(), NOW(), NOW() FROM quizzes q, users u WHERE q.title = 'Quiz 1' AND u.username = 'student@lms.com' AND NOT EXISTS (SELECT 1 FROM quiz_submissions WHERE quiz_id = q.id AND student_id = u.id);

-- Insert quiz_answer
INSERT INTO quiz_answers (submission_id, question_id, selected_option) SELECT s.id, qq.id, 'A' FROM quiz_submissions s, quiz_questions qq WHERE s.quiz_id = qq.quiz_id AND NOT EXISTS (SELECT 1 FROM quiz_answers WHERE submission_id = s.id AND question_id = qq.id);

-- Insert class_materials
INSERT INTO class_materials (
    class_id, title, file_path, download_count, created_at, updated_at, created_by
)
SELECT c.id, 'Lecture Notes', 'materials/lecture1.pdf', 0, NOW(), NOW(), u.id
FROM classes c, users u
WHERE c.class_name = '10A1'
  AND u.username = 'teacher@lms.com'
  AND NOT EXISTS (
    SELECT 1 FROM class_materials WHERE title = 'Lecture Notes'
);


-- Insert activity_logs
INSERT INTO activity_logs (user_id, class_id, action_type, target_table, target_id, description, created_at) SELECT u.id, c.id, 'UPLOAD', 'class_materials', cm.id, 'Uploaded lecture notes', NOW() FROM users u, classes c, class_materials cm WHERE u.username = 'teacher@lms.com' AND c.class_name = '10A1' AND cm.class_id = c.id AND NOT EXISTS (SELECT 1 FROM activity_logs WHERE target_table = 'class_materials' AND target_id = cm.id);