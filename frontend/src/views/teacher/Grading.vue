<template>
  <div class="grading-center">
    <!-- View 1: Subject Selection -->
    <div v-if="!currentSubject" class="subject-view">
      <div class="glass-card mb-4" style="padding: 20px;">
        <h2>试卷批阅</h2>
        <p style="color: var(--text-secondary); margin-top: 8px;">按科目批阅学生提交的试卷内容（包含主观题批改及历史批阅记录查询）。</p>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="subject in subjects" :key="subject.id">
          <div class="glass-card subject-card" @click="enterSubjectGrading(subject)">
            <div class="icon-wrap" :style="{ background: subject.color }">
              <i :class="subject.icon"></i>
            </div>
            <div class="subject-info">
              <h3>{{ subject.name }}</h3>
              <p>待批阅试卷: <strong class="text-danger">{{ subject.paperCount }}</strong> 份</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- View 2: Detailed Grading Pool -->
    <div v-else class="detail-view">
      <div class="glass-card mb-4 top-bar">
        <el-button icon="el-icon-back" circle @click="currentSubject = null" />
        <h2 style="margin: 0;">{{ currentSubject.name }} - 试卷批阅</h2>
      </div>

      <!-- Filters -->
      <div class="glass-card filter-bar mb-4" style="padding: 20px 20px 0 20px;">
        <el-form :inline="true" :model="filters">
          <el-form-item label="学期：">
            <el-select v-model="filters.term" placeholder="全部学期" clearable style="width: 150px">
              <el-option v-for="term in termOptions" :key="term" :label="term" :value="term" />
            </el-select>
          </el-form-item>
          <el-form-item label="批阅状态：">
            <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 150px">
              <el-option label="待批阅" value="待批阅" />
              <el-option label="已批阅" value="已批阅" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级：">
            <el-select v-model="filters.grade" placeholder="全部年级" clearable style="width: 150px">
              <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级：">
            <el-select v-model="filters.class" placeholder="全部班级" clearable style="width: 150px">
              <el-option v-for="className in classOptions" :key="className" :label="className" :value="className" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 未批改列表 -->
      <div class="glass-card p-4">
        <el-table :data="filteredPapers" class="custom-table">
          <el-table-column prop="paperId" label="答卷ID" width="180" />
          <el-table-column prop="examName" label="所属考试" show-overflow-tooltip />
          <el-table-column prop="studentClass" label="学生班级" width="140" />
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="objectiveScore" label="客观题得分" width="120" align="center">
            <template #default="scope">
              <span style="font-weight: 600; color: var(--primary-color);">{{ scope.row.objectiveScore }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === '待批阅' ? 'danger' : (scope.row.status === '异常结束' ? 'warning' : 'success')">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" plain @click="startGrading(scope.row)">
                {{ getActionLabel(scope.row) }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination background layout="prev, pager, next, total" :total="filteredPapers.length" />
        </div>
      </div>
    </div>

    <el-dialog v-model="gradingVisible" :title="currentRecordStatus === '已批阅' ? '批阅结果复核' : '主观题评阅'" width="60%">
      <div v-if="!subjectiveAnswers.length" style="text-align:center; padding:40px; color:var(--text-secondary);">
        该试卷暂无需要手工批改的主观题（简答/填空），客观题由系统自动判分。
      </div>
      <div v-for="(item, idx) in subjectiveAnswers" :key="item.detailId" class="grade-item glass-container mb-4">
        <h4>第{{ idx+1 }}题（{{ item.questionType }} / 满分{{ item.maxScore }}分）</h4>
        <p class="question-text">{{ item.questionTitle }}</p>
        <div class="student-answer">
          <div class="ans-label">学生作答：</div>
          <p>{{ item.studentAnswer || '（未作答）' }}</p>
        </div>
        <div class="student-answer" style="margin-top:8px; border-left-color: var(--success-color);">
          <div class="ans-label">参考答案：</div>
          <p>{{ item.referenceAnswer }}</p>
        </div>
        <div class="grade-input mt-4">
          <span style="font-weight: 600; margin-right: 15px;">教师赋分：</span>
          <el-input-number v-model="currentScores[item.detailId]" :min="0" :max="item.maxScore" />
          <span style="color:var(--text-secondary); margin-left:10px;">/ {{ item.maxScore }} 分</span>
        </div>
        <div v-if="currentRecordStatus === '已批阅'" style="margin-top: 10px; color: var(--text-secondary); font-size: 13px;">
          原分数 / 修改后分数：
          <strong>{{ originalScores[item.detailId] ?? 0 }}</strong>
          /
          <strong :style="{ color: currentScores[item.detailId] !== originalScores[item.detailId] ? 'var(--warning-color)' : 'var(--text-primary)' }">
            {{ currentScores[item.detailId] ?? 0 }}
          </strong>
        </div>
      </div>
      <div v-if="subjectiveAnswers.length && currentRecordStatus === '已批阅'" style="margin-top: 12px; text-align: right; color: var(--text-secondary); font-size: 14px;">
        总主观题分：
        <strong>{{ originalSubjectiveTotal }}</strong>
        /
        <strong :style="{ color: currentSubjectiveTotal !== originalSubjectiveTotal ? 'var(--warning-color)' : 'var(--text-primary)' }">
          {{ currentSubjectiveTotal }}
        </strong>
      </div>
      <template #footer>
        <el-button @click="gradingVisible = false">关闭</el-button>
        <el-button v-if="subjectiveAnswers.length" type="primary" @click="submitGrade">
          {{ currentRecordStatus === '已批阅' ? '保存修改' : '提交批阅结果' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const currentSubject = ref(null)
const filters = ref({ term: '', status: '', grade: '', class: '' })
const gradingVisible = ref(false)
const currentRecordId = ref(null)
const currentRecordStatus = ref('待批阅')
const subjectiveAnswers = ref([])
const currentScores = ref({})
const originalScores = ref({})

const subjectPalette = [
  { icon: 'el-icon-odometer', color: 'rgba(56, 189, 248, 0.2)' },
  { icon: 'el-icon-chat-line-round', color: 'rgba(250, 204, 21, 0.2)' },
  { icon: 'el-icon-monitor', color: 'rgba(16, 185, 129, 0.2)' },
  { icon: 'el-icon-connection', color: 'rgba(168, 85, 247, 0.2)' }
]
const subjects = ref([])
const managedClasses = ref([])

const allPapers = ref([])
const gradingLoading = ref(false)

const getClassGrade = (cls) => cls?.grade || (cls?.createTime ? `${new Date(cls.createTime).getFullYear()}级` : '')
const termOptions = computed(() => [...new Set(allPapers.value.map(item => item.term).filter(Boolean))])
const gradeOptions = computed(() => [...new Set(managedClasses.value.map(getClassGrade).filter(Boolean))])
const classOptions = computed(() => [...new Set(managedClasses.value.map(item => item.className).filter(Boolean))])

const getStudentGrade = (className) => {
  const cls = managedClasses.value.find(item => item.className === className)
  return getClassGrade(cls)
}

const resolveTermByDate = (dateValue, title) => {
  const titleText = String(title || '')
  const titleTermMatch = titleText.match(/(20\d{2})-(20\d{2})\s*(?:学年)?\s*(第[一二两]学期|第一学期|第二学期)/)
  if (titleTermMatch) {
    const termName = titleTermMatch[3].replace('两', '二')
    return `${titleTermMatch[1]}-${titleTermMatch[2]} 学年${termName}`
  }

  const shortTermMatch = titleText.match(/(\d{2})-(\d{2})\s*(?:学年)?\s*(第[一二两]学期|第一学期|第二学期)/)
  if (shortTermMatch) {
    const startYear = `20${shortTermMatch[1]}`
    const endYear = `20${shortTermMatch[2]}`
    const termName = shortTermMatch[3].replace('两', '二')
    return `${startYear}-${endYear} 学年${termName}`
  }

  const date = dateValue ? new Date(dateValue) : null
  if (!date || Number.isNaN(date.getTime())) return '未划分学期'
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const startYear = month >= 9 ? year : year - 1
  const termName = (month >= 9 || month === 1) ? '第一学期' : '第二学期'
  return `${startYear}-${startYear + 1} 学年${termName}`
}

const loadManagedSubjects = async () => {
  const profile = await request.get('/profile/me')
  managedClasses.value = profile.managedClasses || []
  subjects.value = (profile.subjects || []).map((name, index) => ({
    id: index + 1,
    name,
    paperCount: 0,
    ...subjectPalette[index % subjectPalette.length]
  }))
}

const originalSubjectiveTotal = computed(() => Object.values(originalScores.value).reduce((sum, score) => sum + (score || 0), 0))
const currentSubjectiveTotal = computed(() => Object.values(currentScores.value).reduce((sum, score) => sum + (score || 0), 0))

const matchSubjectExam = (exam, subject) => {
  if (!exam || !subject) return false
  const matchTag = subject.name.substring(0, 2)
  return exam.subject === subject.name || String(exam.title || '').includes(matchTag)
}

const refreshSubjectCounts = () => {
  subjects.value = subjects.value.map(subject => ({
    ...subject,
    paperCount: allPapers.value.filter(p => p.subjectId === subject.id && p.status === '待批阅').length
  }))
}

const buildPaperRows = async (subject, exams) => {
  const subjectExams = exams.filter(exam => matchSubjectExam(exam, subject))
  const rows = []
  for (let exam of subjectExams) {
    const list = await request.get(`/teacher/grading/list?examId=${exam.examId}`)
    for (let r of list) {
      rows.push({
        recordId: r.record.recordId,
        paperId: 'P' + String(r.record.recordId).padStart(4, '0'),
        term: resolveTermByDate(exam.startTime, exam.title),
        subjectId: subject.id,
        examName: exam.title,
        studentClass: r.studentClass,
        studentGrade: getStudentGrade(r.studentClass),
        studentName: r.studentName,
        objectiveScore: r.record.objectiveScore || 0,
        status: r.record.status === 'finished' ? '已批阅' : (r.record.status === 'abnormal' ? '异常结束' : '待批阅'),
        hasSubjective: !!r.hasSubjective
      })
    }
  }
  return rows
}

const fetchAllGrading = async () => {
  if (!subjects.value.length) return
  gradingLoading.value = true
  try {
    const exams = await request.get('/teacher/exams')
    const rows = []
    for (const subject of subjects.value) {
      rows.push(...await buildPaperRows(subject, exams))
    }
    allPapers.value = rows
    refreshSubjectCounts()
  } catch (e) {
    ElMessage.error('获取批阅列表失败')
  } finally {
    gradingLoading.value = false
  }
}

const fetchGrading = async () => {
  if (!currentSubject.value) return;
  try {
    const exams = await request.get('/teacher/exams')
    const currentRows = await buildPaperRows(currentSubject.value, exams)
    allPapers.value = [
      ...allPapers.value.filter(item => item.subjectId !== currentSubject.value.id),
      ...currentRows
    ]
    refreshSubjectCounts()
  } catch(e) {
    ElMessage.error('获取批阅列表失败')
  }
}

const filteredPapers = computed(() => {
  if (!currentSubject.value) return []
  return allPapers.value.filter(p => {
    return p.subjectId === currentSubject.value.id &&
           (!filters.value.term || p.term === filters.value.term) &&
           (!filters.value.status || p.status === filters.value.status) &&
           (!filters.value.grade || p.studentGrade === filters.value.grade) &&
           (!filters.value.class || p.studentClass === filters.value.class)
  })
})

const getActionLabel = (row) => {
  if (row.status === '异常结束') {
    return '异常记录'
  }
  if (row.status === '待批阅') {
    return row.hasSubjective ? '批阅试卷' : '查看结果'
  }
  return '查看批阅'
}

const enterSubjectGrading = (subj) => {
  currentSubject.value = subj
  filters.value = { term: '', status: '', grade: '', class: '' }
  if (!allPapers.value.some(item => item.subjectId === subj.id)) {
    fetchGrading()
  }
}

onMounted(async () => {
  await loadManagedSubjects()
  await fetchAllGrading()
})

const startGrading = async (row) => {
  currentRecordId.value = row.recordId
  currentRecordStatus.value = row.status
  // Load real answer details for this record
  try {
    const details = await request.get(`/teacher/grading/answers?recordId=${row.recordId}`)
    subjectiveAnswers.value = details
    currentScores.value = {}
    originalScores.value = {}
    details.forEach(d => {
      const score = d.score != null ? d.score : 0
      currentScores.value[d.detailId] = score
      originalScores.value[d.detailId] = score
    })
  } catch (e) {
    subjectiveAnswers.value = []
    originalScores.value = {}
    ElMessage.warning('加载答题详情失败，请重试')
  }
  gradingVisible.value = true
}

const submitGrade = async () => {
  try {
    const scores = Object.entries(currentScores.value).map(([detailId, score]) => ({
      detailId: parseInt(detailId),
      score: score
    }))
    
    await request.post('/teacher/grading/submit', { 
      recordId: currentRecordId.value, 
      scores: scores
    })
    
    gradingVisible.value = false
    ElMessage.success(currentRecordStatus.value === '已批阅' ? '修改后的成绩已保存，并同步到学生成绩中心！' : '批阅完成，成绩已保存！')
    fetchGrading()
  } catch(e) {
    ElMessage.error('批阅保存失败')
  }
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 24px; }
.mt-4 { margin-top: 20px; }

.text-danger { color: var(--danger-color); }
.top-bar { padding: 20px; display: flex; align-items: center; gap: 16px; }

.subject-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 20px;
}

.subject-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  border-color: var(--primary-color);
}

.icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--primary-color);
}

.subject-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.subject-info p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

:deep(.filter-bar) { padding: 20px 20px 0 20px; }
:deep(.el-form-item) { margin-bottom: 20px; }
:deep(.el-table) { background: transparent !important; border:none; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }

/* 批阅弹窗样式 */
:deep(.el-overlay) { background-color: rgba(0,0,0,0.6); backdrop-filter: blur(5px); }
:deep(.glass-dialog) { background: var(--glass-bg); backdrop-filter: blur(12px); border-radius: 12px; }

.grade-item { padding: 20px; border-radius: 8px; }
.question-text { font-size: 16px; margin: 10px 0; font-weight: 500; }
.student-answer { background: rgba(0,0,0,0.03); padding: 15px; border-radius: 6px; border-left: 3px solid var(--primary-color); }
.ans-label { color: var(--text-secondary); font-size: 13px; margin-bottom: 5px; }
.grade-input { display: flex; align-items: center; }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px dashed var(--glass-border);
}
</style>
