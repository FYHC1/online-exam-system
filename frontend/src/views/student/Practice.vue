<template>
  <div class="practice-container">
    <div class="glass-card mb-4" style="padding: 20px;">
      <h2>我的错题本</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">在此复习您曾在考试和测验中做错的题目。</p>
    </div>

    <div class="glass-card filter-bar mb-4" style="padding: 20px 20px 0 20px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="所属学科">
          <el-select v-model="filters.subject" placeholder="全部学科">
            <el-option label="全部学科" value="" />
            <el-option v-for="subject in subjectOptions" :key="subject" :label="subject" :value="subject" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="filters.type" placeholder="全部题型">
            <el-option label="全部" value="" />
            <el-option label="单选题" value="单选题" />
            <el-option label="多选题" value="多选题" />
            <el-option label="判断题" value="判断题" />
            <el-option label="简答题" value="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="filters.difficulty" placeholder="全部难度">
            <el-option label="全部" value="" />
            <el-option label="简单 (1-2星)" value="easy" />
            <el-option label="较难 (3-5星)" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">筛选错题</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card p-4">
      <el-table :data="filteredQuestions" style="width: 100%" class="custom-table">
        <el-table-column prop="title" label="题目内容" />
        <el-table-column prop="type" label="题型" width="120" />
        <el-table-column prop="examName" label="来源考试" />
        <el-table-column prop="difficulty" label="难度" width="120" align="center">
          <template #default="scope">
            <el-rate v-model="scope.row.difficulty" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误次数" width="120" align="center">
          <template #default="scope">
            <el-tag type="danger">{{ scope.row.errorCount }} 次</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="viewDetail(scope.row)">重新挑战</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="challengeDialogVisible" title="错题重练" width="720px">
      <template v-if="currentQuestion">
        <div style="margin-bottom: 16px; color: var(--text-secondary);">
          来源考试：{{ currentQuestion.examName }}
        </div>
        <h3 style="margin: 0 0 16px; line-height: 1.6;">{{ currentQuestion.title }}</h3>

        <div v-if="isChoiceQuestion(currentQuestion)" class="challenge-options">
          <el-radio-group v-if="currentQuestion.type !== '多选题'" v-model="challengeAnswer" class="challenge-group">
            <el-radio v-for="option in currentQuestion.options" :key="option.value" :value="option.value" class="challenge-option">
              <template v-if="option.prefix">{{ option.prefix }}. </template>{{ option.label }}
            </el-radio>
          </el-radio-group>
          <el-checkbox-group v-else v-model="challengeAnswer" class="challenge-group">
            <el-checkbox v-for="option in currentQuestion.options" :key="option.value" :value="option.value" class="challenge-option">
              <template v-if="option.prefix">{{ option.prefix }}. </template>{{ option.label }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <el-input
          v-else
          v-model="challengeAnswer"
          type="textarea"
          :rows="6"
          placeholder="请输入你的答案"
        />

        <el-alert
          v-if="showChallengeResult"
          :title="challengeResultTitle"
          :type="challengeCorrect ? 'success' : 'error'"
          :closable="false"
          style="margin-top: 16px;"
        />
        <div v-if="showChallengeResult" style="margin-top: 12px; color: var(--text-secondary);">
          参考答案：{{ currentQuestion.referenceAnswer || '暂无' }}
        </div>
      </template>

      <template #footer>
        <el-button @click="closeChallengeDialog">关闭</el-button>
        <el-button type="primary" :disabled="challengeSubmitting || showChallengeResult" @click="submitChallenge">
          {{ challengeSubmitting ? '提交中...' : '提交答案' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const filters = ref({ subject: '', type: '', difficulty: '' })

const wrongQuestions = ref([])
const challengeDialogVisible = ref(false)
const currentQuestion = ref(null)
const challengeAnswer = ref('')
const showChallengeResult = ref(false)
const challengeCorrect = ref(false)
const challengeSubmitting = ref(false)

const normalizeOptions = (rawOptions, questionType) => {
  if (!rawOptions) return []

  try {
    const parsed = JSON.parse(rawOptions)
    if (!Array.isArray(parsed)) return []

    return parsed.map((option, index) => {
      const optionLabel = option?.label || option?.text || String(option)

      if (questionType === '判断题') {
        return {
          value: optionLabel,
          label: optionLabel,
          prefix: ''
        }
      }

      return {
        value: option?.value || String.fromCharCode(65 + index),
        label: optionLabel,
        prefix: String.fromCharCode(65 + index)
      }
    })
  } catch {
    return []
  }
}

const isChoiceQuestion = (question) => ['单选题', '多选题', '判断题'].includes(question.type)

const normalizeAnswer = (answer) => {
  if (Array.isArray(answer)) {
    return [...answer].sort().join(',')
  }
  return String(answer || '').replace(/，/g, ',').trim()
}

const challengeResultTitle = computed(() => challengeCorrect.value ? '回答正确，继续保持。' : '回答有误，建议再复习一遍。')
const subjectOptions = computed(() => [...new Set(wrongQuestions.value.map(item => item.subject).filter(Boolean))])

const loadWrongQuestions = async () => {
  try {
    const res = await request.get('/student/wrong-questions')
    wrongQuestions.value = res.map(item => ({
      id: item.id,
      questionId: item.questionId,
      title: item.questionTitle || '题目内容加载中...',
      type: item.questionType || '未知',
      subject: item.questionSubject || '未分类',
      examName: item.sourceExamTitle || '历史考试',
      errorCount: item.errorCount || 1,
      difficulty: item.questionDifficulty || 3,
      options: normalizeOptions(item.questionOptions, item.questionType),
      referenceAnswer: item.referenceAnswer || '',
      correctStreak: item.correctStreak || 0
    }))
  } catch (e) {
    // Fallback to empty
    wrongQuestions.value = []
  }
}

onMounted(() => {
  loadWrongQuestions()
})

const filteredQuestions = computed(() => {
  return wrongQuestions.value.filter(q => {
    let diffMatch = true;
    if (filters.value.difficulty === 'easy') diffMatch = q.difficulty <= 2;
    if (filters.value.difficulty === 'hard') diffMatch = q.difficulty >= 3;

    return (!filters.value.subject || q.subject === filters.value.subject) &&
           (!filters.value.type || q.type === filters.value.type) &&
           diffMatch
  })
})

const viewDetail = (row) => {
  currentQuestion.value = row
  challengeAnswer.value = row.type === '多选题' ? [] : ''
  showChallengeResult.value = false
  challengeCorrect.value = false
  challengeDialogVisible.value = true
}

const submitChallenge = async () => {
  if (!currentQuestion.value || challengeSubmitting.value || showChallengeResult.value) return

  const userAnswer = normalizeAnswer(challengeAnswer.value)
  if (!userAnswer) {
    ElMessage.warning('请先作答再提交')
    return
  }

  challengeCorrect.value = userAnswer === normalizeAnswer(currentQuestion.value.referenceAnswer)
  showChallengeResult.value = true
  challengeSubmitting.value = true

  try {
    const result = await request.post('/student/wrong-questions/challenge', {
      questionId: currentQuestion.value.questionId,
      correct: challengeCorrect.value
    })

    if (result.removed) {
      wrongQuestions.value = wrongQuestions.value.filter(item => item.questionId !== currentQuestion.value.questionId)
      ElMessage.success('已连续答对两次，该题已从错题本移除')
      closeChallengeDialog()
      return
    }

    const target = wrongQuestions.value.find(item => item.questionId === currentQuestion.value.questionId)
    if (target) {
      target.correctStreak = result.correctStreak || 0
      if (!challengeCorrect.value) {
        target.errorCount += 1
      }
    }

    if (challengeCorrect.value) {
      const remainCount = Math.max((result.requiredCorrectStreak || 2) - (result.correctStreak || 0), 0)
      if (remainCount > 0) {
        ElMessage.success(`再连续答对 ${remainCount} 次即可移出错题本`)
      }
    }
  } catch (e) {
    ElMessage.error('重练结果同步失败')
    showChallengeResult.value = false
  } finally {
    challengeSubmitting.value = false
  }
}

const closeChallengeDialog = () => {
  challengeDialogVisible.value = false
  currentQuestion.value = null
  challengeAnswer.value = ''
  showChallengeResult.value = false
  challengeSubmitting.value = false
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 24px; }
:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th) { background-color: transparent !important; }

.challenge-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.challenge-option {
  margin: 0 !important;
  padding: 12px 16px;
  border: 1px solid var(--glass-border);
  border-radius: 8px;
}
</style>
