-- 테스트 데이터 (서버 시작 시 자동 실행)

-- 1. 회원 데이터
INSERT IGNORE INTO member (student_id, password, name, major, role, status)
VALUES
('20240001', '$2a$10$example', '홍길동', '컴퓨터공학과', 'EWHA_STUDENT', 'ACTIVE'),
('20240002', '$2a$10$example', '김영희', '경영학과', 'EWHA_STUDENT', 'ACTIVE'),
('20190001', '$2a$10$example', '이동문', '수학과', 'ALUMNI', 'ACTIVE');

-- 2. 프로그램 데이터
INSERT IGNORE INTO program (name, center_type, category, price, difficulty)
VALUES
('필라테스', 'ECC_FITNESS', 'GX', 50000, '초급'),
('요가', 'ECC_FITNESS', 'GX', 45000, '초급'),
('스피닝', 'ECC_FITNESS', 'GX', 40000, '중급'),
('헬스', 'FITNESS_ROOM', 'HEALTH', 30000, '초급'),
('스쿼시', 'ECC_FITNESS', 'SQUASH', 60000, '중급');

-- 3. 강좌 데이터
INSERT IGNORE INTO course (name, program_id, instructor_name, day_of_week, start_time, end_time, max_capacity, current_capacity, available_seats, status, version)
VALUES
('필라테스 A반', 1, '김강사', '월수금', '09:00', '10:00', 20, 15, 5, 'ACTIVE', 0),
('필라테스 B반', 1, '김강사', '화목', '10:00', '11:00', 20, 5, 15, 'ACTIVE', 0),
('요가 A반', 2, '이강사', '월수금', '11:00', '12:00', 15, 14, 1, 'ACTIVE', 0),
('스피닝 A반', 3, '박강사', '화목', '18:00', '19:00', 25, 20, 5, 'ACTIVE', 0),
('헬스 기초반', 4, '최강사', '월화수목금', '07:00', '08:00', 30, 10, 20, 'ACTIVE', 0);

-- 4. 계좌 데이터
INSERT IGNORE INTO account (account_number, balance, member_id)
VALUES
('1234567890', 100000, 1),
('0987654321', 200000, 2),
('1122334455', 150000, 3);