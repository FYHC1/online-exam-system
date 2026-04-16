# 在线考试系统 - 接口文档 (API Documentation)

全局说明：
所有接口均基于 `application/json` 进行数据交互。
所有请求头需携带 Token 进行鉴权：`Authorization: Bearer <token>` (除登录/注册、获取验证码接口外)。
验证码校验机制：登录接口需配合Redis校验验证码。

全局统一响应结构：
```json
{
  "code": 200,          // 状态码：200-成功，401-未授权，403-无权限，500-服务器错误
  "message": "操作成功", // 响应提示信息
  "data": {}            // 响应业务数据（对象或数组）
}
```

---

## 1. 认证与公共模块 (Auth)

### 1.1 获取图形验证码
- **路径**: `/api/auth/captcha`
- **方法**: `GET`
- **响应数据**:
  - `uuid` (string): 验证码唯一标识 (存Redis键)
  - `img` (string): Base64 验证码图片数据

### 1.2 用户登录
- **路径**: `/api/auth/login`
- **方法**: `POST`
- **参数**:
  - `username` (string): 登录账号 (学号/工号)
  - `password` (string): 密码
  - `role` (string): 角色类型 (`student`, `teacher`, `admin`)
  - `code` (string): 验证码
  - `uuid` (string): 验证码唯一标识
- **响应数据**:
  - `token` (string): JWT 鉴权 Token
  - `userInfo` (object): 用户基本信息
    - `userId` (number)
    - `realName` (string)
    - `role` (string)
    - `classId` (number, 可选)

### 1.3 用户注册 (仅限学生)
- **路径**: `/api/auth/register`
- **方法**: `POST`
- **参数**:
  - `username`, `password`, `realName`, `phone`, `classId`

### 1.4 修改密码
- **路径**: `/api/auth/updatePassword`
- **方法**: `POST`
- **参数**:
  - `oldPassword` (string)
  - `newPassword` (string)

---

## 2. 学生端接口 (Student)

### 2.1 获取首页信息
- **路径**: `/api/student/dashboard`
- **方法**: `GET`
- **简介**: 获取最新公告、近期未完成考试、成绩概览。

### 2.2 获取考试列表
- **路径**: `/api/student/exams`
- **方法**: `GET`
- **参数**: `status` (可选: `pending`, `finished`)
- **响应数据**: 包含考试ID、标题、起止时间、及格线、时长、状态。

### 2.3 获取试卷详情 (开始答题)
- **路径**: `/api/student/exam/{examId}`
- **方法**: `GET`
- **响应数据**: 试卷基础信息及题目列表（无答案）。

### 2.4 提交考试答卷
- **路径**: `/api/student/exam/submit`
- **方法**: `POST`
- **参数**: 
  - `examId` (number)
  - `answers` (array): `[{ "questionId": 1, "answer": "A" }, ...]`
- **响应数据**: 客观题得分、总分（如果全为客观题可以直接返回结果）、状态（待批阅/已批阅）。

### 2.5 获取错题本列表
- **路径**: `/api/student/wrong-questions`
- **方法**: `GET`

### 2.6 获取历史成绩与答卷明细
- **路径**: `/api/student/records`
- **方法**: `GET`

---

## 3. 教师端接口 (Teacher)

### 3.1 教师数据看板
- **路径**: `/api/teacher/dashboard`
- **方法**: `GET`
- **简介**: 获取教师负责班级的参考率、及格率、平均分等统计。

### 3.2 题库与试题管理 (题库按科目进行组织)
- **路径**: `/api/teacher/questions`
- **方法**: `GET` / `POST` / `PUT` / `DELETE`
- **参数 (GET)**: `subjectId`, `difficulty`, `type`, `keyword`
- **批量导入**: `/api/teacher/questions/import` (POST `multipart/form-data`)

### 3.3 试卷与考试任务管理
- **路径**: `/api/teacher/exams`
- **方法**: `POST` (发布考试), `GET` (获取列表), `PUT` (修改), `DELETE` (取消)
- **自动组卷参数**: `autoConfig: { single: 10, multiple: 5, judge: 10, difficulty: "medium" }`
- **状态管理**: 支持结束考试 (`status: finished`) 或监控。

### 3.4 待批改答卷列表
- **路径**: `/api/teacher/grading/list`
- **方法**: `GET`
- **参数**: `examId`, `classId`

### 3.5 提交批阅评分 (支持按题/按人)
- **路径**: `/api/teacher/grading/submit`
- **方法**: `POST`
- **参数**:
  - `recordId` (答卷记录ID)
  - `scores`: `[{ "detailId": 10, "score": 5 }]`

---

## 4. 超级管理员接口 (Admin)

### 4.1 全平台数据总览 (大盘数据)
- **路径**: `/api/admin/dashboard`
- **方法**: `GET`

### 4.2 用户与班级组织架构管理
- **路径**: `/api/admin/users`, `/api/admin/classes`, `/api/admin/departments`
- **方法**: `GET`, `POST`, `PUT`, `DELETE`
- **功能**: 管理学校的院系、专业、班级树状结构，以及增删改查账户、密码重置。

### 4.3 公告发布与维护
- **路径**: `/api/admin/announcements`
- **方法**: `GET`, `POST`, `PUT`, `DELETE`

### 4.4 系统配置与日志审计
- **路径**: `/api/admin/settings/logs`
- **方法**: `GET`
- **简介**: 查询底层的系统敏感操作审计日志。
