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
            <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              accept=".xlsx,.xls"
              :on-change="handleExcelImport"
            >
              <el-button plain type="primary" :loading="importing"><i class="el-icon-upload2" /> Excel模板批量导入</el-button>
            </el-upload>
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
        <template v-if="choiceTypes.includes(questionForm.type)">
          <el-form-item label="选项设置">
            <div class="option-editor">
              <div v-for="(opt, idx) in questionForm.options" :key="idx" class="option-row">
                <span class="option-letter">{{ getOptionLetter(idx) }}</span>
                <el-input v-model="questionForm.options[idx]" :placeholder="`请输入选项 ${getOptionLetter(idx)} 内容`" />
                <el-button v-if="questionForm.options.length > 2" link type="danger" @click="removeOption(idx)">删除</el-button>
              </div>
              <el-button type="primary" plain size="small" @click="addOption">新增选项</el-button>
            </div>
          </el-form-item>
          <el-form-item label="标准答案">
            <el-radio-group v-if="questionForm.type === '单选题'" v-model="questionForm.answer">
              <el-radio v-for="(_, idx) in questionForm.options" :key="idx" :value="getOptionLetter(idx)">{{ getOptionLetter(idx) }}</el-radio>
            </el-radio-group>
            <el-checkbox-group v-else v-model="questionForm.multiAnswer">
              <el-checkbox v-for="(_, idx) in questionForm.options" :key="idx" :value="getOptionLetter(idx)">{{ getOptionLetter(idx) }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </template>

        <template v-else-if="questionForm.type === '判断题'">
          <el-form-item label="标准答案">
            <el-radio-group v-model="questionForm.answer">
              <el-radio value="正确">正确</el-radio>
              <el-radio value="错误">错误</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <template v-else-if="questionForm.type === '填空题'">
          <el-form-item label="标准答案">
            <el-input v-model="questionForm.answer" placeholder="请输入填空题答案；多个答案可用 / 分隔" />
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="参考答案">
            <el-input v-model="questionForm.answer" type="textarea" :rows="4" placeholder="请输入简答题参考答案或评分要点" />
          </el-form-item>
        </template>
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
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import request from '@/utils/request'

const currentSubject = ref(null)
const filters = ref({ keyword: '', type: '', difficulty: '' })

const subjectPalette = [
  { icon: 'el-icon-odometer', color: 'rgba(56, 189, 248, 0.2)' },
  { icon: 'el-icon-chat-line-round', color: 'rgba(250, 204, 21, 0.2)' },
  { icon: 'el-icon-monitor', color: 'rgba(16, 185, 129, 0.2)' },
  { icon: 'el-icon-connection', color: 'rgba(168, 85, 247, 0.2)' }
]
const subjects = ref([])

const allQuestions = ref([])
const importing = ref(false)

const loadManagedSubjects = async () => {
  const profile = await request.get('/profile/me')
  subjects.value = (profile.subjects || []).map((name, index) => ({
    id: index + 1,
    name,
    count: 0,
    ...subjectPalette[index % subjectPalette.length]
  }))
}

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
const createQuestionForm = (subject = '') => ({
  questionId: null,
  subject,
  type: '单选题',
  title: '',
  options: ['', '', '', ''],
  answer: '',
  multiAnswer: [],
  difficulty: 3
})
const questionForm = ref(createQuestionForm())
const isEditing = ref(false)
const hydratingQuestionForm = ref(false)
const choiceTypes = ['单选题', '多选题']
const optionTypes = ['单选题', '多选题', '判断题']

const getOptionLetter = (index) => String.fromCharCode(65 + index)
const optionColumns = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

const cleanCell = (value) => String(value ?? '').replace(/\u00a0/g, ' ').trim()

const normalizeDifficulty = (value) => {
  const text = cleanCell(value)
  const map = { 简单: 1, 容易: 1, 中等: 3, 一般: 3, 困难: 5, 难: 5 }
  if (map[text]) return map[text]
  const numeric = Number(text.replace('星', ''))
  return Number.isFinite(numeric) && numeric >= 1 && numeric <= 5 ? numeric : 3
}

const normalizeAnswerLetters = (value) => cleanCell(value)
  .toUpperCase()
  .replace(/[^A-H]/g, '')
  .split('')
  .filter((item, index, arr) => arr.indexOf(item) === index)
  .sort()

const parseImportRows = (rows) => {
  const parsed = []
  const errors = []

  rows.slice(1).forEach((row, index) => {
    const rowNumber = index + 2
    const type = cleanCell(row[0])
    const title = cleanCell(row[1])
    if (!type && !title) return

    if (!['单选题', '多选题', '判断题', '填空题', '简答题'].includes(type)) {
      errors.push(`第 ${rowNumber} 行：题型不支持`)
      return
    }
    if (!title) {
      errors.push(`第 ${rowNumber} 行：问题内容不能为空`)
      return
    }

    const difficulty = normalizeDifficulty(row[2])
    const rawOptions = row.slice(4, 12).map(cleanCell)
    const options = rawOptions.filter(Boolean)
    let answer = cleanCell(row[3])
    let payloadOptions = null

    if (type === '单选题' || type === '多选题') {
      if (options.length < 2) {
        errors.push(`第 ${rowNumber} 行：${type}至少需要 2 个选项`)
        return
      }
      const letters = normalizeAnswerLetters(answer)
      if (type === '单选题' && letters.length !== 1) {
        errors.push(`第 ${rowNumber} 行：单选题正确选项必须且只能填写 1 个字母`)
        return
      }
      if (type === '多选题' && letters.length < 2) {
        errors.push(`第 ${rowNumber} 行：多选题正确选项至少填写 2 个字母`)
        return
      }
      const invalidLetter = letters.find(letter => optionColumns.indexOf(letter) >= options.length)
      if (invalidLetter) {
        errors.push(`第 ${rowNumber} 行：正确选项 ${invalidLetter} 没有对应选项内容`)
        return
      }
      answer = letters.join(',')
      payloadOptions = JSON.stringify(options)
    } else if (type === '判断题') {
      const judgeOptions = options.length >= 2 ? options.slice(0, 2) : ['正确', '错误']
      const letters = normalizeAnswerLetters(answer)
      if (letters.length === 1) {
        const answerIndex = optionColumns.indexOf(letters[0])
        answer = judgeOptions[answerIndex] || ''
      }
      if (!['正确', '错误'].includes(answer)) {
        errors.push(`第 ${rowNumber} 行：判断题正确选项需填写 A/B 或 正确/错误`)
        return
      }
      payloadOptions = JSON.stringify(['正确', '错误'])
    } else {
      const answers = options.filter(Boolean)
      answer = answer || answers.join('/')
    }

    parsed.push({
      subject: currentSubject.value.name,
      type,
      title,
      options: payloadOptions,
      answer,
      difficulty
    })
  })

  return { parsed, errors }
}

const handleExcelImport = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file || importing.value) return
  if (!/\.(xlsx|xls)$/i.test(file.name)) {
    ElMessage.error('请上传 Excel 模板文件（.xlsx 或 .xls）')
    return
  }

  importing.value = true
  try {
    const buffer = await file.arrayBuffer()
    const workbook = XLSX.read(buffer, { type: 'array' })
    const sheet = workbook.Sheets[workbook.SheetNames[0]]
    const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' })
    const { parsed, errors } = parseImportRows(rows)

    if (!parsed.length) {
      ElMessage.error(errors[0] || '模板中没有可导入的试题')
      return
    }

    if (errors.length) {
      await ElMessageBox.confirm(
        `已解析 ${parsed.length} 道题，发现 ${errors.length} 条问题。是否先导入可用试题？\n\n${errors.slice(0, 5).join('\n')}${errors.length > 5 ? '\n...' : ''}`,
        '导入确认',
        { type: 'warning', confirmButtonText: '继续导入', cancelButtonText: '取消' }
      )
    }

    let success = 0
    const failed = []
    for (const question of parsed) {
      try {
        await request.post('/teacher/questions', question)
        success += 1
      } catch (e) {
        failed.push(question.title)
      }
    }

    await Promise.all([fetchQuestions(), fetchSubjectCounts()])
    if (failed.length) {
      ElMessage.warning(`成功导入 ${success} 道题，${failed.length} 道题导入失败`)
    } else {
      ElMessage.success(`成功导入 ${success} 道题`)
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Failed to import Excel questions', e)
      ElMessage.error('Excel模板解析或导入失败')
    }
  } finally {
    importing.value = false
  }
}

