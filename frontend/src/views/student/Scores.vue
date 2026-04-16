<template>
  <div class="scores-container">
    <div class="glass-card p-4 mb-4 header-card">
      <div>
        <h2>成绩中心</h2>
        <p>查看历史考试分析报告与详细雷达图</p>
      </div>
      <!-- 占位示意图表 - 这里我们用纯CSS画一个柱状图示意，Teacher端会引入ECharts -->
      <div class="score-summary">
        <div class="stat-box">
          <div class="val text-primary">{{ isNaN(avgScore) ? 0 : avgScore.toFixed(1) }}</div>
          <div class="label">平均分</div>
        </div>
        <div class="stat-box">
          <div class="val text-success">{{ scoreRecords.length }}</div>
          <div class="label">已出成绩总数</div>
        </div>
      </div>
    </div>

    <div class="glass-card filter-bar mb-4" style="padding: 20px 20px 0 20px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="选择学期：">
          <el-select v-model="filters.term" placeholder="全部学期">
            <el-option label="全部学期" value="" />
            <el-option label="2023-2024 第一学期" value="2023-2024 第一学期" />
            <el-option label="2022-2023 第二学期" value="2022-2023 第二学期" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card p-4">
      <el-timeline>
        <el-timeline-item v-for="(record, index) in filteredRecords" :key="index" :timestamp="record.date" placement="top" :type="record.score >= record.passScore ? 'success' : 'danger'">
          <el-card class="timeline-card glass-container">
            <h4>{{ record.examName }}</h4>
            <div class="score-row">
              <span class="final-score" :class="{ 'pass': record.score >= record.passScore }">{{ record.score }} 分</span>
              <span class="detail">及格线：{{ record.passScore }} 分 | 班级排名：{{ record.rank }}</span>
              <el-button type="primary" link style="margin-left: auto;" @click="viewDetail(record)">查看答卷明细 <i class="el-icon-arrow-right"></i></el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const filters = ref({ term: '' })
const scoreRecords = ref([])

const avgScore = computed(() => {
  if (scoreRecords.value.length === 0) return 0;
  const total = scoreRecords.value.reduce((sum, r) => sum + r.score, 0);
  return total / scoreRecords.value.length;
})

const fetchRecords = async () => {
  try {
    const res = await request.get('/student/records')
    scoreRecords.value = res.filter(r => r.record.status === 'finished').map(r => {
      let termVal = '2023-2024 第一学期'
      if (r.examTitle && r.examTitle.includes('2022')) termVal = '2022-2023 第二学期'
      else {
        const match = r.examTitle ? r.examTitle.match(/^(.+?学期)/) : null
        if (match) termVal = match[1]
      }
      
      const cleanTitle = r.examTitle ? r.examTitle.replace(/^.+?学期/, '').trim() : '未知考试'

      return {
        examName: cleanTitle,
        term: termVal,
        date: new Date(r.record.createTime || Date.now()).toLocaleDateString(),
        score: r.record.totalScore || 0,
        passScore: r.passScore || 60,
        rank: '暂未排名'
      }
    })
  } catch(e) {
    ElMessage.error('无法加载成绩数据')
  }
}

onMounted(() => {
  fetchRecords()
})

const filteredRecords = computed(() => {
  return scoreRecords.value.filter(r => !filters.value.term || r.term === filters.value.term)
})

const viewDetail = (record) => {
  ElMessage.success(`正在为您生成《${record.examName}》的答卷详情与错题解析报告...`)
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 24px; }
.header-card { display: flex; justify-content: space-between; align-items: center; }
.header-card p { color: var(--text-secondary); margin-top: 8px; }

.score-summary { display: flex; gap: 30px; }
.stat-box { text-align: center; }
.stat-box .val { font-size: 32px; font-weight: 700; line-height: 1; margin-bottom: 4px; }
.stat-box .label { font-size: 14px; color: var(--text-secondary); }
.text-primary { color: var(--primary-color); }
.text-success { color: var(--success-color); }

.timeline-card { border: none !important; box-shadow: none !important; background: rgba(255,255,255,0.3) !important; }
.timeline-card h4 { margin: 0 0 10px 0; font-size: 16px; }
.score-row { display: flex; align-items: center; gap: 15px; }
.final-score { font-size: 24px; font-weight: bold; color: var(--danger-color); }
.final-score.pass { color: var(--success-color); }
.detail { color: var(--text-secondary); font-size: 14px; }
</style>
