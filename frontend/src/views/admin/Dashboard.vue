<template>
  <div class="admin-dashboard">
    <div class="glass-card mb-4" style="padding: 20px;">
      <h2>系统总览</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">查看系统整体运行情况，包括宏观用户数据、考试数量与系统资源占用状态。</p>
    </div>

    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <div class="glass-card stat-card primary">
          <div class="stat-icon"><i class="el-icon-user"></i></div>
          <div class="stat-info">
            <div class="stat-title">全平台用户总量</div>
            <div class="stat-value">{{ stats.totalUsers }} <span style="font-size:12px; font-weight:normal;">含师生与管理员</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-card success">
          <div class="stat-icon"><i class="el-icon-aim"></i></div>
          <div class="stat-info">
            <div class="stat-title">活跃考试任务</div>
            <div class="stat-value">{{ stats.activeExams }} <span style="font-size:12px; font-weight:normal;">正在进行中</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-card warning">
          <div class="stat-icon"><i class="el-icon-document-copy"></i></div>
          <div class="stat-info">
            <div class="stat-title">题库总题量</div>
            <div class="stat-value">{{ stats.totalQuestions }} <span style="font-size:12px; font-weight:normal;">累积试题量</span></div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-card stat-card danger">
          <div class="stat-icon"><i class="el-icon-data-line"></i></div>
          <div class="stat-info">
            <div class="stat-title">学生答卷总量</div>
            <div class="stat-value">{{ stats.totalRecords }} <span style="font-size:12px; font-weight:normal;">累计提交</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表展示区 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="12">
        <div class="glass-card p-4">
          <h3 style="margin-top:0; border-bottom:1px dashed var(--glass-border); padding-bottom:12px;">系统用户角色比例构成</h3>
          <div class="mock-chart-container">
            <div class="mock-bar-wrap">
              <div class="mock-label">学生</div>
              <div class="mock-bar" :style="{ width: `${getRolePercent('学生')}%`, background: 'var(--primary-color)' }"></div>
              <div class="mock-val">{{ getRoleCount('学生') }}</div>
            </div>
            <div class="mock-bar-wrap">
              <div class="mock-label">教师</div>
              <div class="mock-bar" :style="{ width: `${getRolePercent('教师')}%`, background: 'var(--success-color)' }"></div>
              <div class="mock-val">{{ getRoleCount('教师') }}</div>
            </div>
            <div class="mock-bar-wrap">
              <div class="mock-label">管理员</div>
              <div class="mock-bar" :style="{ width: `${getRolePercent('管理员')}%`, background: 'var(--warning-color)' }"></div>
              <div class="mock-val">{{ getRoleCount('管理员') }}</div>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="12">
        <div class="glass-card p-4">
          <h3 style="margin-top:0; border-bottom:1px dashed var(--glass-border); padding-bottom:12px;">近7日系统访问流量趋势</h3>
          <div class="mock-line-chart">
            <div class="line-point" style="bottom: 20%; left: 0%;"></div>
            <div class="line-segment" style="bottom: 20%; left: 0%; width: 16%; transform: rotate(-15deg); transform-origin: left bottom;"></div>
            
            <div class="line-point" style="bottom: 40%; left: 16%;"></div>
            <div class="line-segment" style="bottom: 40%; left: 16%; width: 16%; transform: rotate(20deg); transform-origin: left bottom;"></div>
            
            <div class="line-point" style="bottom: 30%; left: 32%;"></div>
            <div class="line-segment" style="bottom: 30%; left: 32%; width: 16%; transform: rotate(-45deg); transform-origin: left bottom;"></div>
            
            <div class="line-point" style="bottom: 60%; left: 48%;"></div>
            <div class="line-segment" style="bottom: 60%; left: 48%; width: 16%; transform: rotate(10deg); transform-origin: left bottom;"></div>
            
            <div class="line-point" style="bottom: 55%; left: 64%;"></div>
            <div class="line-segment" style="bottom: 55%; left: 64%; width: 16%; transform: rotate(45deg); transform-origin: left bottom;"></div>
            
            <div class="line-point" style="bottom: 85%; left: 80%;"></div>
            <div class="line-segment" style="bottom: 85%; left: 80%; width: 20%; transform: rotate(-10deg); transform-origin: left bottom;"></div>
            
            <div class="line-point" style="bottom: 80%; left: 100%;"></div>
          </div>
          <div style="display:flex; justify-content:space-between; margin-top:20px; font-size:12px; color:var(--text-secondary);">
            <span>周一</span><span>周二</span><span>周三</span><span>周四</span><span>周五</span><span>周六</span><span>周日</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 异常与健康状态 -->
    <div class="glass-card p-4">
      <div class="health-header">
        <div>
          <h3>系统服务运行监控 (Health Check)</h3>
          <p>每 30 秒自动刷新一次，最后更新：{{ healthUpdatedAt || '暂无' }}</p>
        </div>
        <el-button type="primary" plain :loading="loading" @click="fetchDashboardData">刷新监控</el-button>
      </div>
      <el-table :data="servers" class="custom-table mt-4" border="false">
        <el-table-column prop="node" label="服务组件名称" width="220" />
        <el-table-column prop="cpu" label="CPU使用率" width="150">
          <template #default="scope">
            <el-progress v-if="typeof scope.row.cpu === 'number'" :percentage="scope.row.cpu" :color="customColors" />
            <span v-else class="muted-text">不适用</span>
          </template>
        </el-table-column>
        <el-table-column prop="ram" label="内存占用率" width="150">
          <template #default="scope">
            <el-progress v-if="typeof scope.row.ram === 'number'" :percentage="scope.row.ram" :color="customColors" />
            <span v-else class="muted-text">不适用</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="检测详情" min-width="240" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'