const addOption = () => {
  if (questionForm.value.options.length >= 8) {
    ElMessage.warning('最多支持 8 个选项')
    return
  }
  questionForm.value.options.push('')
}

const removeOption = (index) => {
  const removedLetter = getOptionLetter(index)
  questionForm.value.options.splice(index, 1)
  if (questionForm.value.type === '单选题' && questionForm.value.answer === removedLetter) {
    questionForm.value.answer = ''
  }
  if (questionForm.value.type === '多选题') {
    questionForm.value.multiAnswer = questionForm.value.multiAnswer.filter(item => item !== removedLetter)
  }
}

watch(() => questionForm.value.type, (type, oldType) => {
  if (hydratingQuestionForm.value) return
  if (!oldType || type === oldType) return
  if (type === '判断题') {
    questionForm.value.options = ['正确', '错误']
    questionForm.value.answer = '正确'
    questionForm.value.multiAnswer = []
    return
  }
  if (type === '单选题') {
    questionForm.value.options = questionForm.value.options.length >= 2 ? questionForm.value.options : ['', '', '', '']
    questionForm.value.answer = ''
    questionForm.value.multiAnswer = []
    return
  }
  if (type === '多选题') {
    questionForm.value.options = questionForm.value.options.length >= 2 ? questionForm.value.options : ['', '', '', '']
    questionForm.value.answer = ''
    questionForm.value.multiAnswer = []
    return
  }
  questionForm.value.options = []
  questionForm.value.answer = ''
  questionForm.value.multiAnswer = []
})

