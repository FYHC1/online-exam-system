<template>
  <div class="manual-paper-builder">
    <div class="glass-card mb-4" style="padding: 20px;">
      <el-button icon="el-icon-back" circle @click="goBack" class="mb-4" />
      <h2 style="margin-top: 0;">手动组卷 - {{ examConfig.subject }}</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">
        考试名称：{{ examConfig.title }} <br>
        您可以自由筛选当前科目的题库，勾选题目并设置每道题的具体分值。
      </p>
    </div>

    <div class="glass-card mb-4 filter-bar" style="padding: 20px 20px 0 20px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="题型搜索：">
          <el-select v-model="filters.type" placeholder="全部题型" clearable style="width: 150px">
            <el-option label="单选题" value="单选题" />
            <el-option label="多选题" value="多选题" />
            <el-option label="判断题" value="判断题" />
            <el-option label="简答题" value="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度：">
          <el-select v-model="filters.difficulty" placeholder="全部难度" clearable style="width: 150px">
            <el-option label="容易 (1-2星)" value="easy" />
            <el-option label="中等 (3星)" value="medium" />
            <el-option label="困难 (4-5星)" value="hard" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card p-4">
      <el-table 
        :data="filteredQuestions" 
        @selection-change="handleSelectionChange" 
        style="width: 100%" 
        class="custom-table"
        row-key="id"
      >
        <el-table-column type="selection" width="50" :reserve-selection="true" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.type)" effect="plain">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="题干内容" show-overflow-tooltip />
        <el-table-column prop="difficulty" label="难度" width="160">
          <template #default="scope">
            <el-rate v-model="scope.row.difficulty" disabled :max="5" />
          </template>
        </el-table-column>
        <el-table-column label="分值设置" width="160" align="center">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.score" 
              :min="1" 
              :max="100" 
              size="small" 
              controls-position="right" 
              style="width: 100px"
              :disabled="!isSelected(scope.row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 题型统计区 -->
      <div v-if="typeStats.length > 0" class="stats-area mt-4 p-3" style="background: rgba(var(--primary-color-rgb), 0.05); border-radius: 8px; margin-top: 15px; padding: 15px;">
        <span style="font-weight: bold; margin-right: 15px; color: var(--text-regular);">已选分布：</span>
        <el-tag 
          v-for="([type, data]) in typeStats" 
          :key="type" 
          :type="getTagType(type)" 
          class="mr-2"
          style="margin-right: 10px;"
        >
          {{ type }}：{{ data.count }}题, 计{{ data.score }}分
        </el-tag>
      </div>

      <div class="bottom-action-bar">
        <div class="summary">
          <span>已选择 <strong style="color: var(--primary-color); font-size: 18px;">{{ selectedQuestions.length }}</strong> 道题目</span>
          <span style="margin-left: 20px;">试卷总分预期：<strong style="color: var(--danger-color); font-size: 18px;">{{ totalScore }}</strong> 分</span>
        </div>
        <div>
          <el-button @click="goBack">返回取消</el-button>
          <el-button type="primary" size="large" @click="submitExam" :loading="submitting">确认组卷并发布</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const examConfig = ref({})
const rawQuestions = ref([])
const selectedQuestions = ref([])
const submitting = ref(false)

const filters = ref({
  type: '',
  difficulty: ''
})

onMounted(async () => {
  const cfgStr = sessionStorage.getItem('pendingExamConfig')
  if (!cfgStr) {
    ElMessage.error('考试配置丢失，请重新填写')
    router.push('/teacher/exams')
    return
  }
  
  examConfig.value = JSON.parse(cfgStr)
  
  // Load questions for the target subject
  try {
    const res = await request.get('/teacher/questions', { params: { subject: examConfig.value.subject } })
    rawQuestions.value = res.map(q => ({
      id: q.questionId,
      type: q.type,
      title: q.title,
      difficulty: q.difficulty || 3,
      // Default score based on type
      score: q.type === '简答题' ? 10 : 5
    }))
  } catch (e) {
    ElMessage.error('题库加载失败')
  }
})

const filteredQuestions = computed(() => {
  return rawQuestions.value.filter(q => {
    let matchType = !filters.value.type || q.type === filters.value.type
    let matchDiff = true
    if (filters.value.difficulty === 'easy') matchDiff = q.difficulty <= 2
    else if (filters.value.difficulty === 'medium') matchDiff = q.difficulty === 3
    else if (filters.value.difficulty === 'hard') matchDiff = q.difficulty >= 4
    
    return matchType && matchDiff
  })
})

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const isSelected = (row) => {
  return selectedQuestions.value.some(sq => sq.id === row.id)
}

const totalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, q) => sum + (q.score || 0), 0)
})

const typeStats = computed(() => {
  const stats = {
    '单选题': { count: 0, score: 0 },
    '多选题': { count: 0, score: 0 },
    '判断题': { count: 0, score: 0 },
    '简答题': { count: 0, score: 0 }
  }
  
  selectedQuestions.value.forEach(q => {
    if (stats[q.type]) {
      stats[q.type].count++
      stats[q.type].score += (q.score || 0)
    }
  })
  
  return Object.entries(stats).filter(([_, data]) => data.count > 0)
})

const getTagType = (type) => {
  if (type === '单选题') return 'primary'
  if (type === '多选题') return 'warning'
  if (type === '判断题') return 'success'
  return 'danger'
}

const goBack = () => {
  sessionStorage.removeItem('pendingExamConfig')
  router.push('/teacher/exams')
}

const submitExam = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请至少选择一道题目加入试卷')
    return
  }

  submitting.value = true
  
  // Package payload exactly as required by TeacherController
  const payload = {
    ...examConfig.value,
    manualQuestions: selectedQuestions.value.map(q => ({
      questionId: q.id,
      score: q.score
    }))
  }

  try {
    await request.post('/teacher/exams', payload)
    ElMessage.success('考试创建并发布成功！')
    sessionStorage.removeItem('pendingExamConfig')
    router.push('/teacher/exams')
  } catch (e) {
    ElMessage.error('发布失败：' + (e.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.manual-paper-builder {
  animation: fadeIn 0.4s ease-out forwards;
}

.bottom-action-bar {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--glass-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
