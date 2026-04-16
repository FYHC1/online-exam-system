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
            <div class="stat-title">日均访问量 (UV)</div>
            <div class="stat-value">14.2k <span style="font-size:12px; font-weight:normal;">近7天平均</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表展示区 mock用css写的假图表 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="12">
        <div class="glass-card p-4">
          <h3 style="margin-top:0; border-bottom:1px dashed var(--glass-border); padding-bottom:12px;">系统用户角色比例构成</h3>
          <div class="mock-chart-container">
            <div class="mock-bar-wrap">
              <div class="mock-label">学生</div>
              <div class="mock-bar" style="width: 85%; background: var(--primary-color);"></div>
              <div class="mock-val">3,205</div>
            </div>
            <div class="mock-bar-wrap">
              <div class="mock-label">教师</div>
              <div class="mock-bar" style="width: 12%; background: var(--success-color);"></div>
              <div class="mock-val">280</div>
            </div>
            <div class="mock-bar-wrap">
              <div class="mock-label">管理员</div>
              <div class="mock-bar" style="width: 3%; background: var(--warning-color);"></div>
              <div class="mock-val">17</div>
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
      <h3 style="margin-top:0;">系统服务运行监控 (Health Check)</h3>
      <el-table :data="servers" class="custom-table mt-4" border="false">
        <el-table-column prop="node" label="服务组件名称" width="220" />
        <el-table-column prop="cpu" label="CPU使用率" width="150">
          <template #default="scope">
            <el-progress :percentage="scope.row.cpu" :color="customColors" />
          </template>
        </el-table-column>
        <el-table-column prop="ram" label="内存占用率" width="150">
          <template #default="scope">
            <el-progress :percentage="scope.row.ram" :color="customColors" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const stats = ref({ totalUsers: 3502, activeExams: 8, totalQuestions: 124592 })

const fetchDashboardData = async () => {
  try {
    const res = await request.get('/admin/dashboard')
    stats.value.totalUsers = res.totalUsers || 0
    stats.value.activeExams = res.activeExams || 0
    stats.value.totalQuestions = res.totalQuestions || 0
  } catch(e) {
    console.error('Failed to fetch dashboard data', e)
  }
}

onMounted(() => {
  fetchDashboardData()
})

const customColors = [
  { color: '#10b981', percentage: 40 },
  { color: '#f59e0b', percentage: 70 },
  { color: '#ef4444', percentage: 90 },
]

const servers = ref([
  { node: 'Redis 分布式缓存节点', cpu: 32, ram: 45, status: '正常' },
  { node: 'MySQL 核心数据库 (Master)', cpu: 65, ram: 82, status: '正常' },
  { node: '消息队列与异步服务 (MQ)', cpu: 15, ram: 28, status: '正常' },
])
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

:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; }
</style>