const stats = ref({ totalUsers: 0, activeExams: 0, totalQuestions: 0, totalRecords: 0, roleDistribution: [] })
const loading = ref(false)
const healthUpdatedAt = ref('')
let refreshTimer = null

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/dashboard')
    stats.value.totalUsers = res.totalUsers || 0
    stats.value.activeExams = res.activeExams || 0
    stats.value.totalQuestions = res.totalQuestions || 0
    stats.value.totalRecords = res.totalRecords || 0
    stats.value.roleDistribution = res.roleDistribution || []
    servers.value = res.serviceHealth?.servers || []
    healthUpdatedAt.value = res.serviceHealth?.updatedAt || new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } catch(e) {
    console.error('Failed to fetch dashboard data', e)
  } finally {
    loading.value = false
  }
}

const getRoleItem = (role) => stats.value.roleDistribution.find(item => item.role === role) || { count: 0, percent: 0 }
const getRoleCount = (role) => getRoleItem(role).count
const getRolePercent = (role) => Math.max(Number(getRoleItem(role).percent) || 0, getRoleCount(role) > 0 ? 3 : 0)

onMounted(() => {
  fetchDashboardData()
  refreshTimer = window.setInterval(() => {
    if (!loading.value) {
      fetchDashboardData()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
})

const customColors = [
  { color: '#10b981', percentage: 40 },
  { color: '#f59e0b', percentage: 70 },
  { color: '#ef4444', percentage: 90 },
]

const servers = ref([])
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 24px; }
.mt-4 { margin-top: 20px; }

.stat-card { padding: 24px; display: flex; align-items: center; gap: 16px; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-5px); }

.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: bold; }
.primary .stat-icon { background: rgba(56, 189, 248, 0.1); color: #38bdf8; }
.success .stat-icon { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.warning .stat-icon { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.danger .stat-icon { background: rgba(239, 68, 68, 0.1); color: #ef4444; }

.stat-title { font-size: 14px; color: var(--text-secondary); margin-bottom: 6px; }
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }

.mock-chart-container { margin-top: 20px; }
.mock-bar-wrap { display: flex; align-items: center; margin-bottom: 16px; }
.mock-label { width: 80px; font-size: 14px; color: var(--text-secondary); }
.mock-bar { height: 12px; border-radius: 6px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.mock-val { margin-left: 10px; font-size: 14px; font-weight: 600; color: var(--text-primary); }

.mock-line-chart { position: relative; height: 150px; background: rgba(0,0,0,0.02); border-radius: 8px; margin-top: 20px; border-bottom: 1px solid var(--glass-border); border-left: 1px solid var(--glass-border); }
.line-point { position: absolute; width: 8px; height: 8px; background: var(--primary-color); border-radius: 50%; transform: translate(-50%, 50%); z-index: 2; box-shadow: 0 0 10px var(--primary-color); }
.line-segment { position: absolute; height: 3px; background: var(--primary-color); z-index: 1; border-radius: 2px; }

.health-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.health-header h3 { margin: 0; }
.health-header p { margin-top: 8px; color: var(--text-secondary); font-size: 12px; }
.muted-text { color: var(--text-secondary); font-size: 13px; }

:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; }
</style>
