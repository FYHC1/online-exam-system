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
            <el-option v-for="term in availableTerms" :key="term.name" :label="term.name" :value="term.name" />
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
const availableTerms = ref([])
const termSettings = ref([])
const STUDY_YEARS = 4

const avgScore = computed(() => {
  if (scoreRecords.value.length === 0) return 0;
  const total = scoreRecords.value.reduce((sum, r) => sum + r.score, 0);
  return total / scoreRecords.value.length;
})

const extractEnrollmentYear = (user = {}) => {
  const candidates = [user.enrollmentYear, user.grade, user.username]
  for (const item of candidates) {
    const match = String(item || '').match(/20\d{2}/)
    if (match) return Number(match[0])
  }
  return null
}

const loadEnrollmentYear = async () => {
  const localUser = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const localYear = extractEnrollmentYear(localUser)
  if (localYear) return localYear

  try {
    const profile = await request.get('/profile/me')
    const mergedUser = { ...localUser, ...profile }
    localStorage.setItem('userInfo', JSON.stringify(mergedUser))
    return extractEnrollmentYear(mergedUser) || new Date().getFullYear()
  } catch {
    return new Date().getFullYear()
  }
}

const buildStudyTerms = (enrollmentYear) => {
  const terms = []
  for (let year = enrollmentYear; year < enrollmentYear + STUDY_YEARS; year++) {
    terms.push({ name: `${year}-${year + 1} 学年第一学期`, range: [`${year}-09-01`, `${year + 1}-01-31`] })
    terms.push({ name: `${year}-${year + 1} 学年第二学期`, range: [`${year + 1}-02-01`, `${year + 1}-08-31`] })
  }
  return terms
}

const filterStudyTerms = (terms, enrollmentYear) => {
  const graduationStartYear = enrollmentYear + STUDY_YEARS - 1
  return terms.filter(term => {
    const startYear = Number(String(term.name).match(/^(20\d{2})-/)?.[1])
    return !Number.isNaN(startYear) && startYear >= enrollmentYear && startYear <= graduationStartYear
  })
}

const fetchRecords = async () => {
  try {
    const res = await request.get('/student/records')
    scoreRecords.value = res.filter(r => r.record.status === 'finished').map(r => {
      const recordDate = r.examStartTime || r.record.createTime || null
      let termVal = resolveTermByDate(recordDate, r.examTitle)
      
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

const loadAvailableTerms = async () => {
  const enrollmentYear = await loadEnrollmentYear()
  try {
    const termData = await request.get('/profile/terms')
    const terms = termData.terms || []
    termSettings.value = terms
    availableTerms.value = filterStudyTerms(terms, enrollmentYear)
  } catch {
    const fallbackTerms = buildStudyTerms(enrollmentYear)
    availableTerms.value = fallbackTerms
    termSettings.value = fallbackTerms
  }
}

const resolveTermByDate = (dateValue, title) => {
  const rawDateText = typeof dateValue === 'string' ? dateValue.slice(0, 10) : ''
  const date = dateValue ? new Date(dateValue) : null
  if (rawDateText || (date && !Number.isNaN(date.getTime()))) {
    const dateText = rawDateText || [
      date.getFullYear(),
      String(date.getMonth() + 1).padStart(2, '0'),
      String(date.getDate()).padStart(2, '0')
    ].join('-')
    const matchedTerm = termSettings.value.find(term => {
      if (!term.range || term.range.length < 2) return false
      return dateText >= term.range[0] && dateText <= term.range[1]
    })
    if (matchedTerm) {
      return matchedTerm.name
    }
  }

  const match = title ? title.match(/^(.+?学期)/) : null
  if (match) return match[1]

  if (date && !Number.isNaN(date.getTime())) {
    const year = date.getFullYear()
    const month = date.getMonth() + 1
    const academicStartYear = month >= 9 ? year : year - 1
    const termName = (month >= 9 || month === 1) ? '第一学期' : '第二学期'
    return `${academicStartYear}-${academicStartYear + 1} 学年${termName}`
  }

  return '未划分学期'
}

onMounted(async () => {
  await loadAvailableTerms()
  await fetchRecords()
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
