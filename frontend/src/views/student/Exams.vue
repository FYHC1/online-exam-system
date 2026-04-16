<template>
  <div class="exams-container">
    <div class="glass-card filter-bar mb-4">
      <el-form :inline="true" :model="filters">
        <el-form-item label="学期：">
          <el-select v-model="filters.term" placeholder="选择学期">
            <el-option label="全部" value="" />
            <el-option label="2023-2024 第一学期" value="23-24-1" />
            <el-option label="2022-2023 第二学期" value="22-23-2" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目：">
          <el-select v-model="filters.subject" placeholder="选择科目">
            <el-option label="全部" value="" />
            <el-option label="高等数学" value="math" />
            <el-option label="大学英语" value="english" />
            <el-option label="JavaWeb" value="java" />
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
        <el-form-item>
          <el-button type="primary">筛选查询</el-button>
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
        <el-pagination background layout="prev, pager, next, total" :total="allExams.length" />
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

      // Intelligent parsing for Term
      const extractTerm = (title) => {
        const match = title.match(/^(.+?学期)/)
        return match ? match[1] : '全周期'
      }

      const extractSubjectFromTitle = (title) => {
        const match = title.match(/《(.+?)》/)
        return match ? match[1] : '通用'
      }
      
      return {
        id: exam.examId,
        title: exam.title,
        subject: (exam.subject && exam.subject !== '通用') ? exam.subject : extractSubjectFromTitle(exam.title),
        term: extractTerm(exam.title),
        time: `${formatTime(exam.startTime)} ~ ${formatTime(exam.endTime)}`,
        status: statusText
      }
    })
  } catch (error) {
    ElMessage.error('无法加载考试列表')
    console.error(error)
  }
}

onMounted(() => {
  loadExams()
})

const paginatedExams = computed(() => {
  return allExams.value.filter(exam => {
    // Term filter: map select values to keywords
    let matchTerm = true
    if (filters.value.term) {
      if (filters.value.term === '23-24-1') matchTerm = exam.title.includes('2023-2024') || exam.title.includes('2023')
      else if (filters.value.term === '22-23-2') matchTerm = exam.title.includes('2022-2023') || exam.title.includes('2022')
    }
    
    // Subject filter
    let matchSubject = true
    if (filters.value.subject) {
      if (filters.value.subject === 'math') matchSubject = exam.title.includes('数学')
      else if (filters.value.subject === 'english') matchSubject = exam.title.includes('英语')
      else if (filters.value.subject === 'java') matchSubject = exam.title.toLowerCase().includes('java')
    }
    
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
