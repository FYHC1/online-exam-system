<template>
  <div class="admin-resources">
    <div class="glass-card mb-4 header-card">
      <div>
        <h2>全域资源</h2>
        <p>统一查看平台题库资源与历史试卷快照，便于按学科、所属院系和考试状态进行审计与维护。</p>
      </div>
      <div class="summary-grid">
        <div class="summary-item">
          <div class="summary-value">{{ filteredQuestions.length }}</div>
          <div class="summary-label">当前题目数</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ filteredExams.length }}</div>
          <div class="summary-label">当前试卷数</div>
        </div>
      </div>
    </div>

    <div class="glass-card p-0">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="题库分类" name="categories">
          <div class="pane-body">
            <div class="category-header standalone">
              <div>
                <h3>题库分类</h3>
                <p class="section-desc">按学科查看题库规模，并可一键跳转到对应题库资源列表。</p>
              </div>
              <el-button type="primary" @click="questionDialogVisible = true">新增题目</el-button>
            </div>

            <div class="category-grid">
              <div
                v-for="item in questionCategories"
                :key="item.subject"
                class="category-card category-card-large"
                :class="{ active: qFilters.subject === item.subject && activeTab === 'questions' }"
                @click="openQuestionCategory(item.subject)"
              >
                <div class="category-title-row">
                  <div class="category-title">{{ item.subject }}</div>
                  <el-tag size="small" type="primary">{{ item.types.length }} 类题型</el-tag>
                </div>
                <div class="category-meta">题目总数：{{ item.count }}</div>
                <div class="category-types">包含：{{ item.types.join(' / ') || '暂无题型' }}</div>
                <el-button type="primary" plain size="small" style="margin-top: 14px;">查看该题库</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="题库资源" name="questions">
          <div class="pane-body">
            <div class="resource-toolbar">
              <div>
                <h3>题库资源</h3>
                <p class="section-desc">可按所属学科、录入教师和题目类型筛选当前题库资源。</p>
              </div>
              <el-button type="primary" @click="questionDialogVisible = true">新增题目</el-button>
            </div>

            <el-form :inline="true" :model="qFilters" class="mb-4">
              <el-form-item label="所属学科：">
                <el-select v-model="qFilters.subject" placeholder="全部学科" clearable>
                  <el-option v-for="subject in questionSubjects" :key="subject" :label="subject" :value="subject" />
                </el-select>
              </el-form-item>
              <el-form-item label="录入教师：">
                <el-input v-model="qFilters.creator" placeholder="搜索教师姓名" clearable />
              </el-form-item>
              <el-form-item label="题目类型：">
                <el-select v-model="qFilters.type" placeholder="全部类型" clearable>
                  <el-option v-for="type in questionTypes" :key="type" :label="type" :value="type" />
                </el-select>
              </el-form-item>
            </el-form>

            <el-table :data="pagedQuestions" style="width: 100%" :border="false">
              <el-table-column prop="qcId" label="题目编号" width="160" />
              <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
              <el-table-column prop="type" label="题目类型" width="110">
                <template #default="scope">
                  <el-tag size="small" type="info">{{ scope.row.type }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="subject" label="所属学科" width="140" />
              <el-table-column prop="department" label="所属院系" width="160" />
              <el-table-column prop="creator" label="录入教师" width="120" />
              <el-table-column label="操作" width="180" align="center">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click="previewQuestion(scope.row)">查看详情</el-button>
                  <el-button link type="danger" size="small" @click="archiveQuestion(scope.row)">移出资源池</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                background
                layout="prev, pager, next, total"
                :total="filteredQuestions.length"
                :page-size="pageSize"
                :current-page="questionPage"
                @current-change="questionPage = $event"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="历史试卷" name="exams">
          <div class="pane-body">
            <el-form :inline="true" :model="eFilters" class="mb-4">
              <el-form-item label="试卷名称：">
                <el-input v-model="eFilters.title" placeholder="搜索试卷名称" clearable />
              </el-form-item>
              <el-form-item label="所属院系：">
                <el-select v-model="eFilters.college" placeholder="全部院系" clearable>
                  <el-option label="全部院系" value="" />
                  <el-option v-for="college in examColleges" :key="college" :label="college" :value="college" />
                </el-select>
              </el-form-item>
              <el-form-item label="考试状态：">
                <el-select v-model="eFilters.status" placeholder="全部状态" clearable>
                  <el-option label="未开始" value="未开始" />
                  <el-option label="进行中" value="进行中" />
                  <el-option label="已结束" value="已结束" />
                </el-select>
              </el-form-item>
            </el-form>

            <el-table :data="pagedExams" style="width: 100%" :border="false">
              <el-table-column prop="roomId" label="试卷编号" width="140" />
              <el-table-column prop="title" label="试卷名称" show-overflow-tooltip />
              <el-table-column prop="college" label="所属院系" width="160" />
              <el-table-column prop="status" label="考试状态" width="100">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.status === '进行中' ? 'success' : (scope.row.status === '未开始' ? 'info' : 'warning')">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="questionCount" label="题目数量" width="100" align="center" />
              <el-table-column label="操作" width="220" align="center">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click="previewExam(scope.row)">查看明细</el-button>
                  <el-button link type="warning" size="small" @click="toggleExamArchive(scope.row)">变更状态</el-button>
                  <el-button link type="danger" size="small" @click="removeExam(scope.row)">移出资源池</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                background
                layout="prev, pager, next, total"
                :total="filteredExams.length"
                :page-size="pageSize"
                :current-page="examPage"
                @current-change="examPage = $event"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="questionDialogVisible" title="新增题目" width="560px">
      <el-form :model="questionForm" label-width="90px">
        <el-form-item label="所属学科">
          <el-input v-model="questionForm.subject" placeholder="请输入学科名称" />
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="questionForm.type" placeholder="请选择题目类型" style="width: 100%">
            <el-option label="单选题" value="单选题" />
            <el-option label="多选题" value="多选题" />
            <el-option label="判断题" value="判断题" />
            <el-option label="简答题" value="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input v-model="questionForm.title" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item v-if="questionForm.type !== '简答题'" label="选项">
          <el-input v-model="questionForm.optionsText" type="textarea" :rows="4" placeholder="每行一个选项" />
        </el-form-item>
        <el-form-item label="参考答案">
          <el-input v-model="questionForm.answer" />
        </el-form-item>
        <el-form-item label="难度等级">
          <el-input-number v-model="questionForm.difficulty" :min="1" :max="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion">确认新增</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="questionDetailVisible" title="题目详情" width="620px">
      <div v-if="currentQuestionDetail">
        <p><strong>所属学科：</strong>{{ currentQuestionDetail.subject }}</p>
        <p><strong>题目类型：</strong>{{ currentQuestionDetail.type }}</p>
        <p><strong>题目内容：</strong>{{ currentQuestionDetail.title }}</p>
        <p v-if="currentQuestionDetail.options"><strong>题目选项：</strong>{{ currentQuestionDetail.options }}</p>
        <p><strong>参考答案：</strong>{{ currentQuestionDetail.answer }}</p>
        <p><strong>难度等级：</strong>{{ currentQuestionDetail.difficulty }}</p>
      </div>
    </el-dialog>

    <el-dialog v-model="examDetailVisible" title="试卷详情" width="700px">
      <div v-if="currentExamDetail">
        <p><strong>试卷名称：</strong>{{ currentExamDetail.title }}</p>
        <p><strong>所属学科：</strong>{{ currentExamDetail.subject }}</p>
        <p><strong>考试时间：</strong>{{ formatDateRange(currentExamDetail.startTime, currentExamDetail.endTime) }}</p>
        <p><strong>目标班级：</strong>{{ currentExamDetail.targetClasses || '未指定' }}</p>
        <el-table :data="currentExamDetail.questions || []" style="width: 100%; margin-top: 16px;" :border="false">
          <el-table-column prop="questionId" label="题号" width="90" />
          <el-table-column prop="title" label="题目内容" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="score" label="分值" width="80" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('questions')

const qFilters = ref({ subject: '', creator: '', type: '' })
const eFilters = ref({ title: '', college: '', status: '' })
const pageSize = 10
const questionPage = ref(1)
const examPage = ref(1)

const questionDialogVisible = ref(false)
const questionDetailVisible = ref(false)
const examDetailVisible = ref(false)
const currentQuestionDetail = ref(null)
const currentExamDetail = ref(null)
const questionForm = ref({ subject: '', type: '', title: '', optionsText: '', answer: '', difficulty: 3 })

const allQuestions = ref([])
const allExams = ref([])

const questionSubjects = computed(() => [...new Set(allQuestions.value.map(item => item.subject))])
const questionTypes = computed(() => [...new Set(allQuestions.value.map(item => item.type))])
const examColleges = computed(() => [...new Set(allExams.value.map(item => item.college))])
const questionCategories = computed(() => questionSubjects.value.map(subject => ({
  subject,
  count: allQuestions.value.filter(item => item.subject === subject).length,
  types: [...new Set(allQuestions.value.filter(item => item.subject === subject).map(item => item.type))]
})))

const filteredQuestions = computed(() => {
  return allQuestions.value.filter(item =>
    (!qFilters.value.subject || item.subject === qFilters.value.subject) &&
    (!qFilters.value.creator || item.creator.includes(qFilters.value.creator)) &&
    (!qFilters.value.type || item.type === qFilters.value.type)
  )
})

const pagedQuestions = computed(() => {
  const start = (questionPage.value - 1) * pageSize
  return filteredQuestions.value.slice(start, start + pageSize)
})

const filteredExams = computed(() => {
  return allExams.value.filter(item =>
    (!eFilters.value.title || item.title.includes(eFilters.value.title)) &&
    (!eFilters.value.college || item.college === eFilters.value.college) &&
    (!eFilters.value.status || item.status === eFilters.value.status)
  )
})

const pagedExams = computed(() => {
  const start = (examPage.value - 1) * pageSize
  return filteredExams.value.slice(start, start + pageSize)
})

const openQuestionCategory = (subject) => {
  qFilters.value.subject = subject
  questionPage.value = 1
  activeTab.value = 'questions'
}

const formatDateRange = (startTime, endTime) => {
  if (!startTime || !endTime) return '未设置'
  return `${new Date(startTime).toLocaleString()} - ${new Date(endTime).toLocaleString()}`
}

const previewQuestion = async (row) => {
  try {
    currentQuestionDetail.value = await request.get(`/admin/resources/questions/${row.questionId}`)
    questionDetailVisible.value = true
  } catch (e) {
    ElMessage.error('获取题目详情失败')
  }
}

const archiveQuestion = async (row) => {
  ElMessageBox.confirm(`确定将题目 ${row.qcId} 移出全域资源池吗？`, '资源处理确认', { type: 'warning' })
    .then(async () => {
      await request.delete(`/admin/resources/questions/${row.questionId}`)
      ElMessage.success('题目已移出资源池')
      fetchResources()
    })
    .catch(() => {})
}

const previewExam = async (row) => {
  try {
    currentExamDetail.value = await request.get(`/admin/resources/exams/${row.examId}`)
    examDetailVisible.value = true
  } catch (e) {
    ElMessage.error('获取试卷详情失败')
  }
}

const toggleExamArchive = async (row) => {
  const nextStatus = row.status === '未开始' ? '进行中' : (row.status === '进行中' ? '已结束' : '未开始')
  try {
    await request.put(`/admin/resources/exams/${row.examId}/status?status=${encodeURIComponent(nextStatus)}`)
    ElMessage.success(`试卷状态已更新为${nextStatus}`)
    fetchResources()
  } catch (e) {
    ElMessage.error('试卷状态更新失败')
  }
}

const removeExam = async (row) => {
  ElMessageBox.confirm(`确定将试卷 ${row.roomId} 移出资源池吗？`, '资源处理确认', { type: 'warning' })
    .then(async () => {
      await request.delete(`/admin/resources/exams/${row.examId}`)
      ElMessage.success('试卷已移出资源池')
      fetchResources()
    })
    .catch(() => {})
}

const submitQuestion = async () => {
  if (!questionForm.value.subject || !questionForm.value.type || !questionForm.value.title || !questionForm.value.answer) {
    ElMessage.error('请完整填写题目信息')
    return
  }

  try {
    await request.post('/admin/resources/questions', {
      subject: questionForm.value.subject,
      type: questionForm.value.type,
      title: questionForm.value.title,
      options: questionForm.value.type === '简答题'
        ? null
        : JSON.stringify(questionForm.value.optionsText.split('\n').map(item => item.trim()).filter(Boolean)),
      answer: questionForm.value.answer,
      difficulty: questionForm.value.difficulty
    })
    ElMessage.success('题目新增成功')
    questionDialogVisible.value = false
    questionForm.value = { subject: '', type: '', title: '', optionsText: '', answer: '', difficulty: 3 }
    fetchResources()
  } catch (e) {
    ElMessage.error('题目新增失败')
  }
}

const fetchResources = async () => {
  try {
    const [questions, exams] = await Promise.all([
      request.get('/admin/resources/questions'),
      request.get('/admin/resources/exams')
    ])
    allQuestions.value = questions
    allExams.value = exams
    questionPage.value = 1
    examPage.value = 1
  } catch (e) {
    ElMessage.error('获取全域资源失败')
  }
}

onMounted(() => {
  fetchResources()
})
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.header-card {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.header-card p {
  color: var(--text-secondary);
  margin-top: 8px;
}

.summary-grid {
  display: flex;
  gap: 16px;
}

.summary-item {
  min-width: 140px;
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.42);
  border: 1px solid var(--glass-border);
  text-align: center;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.resource-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.category-header.standalone {
  margin-bottom: 20px;
}

.section-desc {
  margin-top: 6px;
  color: var(--text-secondary);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.category-card {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--glass-border);
  background: rgba(255, 255, 255, 0.38);
  cursor: pointer;
  transition: 0.2s ease;
}

.category-card.active,
.category-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.category-card-large {
  min-height: 150px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.category-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.category-title {
  font-weight: 600;
}

.category-meta {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.category-types {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
}

.summary-label {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.pane-body {
  padding: 20px;
}

.custom-tabs { padding: 0; }
:deep(.el-tabs__nav-wrap) { padding: 0 20px; border-bottom: 1px solid var(--glass-border); }
:deep(.el-tabs__item) { height: 50px; line-height: 50px; font-size: 16px; transition: color 0.3s; }
:deep(.el-tabs__item.is-active) { color: var(--primary-color) !important; font-weight: 600; }
:deep(.el-tabs__active-bar) { background-color: var(--primary-color) !important; height: 3px; border-radius: 3px; }
:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0 0 0;
  border-top: 1px dashed var(--glass-border);
  margin-top: 20px;
}
</style>
