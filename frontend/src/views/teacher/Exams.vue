<template>
  <div class="exams-management">
    <!-- View 1: Subject Selection -->
    <div v-if="!currentSubject" class="subject-view">
      <div class="glass-card mb-4" style="padding: 20px;">
        <h2>考试管理与记录</h2>
        <p style="color: var(--text-secondary); margin-top: 8px;">按科目查看各科目的考试记录（提供已结束及正在进行的考试信息）。</p>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="subject in subjects" :key="subject.id">
          <div class="glass-card subject-card" @click="enterSubjectExams(subject)">
            <div class="icon-wrap" :style="{ background: subject.color }">
              <i :class="subject.icon"></i>
            </div>
            <div class="subject-info">
              <h3>{{ subject.name }}</h3>
              <p>进行中/未开始: {{ subject.activeExams }} 场</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- View 2: Detailed Exam Management -->
    <div v-else class="detail-view">
      <div class="glass-card mb-4 top-bar">
        <el-button icon="el-icon-back" circle @click="currentSubject = null" />
        <h2 style="margin: 0;">{{ currentSubject.name }} - 考试管理</h2>
      </div>

      <div class="glass-card mb-4 filter-bar">
        <el-form :inline="true" :model="filters">
          <el-form-item label="学期：">
            <el-select v-model="filters.term" placeholder="全部学期" clearable style="width: 150px">
              <el-option label="23-24第二学期" value="23-24第二学期" />
              <el-option label="23-24第一学期" value="23-24第一学期" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试状态：">
            <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 150px">
              <el-option label="未开始" value="未开始" />
              <el-option label="进行中" value="进行中" />
              <el-option label="已结束" value="已结束" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级：">
            <el-select v-model="filters.grade" placeholder="全部年级" clearable style="width: 150px">
              <el-option label="2021级" value="2021级" />
              <el-option label="2022级" value="2022级" />
              <el-option label="2023级" value="2023级" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级：">
            <el-select v-model="filters.class" placeholder="全部班级" clearable style="width: 150px">
              <el-option label="1班" value="1班" />
              <el-option label="2班" value="2班" />
              <el-option label="软件1班" value="软件1班" />
            </el-select>
          </el-form-item>
          <el-form-item style="margin-left: auto; margin-right: 0;">
            <el-button type="primary" size="large" @click="openPublishDialog" icon="el-icon-plus">发布新考试</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="glass-card p-4">
        <el-table :data="filteredExams" class="custom-table">
          <el-table-column prop="title" label="考试名称" />
          <el-table-column prop="targetClass" label="参与班级" width="200" />
          <el-table-column prop="duration" label="起止时间" width="280">
            <template #default="scope">
              <span style="font-size:12px; color: var(--text-secondary)">{{ scope.row.startTime }} 至 {{ scope.row.endTime }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="130">
            <template #default="scope">
              <el-tag :type="scope.row.status === '进行中' ? 'success' : (scope.row.status === '未开始' ? 'info' : 'warning')">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center">
            <template #default="scope">
              <!-- 进行中：监控，终止 -->
              <template v-if="scope.row.status === '进行中'">
                <el-button link type="primary" size="small" @click="handleMonitor(scope.row)">现场监控</el-button>
                <el-button link type="danger" size="small" @click="handleTerminate(scope.row)">终止考试</el-button>
              </template>
              <!-- 未开始：修改，取消 -->
              <template v-else-if="scope.row.status === '未开始'">
                <el-button link type="primary" size="small" @click="handleEdit(scope.row)">修改</el-button>
                <el-button link type="danger" size="small" @click="handleCancel(scope.row)">取消发布</el-button>
              </template>
              <!-- 已结束：查看结果 -->
              <template v-else>
                <el-button link type="primary" size="small" @click="exportReport(scope.row)">导出成绩报告</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination background layout="prev, pager, next, total" :total="filteredExams.length" />
        </div>
      </div>
    </div>

    <!-- 弹窗：发布考试 -->
    <el-dialog v-model="publishDialogVisible" :title="editingExamId ? '修改考试' : '发布新考试'" width="600px" custom-class="glass-dialog">
      <el-form :model="newExam" label-width="100px" label-position="left">
        <el-form-item label="考试名称：">
          <el-input v-model="newExam.title" placeholder="例如：2023-2024学年期中结课考试" />
        </el-form-item>
        <el-form-item label="考试科目：">
          <el-input :value="currentSubject ? currentSubject.name : ''" disabled placeholder="自动绑定当前所选科目" />
        </el-form-item>
        <el-form-item label="参与班级：">
          <el-cascader v-model="newExam.targets" :options="targetOptions" :props="{ multiple: true }" collapse-tags clearable style="width: 100%" placeholder="请选择参与班级..." />
        </el-form-item>
        <el-form-item label="考试时间：">
          <el-date-picker v-model="newExam.timerange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="试题难度：">
          <el-select v-model="newExam.difficulty" placeholder="不限难度" clearable style="width: 100%">
            <el-option label="偏易 (适合随堂小测)" value="easy" />
            <el-option label="适中 (适合期中/单元测)" value="medium" />
            <el-option label="偏难 (适合期末/选拔考)" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item label="组卷方式：">
          <el-radio-group v-model="newExam.paperMode" :disabled="!!editingExamId">
            <el-radio-button label="auto">自动随机抽题组卷</el-radio-button>
            <el-radio-button label="manual">手动指定试卷题目</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <div v-if="newExam.paperMode === 'auto'" style="background: rgba(var(--primary-color-rgb), 0.05); padding: 20px; border-radius: 12px; margin-bottom: 20px; border: 1px solid var(--glass-border);">
          <h4 style="margin: 0 0 16px 0; color: var(--primary-color);">自动抽题题型及数量配置</h4>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input-number v-model="newExam.autoConfig.single" :min="0" :max="50" size="small" controls-position="right" style="width: 100%" />
              <div style="font-size: 12px; color: var(--text-secondary); text-align: center; margin-top: 6px;">单选 (题数)</div>
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="newExam.autoConfig.multiple" :min="0" :max="50" size="small" controls-position="right" style="width: 100%" />
              <div style="font-size: 12px; color: var(--text-secondary); text-align: center; margin-top: 6px;">多选 (题数)</div>
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="newExam.autoConfig.judge" :min="0" :max="50" size="small" controls-position="right" style="width: 100%" />
              <div style="font-size: 12px; color: var(--text-secondary); text-align: center; margin-top: 6px;">判断 (题数)</div>
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="newExam.autoConfig.subjective" :min="0" :max="50" size="small" controls-position="right" style="width: 100%" />
              <div style="font-size: 12px; color: var(--text-secondary); text-align: center; margin-top: 6px;">简答 (题数)</div>
            </el-col>
          </el-row>
          <div v-if="!editingExamId" style="margin-top: 16px; display: flex; flex-wrap: wrap; gap: 10px;">
            <el-tag
              v-for="item in autoConfigStatus"
              :key="item.key"
              :type="item.sufficient ? 'success' : 'danger'"
            >
              {{ item.label }}: 可用 {{ item.available }} / 需要 {{ item.required }}
            </el-tag>
          </div>
          <p
            v-if="!editingExamId"
            style="margin: 12px 0 0; font-size: 13px;"
            :style="{ color: autoConfigValid ? 'var(--success-color)' : 'var(--danger-color)' }"
          >
            {{ autoConfigValid ? '当前题库数量满足智能组卷要求。' : '当前题库数量不足，请减少题量或补充题库。' }}
          </p>
        </div>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closePublishDialog">取消</el-button>
          <el-button type="primary" @click="submitPublish">
            {{ editingExamId ? '保存修改' : (newExam.paperMode === 'manual' ? '前往挑选题目' : '确认发布') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

import { useRouter } from 'vue-router'

const router = useRouter()
const currentSubject = ref(null)
const filters = ref({ term: '', status: '', grade: '', class: '' })

const publishDialogVisible = ref(false)
const editingExamId = ref(null)
const newExam = ref({
  title: '',
  targets: [],
  timerange: [],
  difficulty: '',
  paperMode: 'auto',
  autoConfig: { single: 5, multiple: 2, judge: 3, subjective: 1 }
})

const resetExamForm = () => {
  editingExamId.value = null
  newExam.value = {
    title: '',
    targets: [],
    timerange: [],
    difficulty: '',
    paperMode: 'auto',
    autoConfig: { single: 5, multiple: 2, judge: 3, subjective: 1 }
  }
}

const closePublishDialog = () => {
  publishDialogVisible.value = false
  resetExamForm()
}

const questionAvailability = ref({
  单选题: 0,
  多选题: 0,
  判断题: 0,
  简答题: 0
})

const autoConfigMap = [
  { key: 'single', label: '单选题' },
  { key: 'multiple', label: '多选题' },
  { key: 'judge', label: '判断题' },
  { key: 'subjective', label: '简答题' }
]

const autoConfigStatus = computed(() => autoConfigMap.map(item => {
  const required = newExam.value.autoConfig[item.key] || 0
  const available = questionAvailability.value[item.label] || 0
  return {
    ...item,
    required,
    available,
    sufficient: available >= required
  }
}))

const autoConfigValid = computed(() => autoConfigStatus.value.every(item => item.sufficient))

const loadQuestionAvailability = async () => {
  if (!currentSubject.value) return
  try {
    const res = await request.get('/teacher/questions', {
      params: { subject: currentSubject.value.name }
    })
    const counts = { 单选题: 0, 多选题: 0, 判断题: 0, 简答题: 0 }
    res.forEach(question => {
      if (counts[question.type] !== undefined) {
        counts[question.type] += 1
      }
    })
    questionAvailability.value = counts
  } catch (e) {
    ElMessage.error('无法获取当前题库可用题数')
  }
}

const openPublishDialog = () => {
  resetExamForm()
  publishDialogVisible.value = true
  loadQuestionAvailability()
}

const subjects = ref([
  { id: 1, name: '高等数学', activeExams: 0, icon: 'el-icon-odometer', color: 'rgba(56, 189, 248, 0.2)' },
  { id: 2, name: '大学英语', activeExams: 0, icon: 'el-icon-chat-line-round', color: 'rgba(250, 204, 21, 0.2)' },
  { id: 3, name: 'JavaWeb', activeExams: 0, icon: 'el-icon-monitor', color: 'rgba(16, 185, 129, 0.2)' },
  { id: 4, name: '数据结构', activeExams: 0, icon: 'el-icon-connection', color: 'rgba(168, 85, 247, 0.2)' }
])

const targetOptions = ref([
  { value: '2023级', label: '2023级', children: [ { value: '1班', label: '1班' }, { value: '2班', label: '2班' } ] },
  { value: '2022级', label: '2022级', children: [ { value: '软件1班', label: '软件1班' } ] }
])

const buildTargetPaths = (targetClasses) => {
  if (!targetClasses) return []

  return targetClasses.split(',').map(targetClass => {
    const className = targetClass.trim()
    const grade = targetOptions.value.find(option => option.children?.some(child => child.value === className))
    return grade ? [grade.value, className] : [className]
  })
}

const allExams = ref([])

const deriveExamStatus = (exam) => {
  const now = Date.now()
  const startTime = exam.startTime ? new Date(exam.startTime).getTime() : null
  const endTime = exam.endTime ? new Date(exam.endTime).getTime() : null

  if (['cancelled', 'finished', 'grading', 'abnormal'].includes(exam.status)) {
    return '已结束'
  }

  if (exam.status === 'pending' && startTime && now < startTime) {
    return '未开始'
  }

  if (exam.status === 'running') {
    return '进行中'
  }

  if (endTime && now >= endTime) {
    return '已结束'
  }

  if (startTime && now < startTime) {
    return '未开始'
  }

  return '进行中'
}

const refreshSubjectStats = () => {
  subjects.value = subjects.value.map(subject => ({
    ...subject,
    activeExams: allExams.value.filter(exam => exam.subjectName === subject.name && exam.status !== '已结束').length
  }))
}

const fetchExams = async () => {
  try {
    const res = await request.get('/teacher/exams')
    allExams.value = res.map(e => {
      const subjectName = e.subject || (e.title.includes('数学') ? '高等数学' : (e.title.includes('英语') ? '大学英语' : 'JavaWeb'))
      return {
        id: e.examId,
        term: '23-24第一学期',
        subjectId: subjects.value.find(subject => subject.name === subjectName)?.id || null,
        subjectName,
        title: e.title,
        targetClass: e.targetClasses,
        rawTargetClasses: e.targetClasses,
        duration: e.duration || 120,
        startTimeRaw: e.startTime,
        endTimeRaw: e.endTime,
        startTime: new Date(e.startTime).toLocaleString(),
        endTime: new Date(e.endTime).toLocaleString(),
        status: deriveExamStatus(e)
      }
    })
    refreshSubjectStats()
  } catch(e) {
    ElMessage.error('无法加载考试列表')
  }
}

onMounted(() => {
  fetchExams()
})

const filteredExams = computed(() => {
  if (!currentSubject.value) return []
  return allExams.value.filter(e => {
    return e.subjectName === currentSubject.value.name &&
           (!filters.value.term || e.term === filters.value.term) &&
           (!filters.value.status || e.status === filters.value.status) &&
           (!filters.value.grade || e.targetClass.includes(filters.value.grade)) &&
           (!filters.value.class || e.targetClass.includes(filters.value.class))
  })
})

const enterSubjectExams = (subj) => {
  currentSubject.value = subj
  filters.value = { term: '', status: '', grade: '', class: '' }
}

const submitPublish = async () => {
  if (!newExam.value.title) {
     ElMessage.error('请输入考试标题！')
     return
  }
  if (!newExam.value.timerange || newExam.value.timerange.length < 2) {
     ElMessage.error('请选择考试时间段！')
     return
  }
  
  try {
    const startTime = newExam.value.timerange[0]
    const endTime = newExam.value.timerange[1]
    // Extract class IDs: targets is array of [grade, class] arrays -> just take class labels for now
    const targetStr = newExam.value.targets.length > 0
      ? newExam.value.targets.map(arr => arr[arr.length - 1]).join(',')
      : '1,2'
    
    const durationMs = new Date(endTime) - new Date(startTime)
    const durationMinutes = Math.round(durationMs / 60000) || 120
    
    if (!editingExamId.value && newExam.value.paperMode === 'manual') {
      const configObj = {
        title: newExam.value.title,
        subject: currentSubject.value.name,
        targetClasses: targetStr,
        duration: durationMinutes,
        paperMode: 'manual',
        startTime: startTime,
        endTime: endTime
      };
      sessionStorage.setItem('pendingExamConfig', JSON.stringify(configObj));
      publishDialogVisible.value = false;
      router.push('/teacher/manual-paper');
      return;
    }

    if (!editingExamId.value && !autoConfigValid.value) {
      ElMessage.error('当前题库数量不足，无法按所选配置完成智能组卷')
      return
    }
     
    const payload = {
      title: newExam.value.title,
      subject: currentSubject.value.name,
      targetClasses: targetStr,
      duration: durationMinutes,
      paperMode: newExam.value.paperMode,
      autoConfig: newExam.value.paperMode === 'auto' ? newExam.value.autoConfig : null,
      startTime: startTime,
      endTime: endTime
    }
    
    if (editingExamId.value) {
      await request.put(`/teacher/exams/${editingExamId.value}`, payload)
      ElMessage.success('考试修改成功！')
    } else {
      await request.post('/teacher/exams', payload)
      ElMessage.success('考试发布成功！')
    }
     
     closePublishDialog()
     fetchExams()
    } catch (e) {
     ElMessage.error((editingExamId.value ? '修改失败: ' : '发布失败: ') + (e?.message || ''))
  }
}

// Handlers for exam lifecycle
const handleMonitor = (row) => {
  ElMessage.info(`正在接入考场监控大屏: ${row.title}`)
}

const handleTerminate = (row) => {
  ElMessageBox.confirm('是否立即终止该场考试？交卷通道将被强制关闭！', '高危操作', { type: 'error' }).then(async () => {
    try {
      await request.put(`/teacher/exams/${row.id}/status?status=finished`)
      await fetchExams()
      ElMessage.success('考试已强制终止，试卷进入锁定状态。')
    } catch(e) { ElMessage.error('操作失败') }
  }).catch(() => {})
}

const handleEdit = (row) => {
  editingExamId.value = row.id
  newExam.value = {
    title: row.title,
    targets: buildTargetPaths(row.rawTargetClasses),
    timerange: [new Date(row.startTimeRaw), new Date(row.endTimeRaw)],
    difficulty: '',
    paperMode: 'auto',
    autoConfig: { single: 5, multiple: 2, judge: 3, subjective: 1 }
  }
  publishDialogVisible.value = true
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消并销毁该场考试安排吗？此操作不可逆！', '警告', { type: 'warning' }).then(async () => {
    try {
      await request.put(`/teacher/exams/${row.id}/status?status=cancelled`)
      await fetchExams()
      ElMessage.success('考试任务已被撤销。')
    } catch(e) { ElMessage.error('操作失败') }
  }).catch(() => {})
}

const exportReport = (row) => {
  ElMessage.success('正在后台生成并下载该场考试的详细分析报告(Excel/PDF)...')
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 24px; }
.mt-4 { margin-top: 20px; }

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
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px dashed var(--glass-border);
}
</style>
