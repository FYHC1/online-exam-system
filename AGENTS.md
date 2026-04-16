# AGENTS.md - Online Exam System

## Project Overview

A full-stack online examination system with separate `backend/` (Java) and `frontend/` (Vue) directories.

---

## Build & Run Commands

### Backend (Spring Boot)
```bash
cd backend
mvn clean package                    # Build JAR
mvn spring-boot:run                  # Run dev server (port 8080)
```

### Frontend (Vue + Vite)
```bash
cd frontend
npm install                           # Install dependencies
npm run dev                           # Dev server (port 3000)
npm run build                         # Production build
```

### Database Setup
1. Create MySQL database: `CREATE DATABASE online_exam`
2. Run schema: `backend/src/main/resources/exam_system.sql`
3. Run seed data: `backend/src/main/resources/seed_data.sql`

---

## Architecture

### Backend (`backend/`)
- **Package**: `com.exam.system`
- **Main class**: `ExamSystemApplication.java`
- **API base path**: `http://localhost:8080/api`
- **Layers**:
  - `controller/` - REST endpoints (Auth, Student, Teacher, Admin)
  - `service/` - Business logic
  - `mapper/` - MyBatis-Plus data access
  - `entity/` - JPA/Lombok data models
  - `security/` - JWT authentication filter
  - `config/` - Spring Security, Redis, CORS, MyBatis config
  - `common/` - Result wrapper, global exception handler
  - `utils/` - JwtUtils, RedisUtils

### Frontend (`frontend/`)
- **Framework**: Vue 3 + Vite + Element Plus
- **Router**: Role-based guards (`student`, `teacher`, `admin`)
- **Layouts**: `StudentLayout.vue`, `TeacherLayout.vue`, `AdminLayout.vue`
- **API layer**: `src/utils/request.js` (axios with JWT interceptor)
- **Dev proxy**: Vite proxies `/api` → `http://localhost:8080`

---

## API Conventions

- All requests/responses use JSON
- Auth header: `Authorization: Bearer <token>` (except login/register)
- Standard response: `{ "code": 200, "message": "...", "data": {...} }`
- Captcha required for login (UUID stored in Redis)

### Test Accounts (password: `123456` for all)
| Username   | Role    |
|------------|---------|
| admin      | admin   |
| teacher1   | teacher |
| student1   | student |

---

## Key Technical Details

### Dependencies
- **Backend**: Spring Boot 3.2.3, Java 17, MyBatis-Plus 3.5.5, JWT (jjwt 0.11.5), Redis, Kaptcha (captcha)
- **Frontend**: Vue 3.5, Vue Router 5, Element Plus 2.13, Axios 1.13

### Configuration
- Backend config: `backend/src/main/resources/application.yml`
- MySQL: `localhost:3306/online_exam` (user: root, pass: root)
- Redis: `localhost:6379` (password: 123456)
- JWT secret configured in `application.yml`

### Database Tables
- `sys_user` - All users (admin, teacher, student roles)
- `sys_class` - Classes/departments
- `question_bank` - Questions with JSON options
- `test_paper` - Exam paper templates
- `paper_question_rel` - Paper-Question M:N relationship
- `exam_arrangement` - Scheduled exams
- `student_exam_record` - Student exam submissions
- `student_answer_detail` - Per-question answers
- `wrong_question_book` - Incorrect answers for review
- `sys_announcement` - Platform announcements

---

## Common Patterns

### Adding a New Controller
1. Create controller in `controller/`
2. Use `@RequestMapping` with role-specific prefix
3. Return `Result.success(data)` or `Result.error(msg)`
4. Add service interface in `service/`
5. Add mapper in `mapper/`

### Frontend API Call
```javascript
import request from '@/utils/request'

// GET
const data = await request.get('/student/exams')

// POST
await request.post('/student/exam/submit', { examId, answers })
```

---

## Development Notes

- Backend outputs SQL logs to stdout (configured in `mybatis-plus.configuration.log-impl`)
- Frontend uses path alias `@` → `./src`
- Auth bypass for testing: login accepts `code=dummy&uuid=dummy` when captcha unavailable
- Frontend routes auto-redirect authenticated users away from `/login`
