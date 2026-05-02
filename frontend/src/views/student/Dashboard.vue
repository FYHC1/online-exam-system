<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="24">
        <div class="glass-card welcome-card">
          <div class="welcome-text">
            <h2>早上好，{{ user.realName || user.username }}，准备好今天的学习了吗？</h2>
            <p>目前您有 <strong class="text-danger">{{ pendingCount }}</strong> 门待考考试，历史平均分 <strong class="text-primary">{{ stats.avgScore.toFixed(1) }}</strong> 分。</p>
          </div>
          <el-button type="primary" size="large" @click="$router.push('/student/practice')">继续练习</el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 核心功能快捷入口 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <div class="glass-card shortcut-card" @click="$router.push('/student/exams')">
          <div class="shortcut-icon" style="background: rgba(56, 189, 248, 0.1); color: #38bdf8;"><i class="el-icon-edit-outline"></i></div>
          <h3>在线考试</h3>
          <p>参加正式测验与考试</p>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card shortcut-card" @click="$router.push('/student/practice')">
          <div class="shortcut-icon" style="background: rgba(16, 185, 129, 0.1); color: #10b981;"><i class="el-icon-reading"></i></div>
          <h3>个人练习</h3>
          <p>题库自测与错题本</p>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card shortcut-card" @click="$router.push('/student/scores')">
          <div class="shortcut-icon" style="background: rgba(245, 158, 11, 0.1); color: #f59e0b;"><i class="el-icon-document"></i></div>
          <h3>成绩中心</h3>
          <p>历史成绩与分析报告</p>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card shortcut-card" @click="goAnnouncements">
          <div class="shortcut-icon" style="background: rgba(168, 85, 247, 0.1); color: #a855f7;"><i class="el-icon-bell"></i></div>
          <h3>平台公告</h3>
          <p>查看最新系统资讯</p>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 待考列表 -->
      <el-col :span="16">
        <div class="glass-card panel-card">
          <div class="panel-header">
            <h3>近期考试任务</h3>
          </div>
          <el-table :data="exams" style="width: 100%" class="custom-table" :row-style="{ background: 'transparent' }" :header-cell-style="{ background: 'rgba(255,255,255,0.2)', color: 'var(--text-primary)' }">
            <el-table-column prop="title" label="考试名称" />
            <el-table-column prop="time" label="起止时间" width="280" />
            <el-table-column prop="duration" label="时长" width="80" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getStatusTag(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button v-if="scope.row.status === '未参加'" type="primary" size="small" @click="startExam(scope.row.id)">进入考试</el-button>
                <el-button v-else-if="scope.row.status === '待批阅'" type="warning" size="small" plain disabled>待批阅</el-button>
                <el-button v-else-if="scope.row.status === '已批阅'" type="success" size="small" plain @click="$router.push('/student/scores')">查看成绩</el-button>
                <el-button v-else-if="scope.row.status === '异常结束'" type="danger" size="small" plain disabled>异常结束</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 系统公告 -->
      <el-col :span="8">
        <div ref="announcementPanel" class="glass-card panel-card">
          <div class="panel-header">
            <h3>系统公告</h3>
          </div>
          <ul class="announcement-list">
            <li v-for="item in announcements" :key="item.id" @click="viewAnnouncement(item)">
              <span class="tag" v-if="item.isTop">置顶</span>
              <span class="title">{{ item.title }}</span>
              <span class="date">{{ item.date }}</span>
            </li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('userInfo') || '{}'))

const exams = ref([])
const stats = ref({ totalExams: 0, avgScore: 0 })
const announcementPanel = ref(null)
const pendingCount = computed(() => exams.value.filter(e => e.status === '未参加').length)

const fetchDashboardData = async () => {
  try {
    const statData = await request.get('/student/dashboard')
    stats.value = statData
    
    const classId = user.value.classId || 1
    const examData = await request.get(`/student/exams?classId=${classId}`)
    const recordsData = await request.get('/student/records')
    const recordMap = {}
    recordsData.forEach(item => {
      recordMap[item.record.examId] = item.record
    })

    exams.value = examData.map(e => {
      const record = recordMap[e.examId]
      let status = '未参加'
      if (record) {
        if (record.status === 'grading' || record.status === 'pending') status = '待批阅'
        else if (record.status === 'finished') status = '已批阅'
        else if (record.status === 'abnormal') status = '异常结束'
      }

      return {
        id: e.examId,
        title: e.title,
        time: `${new Date(e.startTime).toLocaleString()} ~ ${new Date(e.endTime).toLocaleString()}`,
        duration: '120分钟', // Simplified
        status
      }
    })
  } catch (error) {
    console.error('获取学生大盘数据失败', error)
  }
}

onMounted(() => {
  fetchDashboardData()
  fetchAnnouncements()
})

const getStatusTag = (status) => {
  const map = { '未参加': 'info', '待批阅': 'warning', '已批阅': 'success', '异常结束': 'danger' };
  return map[status] || 'info';
}

const announcements = ref([])

const fetchAnnouncements = async () => {
  try {
    const res = await request.get('/announcements')
    announcements.value = res.map(item => ({
      id: item.id,
      title: item.title,
      content: item.content,
      isTop: item.isTop === 1,
      date: item.createTime ? new Date(item.createTime).toLocaleDateString().slice(5) : ''
    }))
  } catch (e) {
    announcements.value = []
  }
}

const startExam = (id) => {
  router.push(`/student/exam-room/${id}`)
}

const goAnnouncements = () => {
  announcementPanel.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const viewAnnouncement = (item) => {
  ElMessageBox.alert(item.content || '暂无详细内容', item.title, {
    confirmButtonText: '我知道了',
    type: 'info'
  })
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.text-danger { color: var(--danger-color); }
.text-primary { color: var(--primary-color); }

.welcome-card {
  padding: 30px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
}
.welcome-card h2 { margin-bottom: 8px; font-weight: 700; color: var(--primary-color); }
.welcome-card p { color: var(--text-secondary); font-size: 15px; }

.shortcut-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}
.shortcut-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}
.shortcut-icon {
  width: 56px; height: 56px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; margin-bottom: 12px;
}
.shortcut-card h3 { font-size: 16px; margin: 0 0 6px 0; font-weight: 600; color: var(--text-primary); }
.shortcut-card p { font-size: 12px; color: var(--text-secondary); margin: 0; }

.panel-card {
  padding: 24px;
  min-height: 300px;
}
.panel-header {
  margin-bottom: 20px;
  border-bottom: 2px solid var(--glass-border);
  padding-bottom: 12px;
}
.panel-header h3 { font-size: 18px; font-weight: 600; }

.custom-table {
  background: transparent !important;
}

:deep(.el-table, .el-table__expanded-cell) {
  background-color: transparent !important;
}
:deep(.el-table tr, .el-table td, .el-table th) {
  background-color: transparent !important;
  border-bottom: 1px solid var(--glass-border);
}

.announcement-list {
  list-style: none;
  padding: 0;
}
.announcement-list li {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed var(--glass-border);
  cursor: pointer;
  transition: all 0.3s ease;
}
.announcement-list li:hover {
  transform: translateX(5px);
  color: var(--primary-color);
}
.announcement-list .tag {
  background: var(--danger-color);
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 8px;
}
.announcement-list .title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}
.announcement-list .date {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 10px;
}
</style>
