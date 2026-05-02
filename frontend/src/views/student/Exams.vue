<template>
  <div class="exams-container">
    <div class="glass-card filter-bar mb-4">
      <el-form :inline="true" :model="filters">
        <el-form-item label="学期：">
          <el-select v-model="filters.term" placeholder="选择学期">
            <el-option label="全部" value="" />
            <el-option v-for="term in availableTerms" :key="term.name" :label="term.name" :value="term.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目：">
          <el-select v-model="filters.subject" placeholder="选择科目">
            <el-option label="全部" value="" />
            <el-option v-for="subject in availableSubjects" :key="subject" :label="subject" :value="subject" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试状态：">
          <el-select v-model="filters.status" placeholder="选择状态">
            <el-option label="全部" value="" />
            <el-option label="未参加" value="未参加" />
            <el-option label="待批阅" value="待批阅" />
            <el-option label="已批阅" value="已批阅" />
            <el-option label="异常结束" value="异常结束" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card content-card">
      <div class="panel-header">
        <h3>考务中心列表</h3>
      </div>
      <el-table :data="paginatedExams" style="width: 100%" class="custom-table" border="false">
        <el-table-column label="考试名称" show-overflow-tooltip>
          <template #default="scope">
             <!-- If title contains subject like 《Java》, maybe we only show the part after it, but let's just keep full title for clarity and fix other columns -->
             {{ scope.row.title.replace(/^.+?学期/, '').trim() }}
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="所属科目" width="120" />
        <el-table-column prop="term" label="学期" width="160" />
        <el-table-column prop="time" label="起止时间" width="280" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.status === '未参加'" type="primary" size="small" @click="$router.push(`/student/exam-room/${scope.row.id}`)">进入考试</el-button>
            <el-button v-else-if="scope.row.status === '待批阅'" type="warning" size="small" plain disabled>待批阅</el-button>
            <el-button v-else-if="scope.row.status === '已批阅'" type="success" size="small" plain @click="$router.push('/student/scores')">查看成绩</el-button>
            <el-button v-else-if="scope.row.status === '异常结束'" type="danger" size="small" plain disabled>异常结束</el-button>
          </template>
        </el-table-column>
      </el-table>
        <div class="pagination-container">
          <el-pagination background layout="prev, pager, next, total" :total="paginatedExams.length" />
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const filters = ref({ term: '', subject: '', status: '' })

const allExams = ref([])
const availableTerms = ref([])
const termSettings = ref([])
const STUDY_YEARS = 4

const availableSubjects = computed(() => [...new Set(allExams.value.map(item => item.subject).filter(Boolean))])

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

  const match = title?.match(/^(.+?学期)/)
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

const loadExams = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const classId = userInfo.classId || 1
    
    // Fetch available exams
    const exams = await request.get(`/student/exams?classId=${classId}`)
    
    // Fetch records to determine status
    const recordsData = await request.get('/student/records')
    const recordMap = {}
    recordsData.forEach(item => {
      recordMap[item.record.examId] = item.record
    })
    
    // Merge data
    allExams.value = exams.map(exam => {
      const rec = recordMap[exam.examId]
      let statusText = '未参加'
      if (rec) {
        if (rec.status === 'grading' || rec.status === 'pending') statusText = '待批阅'
        else if (rec.status === 'finished') statusText = '已批阅'
        else if (rec.status === 'abnormal') statusText = '异常结束'
      }
      
      const formatTime = (isoString) => {
        if (!isoString) return ''
        return isoString.replace('T', ' ').substring(0, 16)
      }

      const extractSubjectFromTitle = (title) => {
        const match = title.match(/《(.+?)》/)
        return match ? match[1] : '通用'
      }
      
        return {
          id: exam.examId,
          title: exam.title,
          subject: (exam.subject && exam.subject !== '通用') ? exam.subject : extractSubjectFromTitle(exam.title),
          term: resolveTermByDate(exam.startTime, exam.title),
          time: `${formatTime(exam.startTime)} ~ ${formatTime(exam.endTime)}`,
          status: statusText
        }
    })
  } catch (error) {
    ElMessage.error('无法加载考试列表')
    console.error(error)
  }
}

onMounted(async () => {
  await loadAvailableTerms()
  await loadExams()
})

const paginatedExams = computed(() => {
  return allExams.value.filter(exam => {
    // Term filter: map select values to keywords
      const matchTerm = !filters.value.term || exam.term === filters.value.term
      const matchSubject = !filters.value.subject || exam.subject === filters.value.subject
    
    // Status filter
    let matchStatus = true
    if (filters.value.status) {
      matchStatus = exam.status === filters.value.status
    }
    
    return matchTerm && matchSubject && matchStatus
  })
})

const getStatusTag = (status) => {
  const map = { '未参加': 'info', '待批阅': 'warning', '已批阅': 'success', '异常结束': 'danger' };
  return map[status] || 'info';
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.filter-bar { padding: 20px 20px 0 20px; }
.content-card { padding: 24px; min-height: 480px; }
.panel-header { margin-bottom: 20px; border-bottom: 2px solid var(--glass-border); padding-bottom: 12px; }
.panel-header h3 { font-size: 18px; font-weight: 600; margin: 0; }

:deep(.el-form-item) { margin-bottom: 20px; }
:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}
</style>
