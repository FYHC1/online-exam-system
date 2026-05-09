<template>
  <div class="admin-analytics">
    <div class="glass-card header-card mb-4">
      <div>
        <h2>数据监控</h2>
        <p>查看考试、成绩、题库和异常记录，帮助管理员快速发现需要处理的问题。</p>
        <p class="refresh-hint">每 30 秒自动刷新一次，最后更新：{{ lastUpdated || '暂无' }}</p>
      </div>
      <el-button type="primary" plain :loading="loading" @click="fetchAnalytics">刷新数据</el-button>
    </div>

    <el-row :gutter="20" class="mb-4">
      <el-col :span="6" v-for="item in summaryCards" :key="item.label">
        <div class="glass-card stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-desc">{{ item.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-4">
      <el-col :span="16">
        <div class="glass-card p-4 panel-card">
          <div class="panel-header">
            <div>
              <h3>学科通过情况</h3>
              <p>按学科统计考试数量、提交数量、平均分和通过率。</p>
            </div>
          </div>
          <el-table :data="subjects" class="custom-table" style="width: 100%">
            <el-table-column prop="subject" label="学科" min-width="140" />
            <el-table-column prop="examCount" label="考试数" width="90" align="center" />
            <el-table-column prop="recordCount" label="提交数" width="90" align="center" />
            <el-table-column prop="avgScore" label="平均分" width="90" align="center" />
            <el-table-column label="通过率" min-width="180">
              <template #default="scope">
                <el-progress :percentage="Number(scope.row.passRate || 0)" :color="getRateColor(scope.row.passRate)" />
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!subjects.length && !loading" description="暂无学科数据" :image-size="80" />
        </div>
      </el-col>

      <el-col :span="8">
        <div class="glass-card p-4 panel-card">
          <div class="panel-header compact">
            <h3>学院考试量</h3>
            <p>按考试目标班级所属学院统计。</p>
          </div>
          <ul class="rank-list">
            <li v-for="(item, index) in departments" :key="item.department">
              <span class="rank-num" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
              <span class="rank-name">{{ item.department }}</span>
              <span class="rank-score">{{ item.examCount }} 场</span>
            </li>
          </ul>
          <el-empty v-if="!departments.length && !loading" description="暂无学院数据" :image-size="80" />
        </div>
      </el-col>
    </el-row>

    <div class="glass-card p-4">
      <div class="panel-header">
        <div>
          <h3>需关注记录</h3>
          <p>列出异常结束或低于及格线的记录，便于管理员跟进复核、补考或教学反馈。</p>
        </div>
        <el-tag type="warning">{{ attentionRecords.length }} 条</el-tag>
      </div>
      <el-table :data="attentionRecords" class="custom-table" style="width: 100%">
        <el-table-column prop="recordId" label="记录编号" width="100" />
        <el-table-column prop="exam" label="考试" min-width="180" show-overflow-tooltip />
        <el-table-column prop="student" label="学生" width="120" />
        <el-table-column prop="score" label="分数" width="90" align="center">
          <template #default="scope">{{ scope.row.score ?? '待批阅' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="情况" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === '异常结束' ? 'danger' : 'warning'" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="建议处理" min-width="220" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!attentionRecords.length && !loading" description="暂无需关注记录" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const summary = ref({})
const subjects = ref([])
const departments = ref([])
const attentionRecords = ref([])
const lastUpdated = ref('')
let refreshTimer = null

const summaryCards = computed(() => [
  { label: '学生人数', value: summary.value.studentCount || 0, desc: '当前可参与考试的学生账号' },
  { label: '考试总数', value: summary.value.examCount || 0, desc: '平台已创建的考试安排' },
  { label: '答卷提交', value: summary.value.submittedCount || 0, desc: '学生已提交的考试记录' },
  { label: '平均分', value: `${Number(summary.value.avgScore || 0).toFixed(1)} 分`, desc: `通过率 ${Number(summary.value.passRate || 0).toFixed(1)}%` },
  { label: '待批阅', value: summary.value.gradingCount || 0, desc: '需要教师处理的主观题记录' },
  { label: '异常记录', value: summary.value.abnormalCount || 0, desc: '异常结束或需人工复核的记录' },
  { label: '题库题目', value: summary.value.questionCount || 0, desc: '当前题库中可用题目数量' },
  { label: '教师人数', value: summary.value.teacherCount || 0, desc: '当前教师账号数量' }
])

const getRateColor = (rate) => {
  const value = Number(rate || 0)
  if (value < 60) return 'var(--danger-color)'
  if (value < 80) return 'var(--warning-color)'
  return 'var(--success-color)'
}

const fetchAnalytics = async () => {
  loading.value = true
  try {
    const data = await request.get('/admin/analytics')
    summary.value = data.summary || {}
    subjects.value = data.subjects || []
    departments.value = data.departments || []
    attentionRecords.value = data.attentionRecords || []
    lastUpdated.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch (e) {
    ElMessage.error('获取数据监控信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAnalytics()
  refreshTimer = window.setInterval(() => {
    if (!loading.value) {
      fetchAnalytics()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 24px; }

.header-card {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}
.header-card p,
.panel-header p,
.stat-desc {
  color: var(--text-secondary);
}
.header-card p,
.panel-header p {
  margin-top: 8px;
}
.refresh-hint {
  font-size: 12px;
}

.stat-card {
  padding: 20px;
  min-height: 118px;
}
.stat-label {
  color: var(--text-secondary);
  font-size: 14px;
}
.stat-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
}
.stat-desc {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.5;
}

.panel-card {
  min-height: 390px;
}
.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px dashed var(--glass-border);
  padding-bottom: 12px;
  margin-bottom: 16px;
}
.panel-header.compact {
  display: block;
}
.panel-header h3 {
  margin: 0;
}

.rank-list {
  padding: 0;
  margin: 0;
  list-style: none;
}
.rank-list li {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed var(--glass-border);
}
.rank-list li:last-child { border-bottom: none; }
.rank-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 12px;
  color: var(--text-secondary);
}
.rank-num.top3 {
  background: var(--primary-color);
  color: white;
}
.rank-name {
  flex: 1;
  color: var(--text-primary);
  font-weight: 500;
}
.rank-score {
  font-weight: 600;
  color: var(--primary-color);
}

:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; }
</style>