const editQuestion = (row) => {
  isEditing.value = true
  // reload full question from backend to get options & answer
  request.get('/teacher/questions', { params: { subject: currentSubject.value.name, keyword: row.title } }).then(res => {
    const q = res.find(x => x.questionId === row.id)
    if (q) {
      let opts = ['', '', '', '']
      try { opts = JSON.parse(q.options) || opts } catch(e) {}
      hydratingQuestionForm.value = true
      const answer = q.answer || ''
      questionForm.value = {
        questionId: q.questionId,
        subject: q.subject,
        type: q.type,
        title: q.title,
        options: opts.length ? opts : ['', '', '', ''],
        answer,
        multiAnswer: q.type === '多选题' ? answer.split(',').filter(Boolean) : [],
        difficulty: q.difficulty || 3
      }
      nextTick(() => {
        hydratingQuestionForm.value = false
      })
      questionDialogVisible.value = true
    }
  })
}

const addQuestion = () => {
  isEditing.value = false
  questionForm.value = createQuestionForm(currentSubject.value.name)
  questionDialogVisible.value = true
}

const submitQuestion = async () => {
  if (!questionForm.value.title) {
    ElMessage.error('题干不能为空')
    return
  }
  if (choiceTypes.includes(questionForm.value.type)) {
    const validOptions = questionForm.value.options.map(item => item.trim()).filter(Boolean)
    if (validOptions.length < 2) {
      ElMessage.error('选项题至少需要填写 2 个选项')
      return
    }
    if (questionForm.value.type === '单选题' && !questionForm.value.answer) {
      ElMessage.error('请选择单选题标准答案')
      return
    }
    if (questionForm.value.type === '多选题' && questionForm.value.multiAnswer.length < 2) {
      ElMessage.error('多选题至少需要选择 2 个正确答案')
      return
    }
  }
  if (questionForm.value.type === '判断题' && !questionForm.value.answer) {
    ElMessage.error('请选择判断题标准答案')
    return
  }
  if (!['单选题', '多选题', '判断题'].includes(questionForm.value.type) && !questionForm.value.answer) {
    ElMessage.error('请填写标准答案或参考答案')
    return
  }

  const options = questionForm.value.type === '判断题'
    ? ['正确', '错误']
    : questionForm.value.options.map(item => item.trim()).filter(Boolean)
  const answer = questionForm.value.type === '多选题'
    ? [...questionForm.value.multiAnswer].sort().join(',')
    : questionForm.value.answer
  try {
    const payload = {
      questionId: questionForm.value.questionId,
      subject: currentSubject.value.name,
      type: questionForm.value.type,
      title: questionForm.value.title,
      options: optionTypes.includes(questionForm.value.type) ? JSON.stringify(options) : null,
      answer,
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

onMounted(async () => {
  await loadManagedSubjects()
  await fetchSubjectCounts()
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

.option-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.option-letter {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-light);
  color: var(--primary-color);
  font-weight: 700;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px dashed var(--glass-border);
}
</style>
