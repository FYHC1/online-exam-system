import { createRouter, createWebHistory } from 'vue-router';

// 页面组件通过路由懒加载
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '统一登录入口' }
  },
  // --- 学生端 ---
  {
    path: '/student',
    component: () => import('../layout/StudentLayout.vue'),
    meta: { requireAuth: true, role: 'student' },
    children: [
      { path: 'dashboard', name: 'StudentDashboard', component: () => import('../views/student/Dashboard.vue'), meta: { title: '我的桌面' } },
      { path: 'exams', name: 'StudentExams', component: () => import('../views/student/Exams.vue'), meta: { title: '我的考试' } },
      { path: 'profile', name: 'StudentProfile', component: () => import('../views/student/Profile.vue'), meta: { title: '个人资料' } },
      { path: 'exam-room/:examId', name: 'ExamRoom', component: () => import('../views/student/ExamRoom.vue'), meta: { title: '在线考试' } },
      { path: 'practice', name: 'Practice', component: () => import('../views/student/Practice.vue'), meta: { title: '我的题库' } },
      { path: 'scores', name: 'Scores', component: () => import('../views/student/Scores.vue'), meta: { title: '我的成绩' } },
    ]
  },
  // --- 教师端 ---
  {
    path: '/teacher',
    component: () => import('../layout/TeacherLayout.vue'),
    meta: { requireAuth: true, role: 'teacher' },
    children: [
      { path: 'dashboard', name: 'TeacherDashboard', component: () => import('../views/teacher/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'profile', name: 'TeacherProfile', component: () => import('../views/teacher/Profile.vue'), meta: { title: '个人配置' } },
      { path: 'questions', name: 'TeacherQuestionPool', component: () => import('../views/teacher/QuestionPool.vue'), meta: { title: '题库管理' } },
      { path: 'exams', name: 'TeacherExams', component: () => import('../views/teacher/Exams.vue'), meta: { title: '考试管理' } },
      { path: 'manual-paper', name: 'ManualPaperBuilder', component: () => import('../views/teacher/ManualPaperBuilder.vue'), meta: { title: '手动组卷' } },
      { path: 'grading', name: 'TeacherGrading', component: () => import('../views/teacher/Grading.vue'), meta: { title: '批阅试卷' } },
    ]
  },
  // --- 管理员端 ---
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requireAuth: true, role: 'admin' },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '系统总览' } },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue'), meta: { title: '用户管理' } },
      { path: 'announcements', name: 'AdminAnnouncements', component: () => import('../views/admin/Announcements.vue'), meta: { title: '公告管理' } },
      { path: 'resources', name: 'AdminResources', component: () => import('../views/admin/Resources.vue'), meta: { title: '题库与考试' } },
      { path: 'analytics', name: 'AdminAnalytics', component: () => import('../views/admin/Analytics.vue'), meta: { title: '全局数据监控' } },
      { path: 'settings', name: 'AdminSettings', component: () => import('../views/admin/Settings.vue'), meta: { title: '系统配置' } },
    ]
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫：登录拦截与角色校验
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 在线考试系统`;
  }

  const token = localStorage.getItem('token');
  const userInfoStr = localStorage.getItem('userInfo');
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null;

  if (to.meta.requireAuth) {
    if (!token) {
      next('/login');
    } else {
      // 校验角色
      if (to.meta.role && userInfo && userInfo.role !== to.meta.role) {
        next(`/${userInfo.role}/dashboard`); // 强制跳转到自己的角色首页
      } else {
        next();
      }
    }
  } else {
    // 已经登录了不能去登录页
    if (to.path === '/login' && token && userInfo) {
      next(`/${userInfo.role}/dashboard`);
    } else {
      next();
    }
  }
});

export default router;
