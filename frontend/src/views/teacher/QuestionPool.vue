<template>
  <div class="question-pool">
    <!-- View 1: Subject Selection -->
    <div v-if="!currentSubject" class="subject-view">
      <div class="glass-card mb-4" style="padding: 20px;">
        <h2>公共题库管理</h2>
        <p style="color: var(--text-secondary); margin-top: 8px;">请选择教学科目进入对应题库进行统一管理（同科目教师共享题库内容）。</p>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="subject in subjects" :key="subject.id">
          <div class="glass-card subject-card" @click="enterSubjectPool(subject)">
            <div class="icon-wrap" :style="{ background: subject.color }">
              <i :class="subject.icon"></i>
            </div>
            <div class="subject-info">
              <h3>{{ subject.name }}</h3>
              <p>共 {{ subject.count }} 道题</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- View 2: Detailed Question Pool -->
    <div v-else class="detail-view">
      <div class="glass-card mb-4" style="padding: 20px; display: flex; align-items: center; gap: 16px;">
        <el-button icon="el-icon-back" circle @click="currentSubject = null" />
        <h2 style="margin: 0;">{{ currentSubject.name }} - 题库管理</h2>
      </div>

      <div class="glass-card mb-4 filter-bar">
        <el-form :inline="true" :model="filters">
          <el-form-item label="题干搜索">
            <el-input v-model="filters.keyword" placeholder="请输入关键字" clearable />
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="filters.type" placeholder="选择题型" clearable>
              <el-option label="单选题" value="单选题" />
              <el-option label="多选题" value="多选题" />
              <el-option label="判断题" value="判断题" />
              <el-option label="简答题" value="简答题" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="filters.difficulty" placeholder="选择难度" clearable>
              <el-option label="1星" :value="1" />
              <el-option label="2星" :value="2" />
              <el-option label="3星" :value="3" />
              <el-option label="4星" :value="4" />
              <el-option label="5星" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary">查询</el-button>
          </el-form-item>
          <el-form-item style="margin-left: auto; margin-right: 0;">
            <el-button type="success" @click="addQuestion"><i class="el-icon-plus" /> 录入新题</el-button>
            <el-button plain type="primary"><i class="el-icon-upload2" /> Word/Excel 批量导入</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="glass-card">
        <el-table :data="filteredQuestions" style="width: 100%" class="custom-table" border="false">
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="title" label="题干明细" show-overflow-tooltip />
          <el-table-column prop="type" label="题型" width="100">
            <template #default="scope">
              <el-tag size="small" :type="getTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="120">
            <template #default="scope">
              <el-rate v-model="scope.row.difficulty" disabled :max="5" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="editQuestion(scope.row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="deleteQuestion(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination background layout="prev, pager, next, total" :total="filteredQuestions.length" />
        </div>
      </div>
    </div>

    <!-- 题目添加/编辑弹窗 -->
    <el-dialog v-model="questionDialogVisible" :title="isEditing ? '编辑题目' : '录入新题'" width="620px">
      <el-form :model="questionForm" label-width="80px">
        <el-form-item label="题目类型">
          <el-select v-model="questionForm.type" style="width:100%">
            <el-option label="单选题" value="单选题" />
            <el-option label="多选题" value="多选题" />
            <el-option label="判断题" value="判断题" />
            <el-option label="简答题" value="简答题" />
            <el-option label="填空题" value="填空题" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干">
          <el-input type="textarea" :rows="3" v-model="questionForm.title" placeholder="请输入题目内容..." />
        </el-form-item>
        <template v-if="optionTypes.includes(questionForm.type)">
          <el-form-item v-for="(opt, idx) in questionForm.options" :key="idx" :label="`选项 ${String.fromCharCode(65+idx)}`">
            <el-input v-model="questionForm.options[idx]" :placeholder="`选项 ${String.fromCharCode(65+idx)}`" />
          </el-form-item>
        </template>
        <el-form-item label="标准答案">
          <el-input v-model="questionForm.answer" placeholder="单选填A/B/C/D，判断填'正确'或'错误'，简答填参考答案" />
        </el-form-item>
        <el-form-item label="难度系数">
          <el-rate v-model="questionForm.difficulty" :max="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion">{{ isEditing ? '保存修改' : '确认添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const currentSubject = ref(null)
const filters = ref({ keyword: '', type: '', difficulty: '' })

const subjects = ref([
  { id: 1, name: '高等数学', count: 0, icon: 'el-icon-odometer', color: 'rgba(56, 189, 248, 0.2)' },
  { id: 2, name: '大学英语', count: 0, icon: 'el-icon-chat-line-round', color: 'rgba(250, 204, 21, 0.2)' },
  { id: 3, name: 'JavaWeb', count: 0, icon: 'el-icon-monitor', color: 'rgba(16, 185, 129, 0.2)' },
  { id: 4, name: '数据结构', count: 0, icon: 'el-icon-connection', color: 'rgba(168, 85, 247, 0.2)' }
])

const allQuestions = ref([])

const fetchSubjectCounts = async () => {
  try {
    const res = await request.get('/teacher/questions')
    const counts = res.reduce((acc, question) => {
      acc[question.subject] = (acc[question.subject] || 0) + 1
      return acc
    }, {})

    subjects.value = subjects.value.map(subject => ({
      ...subject,
      count: counts[subject.name] || 0
    }))
  } catch (e) {
    console.error('Failed to load subject counts', e)
    ElMessage.error('获取学科统计失败')
  }
}

const fetchQuestions = async () => {
  if (!currentSubject.value) return;
  try {
    const res = await request.get('/teacher/questions', {
      params: { subject: currentSubject.value.name }
    })
    allQuestions.value = res.map(q => ({
      id: q.questionId,
      subjectId: currentSubject.value.id,
      title: q.title,
      type: q.type,
      difficulty: q.difficulty || 3
    }))
  } catch(e) {
    console.error('Failed to load questions', e)
    ElMessage.error('获取题库失败')
  }
}

const filteredQuestions = computed(() => {
  if (!currentSubject.value) return []
  return allQuestions.value.filter(q => {
    return q.subjectId === currentSubject.value.id &&
           (!filters.value.keyword || q.title.includes(filters.value.keyword)) &&
           (!filters.value.type || q.type === filters.value.type) &&
           (!filters.value.difficulty || q.difficulty === filters.value.difficulty)
  })
})

const enterSubjectPool = (subj) => {
  currentSubject.value = subj
  filters.value = { keyword: '', type: '', difficulty: '' }
  fetchQuestions()
}

const getTypeTag = (type) => {
  const map = { '单选题': '', '多选题': 'success', '判断题': 'warning', '简答题': 'danger', '填空题': 'info' }
  return map[type] || ''
}

const deleteQuestion = (id) => {
  ElMessageBox.confirm('确定要删除该题目吗？该科目下所有教师都将受影响。', '警告', { type: 'warning' }).then(async () => {
    try {
      await request.delete(`/teacher/questions/${id}`)
      allQuestions.value = allQuestions.value.filter(q => q.id !== id)
      await fetchSubjectCounts()
      ElMessage.success('题目已成功删除')
    } catch(e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const questionDialogVisible = ref(false)
const questionForm = ref({ questionId: null, subject: '', type: '单选题', title: '', options: ['', '', '', ''], answer: '', difficulty: 3 })
const isEditing = ref(false)
const optionTypes = ['单选题', '多选题', '判断题']

const editQuestion = (row) => {
  isEditing.value = true
  // reload full question from backend to get options & answer
  request.get('/teacher/questions', { params: { subject: currentSubject.value.name, keyword: row.title } }).then(res => {
    const q = res.find(x => x.questionId === row.id)
    if (q) {
      let opts = ['', '', '', '']
      try { opts = JSON.parse(q.options) || opts } catch(e) {}
      questionForm.value = {
        questionId: q.questionId,
        subject: q.subject,
        type: q.type,
        title: q.title,
        options: opts.length ? opts : ['', '', '', ''],
        answer: q.answer || '',
        difficulty: q.difficulty || 3
      }
      questionDialogVisible.value = true
    }
  })
}

const addQuestion = () => {
  isEditing.value = false
  questionForm.value = { questionId: null, subject: currentSubject.value.name, type: '单选题', title: '', options: ['', '', '', ''], answer: '', difficulty: 3 }
  questionDialogVisible.value = true
}

const submitQuestion = async () => {
  if (!questionForm.value.title) {
    ElMessage.error('题干不能为空')
    return
  }
  try {
    const payload = {
      questionId: questionForm.value.questionId,
      subject: currentSubject.value.name,
      type: questionForm.value.type,
      title: questionForm.value.title,
      options: optionTypes.includes(questionForm.value.type) ? JSON.stringify(questionForm.value.options.filter(o => o)) : null,
      answer: questionForm.value.answer,
      difficulty: questionForm.value.difficulty
    }
    
    if (isEditing.value) {
      await request.put('/teacher/questions', payload)
      ElMessage.success('题目修改成功')
    } else {
      await request.post('/teacher/questions', payload)
      ElMessage.success('题目添加成功')
    }
    questionDialogVisible.value = false
    await Promise.all([fetchQuestions(), fetchSubjectCounts()])
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchSubjectCounts()
})
</script>

<!-- Dialog already handled by teleport to body, just close the extra template -->

<style scoped>
.mb-4 { margin-bottom: 20px; }
.filter-bar { padding: 20px 20px 0 20px; }

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

:deep(.el-form-item) { margin-bottom: 20px; }
:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px dashed var(--glass-border);
}
</style>
