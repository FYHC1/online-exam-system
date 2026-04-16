<template>
  <div class="exam-room" :class="{ fullscreen: isFullscreen }" @copy.prevent @cut.prevent @paste.prevent @contextmenu.prevent>
    <!-- 顶部状态栏 -->
    <header class="exam-header glass-container">
      <div class="exam-title text-truncate">
        <h1>{{ examTitle }}</h1>
      </div>
      <div class="exam-status">
        <div class="timer">
          <i class="el-icon-timer"></i>
          剩余时间：<span :class="{ 'warning': timeLeft < 300 }">{{ formatTime(timeLeft) }}</span>
        </div>
        <div class="anti-cheat-status">切屏次数：{{ visibilityChangeCount }} / {{ maxVisibilityChanges }}</div>
        <el-button type="danger" @click="submitPaper">交卷</el-button>
        <el-button @click="toggleFullscreen">{{ isFullscreen ? '退出全屏' : '全屏作答' }}</el-button>
      </div>
    </header>

    <div class="exam-content">
      <!-- 试题目结构 -->
      <main class="question-area glass-container">
        <div class="question-card" v-for="(q, index) in questions" :key="q.id" v-show="currentIndex === index">
          <div class="q-header">
            <span class="q-type">{{ q.type }}</span>
            <span class="q-score">({{ q.score }}分)</span>
          </div>
          <div class="q-body">
            <h3 class="q-title">{{ index + 1 }}. {{ q.title }}</h3>
            <div class="q-options" v-if="q.type === '单选题' || q.type === '多选题' || q.type === '判断题'">
              <el-radio-group v-model="answers[q.id]" class="block-radio" v-if="q.type === '单选题' || q.type === '判断题'">
                <el-radio v-for="opt in q.options" :key="opt.value" :value="opt.value" class="custom-radio">
                  <template v-if="opt.prefix">{{ opt.prefix }}. </template>{{ opt.label }}
                </el-radio>
              </el-radio-group>
              <el-checkbox-group v-model="answers[q.id]" class="block-radio" v-else>
                <el-checkbox v-for="opt in q.options" :key="opt.value" :value="opt.value" class="custom-radio">
                  <template v-if="opt.prefix">{{ opt.prefix }}. </template>{{ opt.label }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
            <div class="q-input" v-else-if="q.type === '填空题'">
              <el-input v-model="answers[q.id]" placeholder="请输入答案" />
            </div>
            <div class="q-input" v-else>
              <el-input type="textarea" :rows="6" v-model="answers[q.id]" placeholder="请输入详细解答过程" />
            </div>
          </div>
        </div>
        
        <div class="q-footer">
          <el-button :disabled="currentIndex === 0" @click="currentIndex--">上一题</el-button>
          <el-button type="primary" :disabled="currentIndex === questions.length - 1" @click="currentIndex++">下一题</el-button>
        </div>
      </main>

      <!-- 答题卡导航 -->
      <aside class="answer-card-area glass-container">
        <h3>答题卡</h3>
        <div class="grid-card">
          <div 
            class="grid-item" 
            :class="{ active: answers[q.id] && answers[q.id].length > 0, current: currentIndex === index }"
            v-for="(q, index) in questions" 
            :key="q.id"
            @click="currentIndex = index"
          >
            {{ index + 1 }}
          </div>
        </div>
        <div class="legend">
          <div class="legend-item"><span class="box done"></span> 已答</div>
          <div class="legend-item"><span class="box"></span> 未答</div>
          <div class="legend-item"><span class="box current"></span> 当前</div>
        </div>
      </aside>
    </div>

    <el-dialog v-model="cheatWarningVisible" title="考试警告" width="420px" :close-on-click-modal="false" :show-close="false">
      <p style="margin: 0; line-height: 1.8;">
        检测到你已离开考试页面 {{ visibilityChangeCount }} 次。请立即返回并专注作答。
        超过 {{ maxVisibilityChanges }} 次将自动强制交卷，并标记为异常结束。
      </p>
      <template #footer>
        <el-button type="primary" @click="acknowledgeCheatWarning">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const examId = route.params.examId

const isFullscreen = ref(false)
const timeLeft = ref(7200) // Default 120 mins
const examTitle = ref('加载中...')
let timer = null
const maxVisibilityChanges = 3
const visibilityChangeCount = ref(0)
const cheatWarningVisible = ref(false)
const hasSubmitted = ref(false)
const isForceSubmitting = ref(false)

const currentIndex = ref(0)
const answers = ref({})
const questions = ref([])

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

      if (option && typeof option === 'object') {
        return {
          value: option.value || String.fromCharCode(65 + index),
          label: option.label || option.text || '',
          prefix: String.fromCharCode(65 + index)
        }
      }

      return {
        value: String.fromCharCode(65 + index),
        label: optionLabel,
        prefix: String.fromCharCode(65 + index)
      }
    })
  } catch (e) {
    return []
  }
}

const fetchPaper = async () => {
  try {
    const res = await request.get(`/student/exam/${examId}/paper`)
    examTitle.value = res.exam.title
    timeLeft.value = (res.paper.duration || 120) * 60
    
    questions.value = res.questions.map(q => {
      return {
        id: q.id,
        type: q.type,
        score: q.score,
        title: q.title,
        options: normalizeOptions(q.rawOptions, q.type)
      }
    })
    
    // Init answer array for multiple choice
    questions.value.forEach(q => {
      if (q.type === '多选题') {
        answers.value[q.id] = []
      }
    })
    
  } catch(e) {
    ElMessage.error('无法获取试卷信息')
  }
}


