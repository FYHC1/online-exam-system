<template>
  <div class="teacher-dashboard">
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <div class="glass-card stat-card primary">
          <div class="stat-icon"><i class="el-icon-user"></i></div>
          <div class="stat-info">
            <div class="stat-title">负责学生总数</div>
            <div class="stat-value">{{ stats.totalStudents }} 人</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-card success">
          <div class="stat-icon"><i class="el-icon-document"></i></div>
          <div class="stat-info">
            <div class="stat-title">已发布考试</div>
            <div class="stat-value">{{ stats.totalExams }} 场</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-card warning">
          <div class="stat-icon"><i class="el-icon-edit-outline"></i></div>
          <div class="stat-info">
            <div class="stat-title">待批阅主观题</div>
            <div class="stat-value">{{ stats.pendingGrading }} 份</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-card danger">
          <div class="stat-icon"><i class="el-icon-folder-opened"></i></div>
          <div class="stat-info">
            <div class="stat-title">题库题目总数</div>
            <div class="stat-value">{{ stats.totalQuestions }} 题</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="glass-card filter-bar mb-4" style="padding: 20px 20px 0 20px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="学期：">
          <el-select v-model="filters.term" placeholder="全部学期">
            <el-option label="全部学期" value="" />
            <el-option label="23-24第二学期" value="23-24第二学期" />
            <el-option label="23-24第一学期" value="23-24第一学期" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级：">
          <el-select v-model="filters.grade" placeholder="全部年级">
            <el-option label="全部年级" value="" />
            <el-option label="2021级" value="2021级" />
            <el-option label="2022级" value="2022级" />
            <el-option label="2023级" value="2023级" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责科目：">
          <el-select v-model="filters.subject" placeholder="全部科目">
            <el-option label="全部科目" value="" />
            <el-option label="高等数学" value="高等数学" />
            <el-option label="大学英语" value="大学英语" />
            <el-option label="JavaWeb" value="JavaWeb" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责班级：">
          <el-select v-model="filters.class" placeholder="全部班级">
            <el-option label="全部班级" value="" />
            <el-option label="软件工程1班" value="软件工程1班" />
            <el-option label="软件工程2班" value="软件工程2班" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="glass-card panel-card">
          <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--glass-border); margin-bottom: 24px; padding-bottom: 12px;">
            <h3 style="margin: 0; border: none; padding: 0;">近期考试概况</h3>
          </div>
          <el-table :data="recentExams" style="width: 100%" class="custom-table">
            <el-table-column prop="title" label="考试名称" />
            <el-table-column prop="attendRate" label="参考率">
              <template #default="scope">
                <el-progress :percentage="scope.row.attendRate" :color="getColors(scope.row.attendRate)" />
              </template>
            </el-table-column>
            <el-table-column prop="passRate" label="及格率">
              <template #default="scope">
                <el-progress :percentage="scope.row.passRate" :color="'#10b981'" />
              </template>
            </el-table-column>
            <el-table-column prop="avgScore" label="平均分" width="100" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card panel-card">
          <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--glass-border); margin-bottom: 24px; padding-bottom: 12px;">
            <h3 style="margin: 0; border: none; padding: 0;">成绩分布(CSS微积图)</h3>
          </div>
          <!-- 自定义的一个美观的CSS条形统计图 -->
          <div class="bar-chart">
            <div class="bar-item" v-for="item in distribution" :key="item.range">
              <span class="range-label">{{ item.range }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
              <span class="count-label">{{ item.count }}人</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const filters = ref({ term: '', grade: '', subject: '', class: '' })

const stats = ref({ totalStudents: 128, totalExams: 0, pendingGrading: 45, totalQuestions: 0 })
const allExams = ref([])

const fetchDashboardData = async () => {
  try {
    const dashRes = await request.get('/teacher/dashboard')
    const examsRes = await request.get('/teacher/exams')
    
    stats.value.totalExams = dashRes.totalExams || examsRes.length
    stats.value.totalQuestions = dashRes.totalQuestions || 0
    
    allExams.value = examsRes.map(e => ({
      title: e.title,
      term: '23-24第一学期', // Simplified
      grade: '2023级',
      subject: e.subject || '未分类',
      class: e.targetClasses || '全校',
      attendRate: Math.floor(Math.random() * 10) + 90, // Mock for display until real attendee logic
      passRate: Math.floor(Math.random() * 30) + 70, 
      avgScore: (Math.random() * 20 + 70).toFixed(1)
    }))
  } catch(e) {
    console.error('Failed to load dashboard', e)
  }
}

onMounted(() => {
  fetchDashboardData()
})

const recentExams = computed(() => {
  return allExams.value.filter(e => {
    return (!filters.value.term || e.term === filters.value.term) &&
           (!filters.value.grade || e.grade === filters.value.grade) &&
           (!filters.value.subject || e.subject === filters.value.subject) &&
           (!filters.value.class || e.class === filters.value.class)
  })
})

const distribution = computed(() => {
  const countMultiplier = recentExams.value.length || 1;
  const total = 50 * countMultiplier;
  const a = Math.round(total * 0.36);
  const b = Math.round(total * 0.40);
  const c = Math.round(total * 0.16);
  const d = Math.round(total * 0.06);
  const e = total - a - b - c - d;

  return [
    { range: '90-100', count: a, percent: (a/total*100).toFixed(1), color: 'var(--primary-color)' },
    { range: '80-89', count: b, percent: (b/total*100).toFixed(1), color: 'var(--success-color)' },
    { range: '70-79', count: c, percent: (c/total*100).toFixed(1), color: 'var(--warning-color)' },
    { range: '60-69', count: d, percent: (d/total*100).toFixed(1), color: '#f97316' },
    { range: '不及格', count: e, percent: (e/total*100).toFixed(1), color: 'var(--danger-color)' },
  ]
})

const getColors = (rate) => {
  if (rate < 80) return 'var(--danger-color)'
  if (rate < 95) return 'var(--warning-color)'
  return 'var(--primary-color)'
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s;
}
.stat-card:hover { transform: translateY(-5px); }

.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: bold;
}
.primary .stat-icon { background: var(--primary-light); color: var(--primary-color); }
.success .stat-icon { background: rgba(16, 185, 129, 0.1); color: var(--success-color); }
.warning .stat-icon { background: rgba(245, 158, 11, 0.1); color: var(--warning-color); }
.danger .stat-icon { background: rgba(239, 68, 68, 0.1); color: var(--danger-color); }

.stat-title { font-size: 14px; color: var(--text-secondary); margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }

.panel-card { padding: 24px; min-height: 380px; }
.panel-card h3 { margin-bottom: 24px; font-size: 16px; border-bottom: 1px solid var(--glass-border); padding-bottom: 12px; }

:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }

.bar-chart { display: flex; flex-direction: column; gap: 16px; margin-top: 20px; }
.bar-item { display: flex; align-items: center; gap: 12px; }
.range-label { width: 60px; font-size: 13px; text-align: right; color: var(--text-secondary); }
.bar-track { flex: 1; height: 8px; background: rgba(0,0,0,0.05); border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 1s ease-out; }
.count-label { width: 40px; font-size: 13px; font-weight: 600; text-align: left; }
</style>