const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600).toString().padStart(2, '0')
  const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0')
  const s = (seconds % 60).toString().padStart(2, '0')
  return `${h}:${m}:${s}`
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(err => {
      console.error(`Error attempting to enable fullscreen: ${err.message}`)
    })
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

const enforceFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().then(() => {
      isFullscreen.value = true
    }).catch(() => {
      ElMessage.warning('浏览器阻止了自动全屏，请手动点击“全屏作答”')
    })
  }
}

const acknowledgeCheatWarning = () => {
  cheatWarningVisible.value = false
  enforceFullscreen()
}

const buildAnswerList = () => Object.keys(answers.value).map(qId => {
  let ans = answers.value[qId]
  if (Array.isArray(ans)) ans = ans.join(',')
  return { questionId: qId, answer: ans || '' }
})

const handleVisibilityChange = async () => {
  if (document.visibilityState !== 'hidden' || hasSubmitted.value) {
    return
  }

  visibilityChangeCount.value += 1

  if (visibilityChangeCount.value >= maxVisibilityChanges) {
    ElMessage.error('切屏次数超过限制，系统将自动交卷并标记为异常结束。')
    await submitPaper({ force: true, abnormalEnd: true, skipConfirm: true })
    return
  }

  cheatWarningVisible.value = true
}

const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

const blockClipboardAction = (event) => {
  event.preventDefault()
  ElMessage.warning('考试过程中禁止复制、粘贴和剪切操作')
}

const submitPaper = async ({ force = false, abnormalEnd = false, skipConfirm = false } = {}) => {
  if (hasSubmitted.value || isForceSubmitting.value) {
    return
  }

  const runSubmit = async () => {
    isForceSubmitting.value = true

    try {
      await request.post('/student/exam/submit', {
        examId: parseInt(examId),
        answers: buildAnswerList(),
        abnormalEnd,
        cheatCount: visibilityChangeCount.value
      })

      hasSubmitted.value = true
      ElMessage.success(abnormalEnd ? '考试已因异常结束自动交卷。' : '交卷成功，正在离开考场...')
      if (document.fullscreenElement) {
        document.exitFullscreen()
      }
      setTimeout(() => {
        router.push('/student/dashboard')
      }, 1500)
    } catch (e) {
      ElMessage.error(abnormalEnd ? '自动交卷失败，请联系监考老师' : '交卷失败')
    } finally {
      isForceSubmitting.value = false
    }
  }

  if (skipConfirm || force) {
    await runSubmit()
    return
  }

  ElMessageBox.confirm(
    '交卷后将无法再次修改答案，确认要现在交卷吗？',
    '交卷提示',
    { confirmButtonText: '立即交卷', cancelButtonText: '继续检查', type: 'warning' }
  ).then(runSubmit).catch(() => {})
}

onMounted(() => {
  fetchPaper()
  enforceFullscreen()
  document.addEventListener('visibilitychange', handleVisibilityChange)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('copy', blockClipboardAction)
  document.addEventListener('cut', blockClipboardAction)
  document.addEventListener('paste', blockClipboardAction)
  timer = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('考试时间到，系统已自动交卷！')
      submitPaper({ force: true, skipConfirm: true })
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('copy', blockClipboardAction)
  document.removeEventListener('cut', blockClipboardAction)
  document.removeEventListener('paste', blockClipboardAction)
})
</script>

<style scoped>
.exam-room {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: var(--bg-color);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 16px;
  user-select: none;
  -webkit-user-select: none;
}

.exam-header {
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.exam-title h1 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: var(--primary-color);
}

.exam-status {
  display: flex;
  align-items: center;
  gap: 20px;
}

.anti-cheat-status {
  font-size: 14px;
  color: var(--warning-color);
  font-weight: 600;
}

.timer {
  font-size: 18px;
  font-weight: 600;
  font-family: monospace;
}
.timer .warning { color: var(--danger-color); animation: blink 1s infinite; }

.exam-content {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.question-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 30px;
  overflow-y: auto;
}

.q-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--glass-border);
}
.q-type { background: var(--primary-color); color: white; padding: 4px 12px; border-radius: 4px; font-size: 14px; margin-right: 10px; }
.q-score { color: var(--text-secondary); }

.q-title { font-size: 18px; line-height: 1.6; margin-bottom: 24px; }
.block-radio { display: flex; flex-direction: column; gap: 16px; }
.custom-radio { padding: 12px 16px; border: 1px solid var(--glass-border); border-radius: 8px; margin: 0 !important; transition: all 0.3s; }
.custom-radio:hover { border-color: var(--primary-color); background: var(--primary-light); }

.q-footer { margin-top: auto; padding-top: 20px; display: flex; justify-content: center; gap: 20px; }

.answer-card-area {
  width: 300px;
  padding: 20px;
  display: flex;
  flex-direction: column;
}
.answer-card-area h3 { margin-bottom: 16px; font-size: 16px; border-bottom: 1px solid var(--glass-border); padding-bottom: 8px; }

.grid-card {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}
.grid-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.grid-item.active { background: var(--success-color); color: white; border-color: var(--success-color); }
.grid-item.current { box-shadow: 0 0 0 2px var(--primary-color); }

.legend { display: flex; justify-content: space-around; margin-top: 20px; font-size: 12px; color: var(--text-secondary); }
.legend-item { display: flex; align-items: center; gap: 4px; }
.legend-item .box { width: 12px; height: 12px; border: 1px solid var(--border-color); border-radius: 2px; }
.box.done { background: var(--success-color); border-color: var(--success-color); }
.box.current { border-color: var(--primary-color); box-shadow: 0 0 0 1px var(--primary-color); }

@keyframes blink { 50% { opacity: 0.5; } }
</style>
