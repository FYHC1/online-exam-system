<template>
  <div class="admin-settings">
    <div class="glass-card mb-4 header-card">
      <div>
        <h2>系统配置</h2>
        <p>统一管理组织架构、当前学期、登录页轮播图以及操作日志，确保平台基础配置清晰可控。</p>
      </div>
      <div class="summary-grid">
        <div class="summary-item">
          <div class="summary-value">{{ orgStats.departments }}</div>
          <div class="summary-label">院系数量</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ orgStats.classes }}</div>
          <div class="summary-label">班级数量</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ auditLogs.length }}</div>
          <div class="summary-label">日志记录数</div>
        </div>
      </div>
    </div>

    <div class="glass-card p-0">
      <el-tabs v-model="activeTab" class="custom-tabs custom-padding">
        <el-tab-pane label="组织架构" name="org">
          <div class="p-4">
            <el-row :gutter="24">
              <el-col :span="10">
                <div class="section-card">
                  <div class="section-header">
                    <div>
                      <h3>组织架构选择</h3>
                      <p>通过院系、专业、班级三级选择快速定位目标节点，适合院系和班级较多时管理。</p>
                    </div>
                    <el-button type="primary" size="small" @click="openAddNodeDialog">新增节点</el-button>
                  </div>

                  <div class="org-selector-grid">
                    <el-form label-position="top">
                      <el-form-item label="院系">
                        <el-select v-model="selectedDepartment" placeholder="请选择院系" style="width: 100%">
                          <el-option v-for="department in departmentOptions" :key="department.name" :label="department.name" :value="department.name" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="专业">
                        <el-select v-model="selectedMajor" placeholder="请选择专业" style="width: 100%">
                          <el-option v-for="major in majorOptions" :key="major.name" :label="major.name" :value="major.name" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="班级">
                        <el-select v-model="selectedClass" placeholder="请选择班级" style="width: 100%" clearable>
                          <el-option v-for="classItem in classOptions" :key="classItem.name" :label="classItem.name" :value="classItem.name" />
                        </el-select>
                      </el-form-item>
                    </el-form>
                  </div>

                  <div class="tree-panel compact-list">
                    <div class="tree-node root">当前层级概览</div>
                    <div class="structure-list">
                      <div class="structure-item">
                        <span class="structure-label">院系</span>
                        <div class="structure-value">
                          {{ currentDepartment?.name || '未选择' }}
                          <el-tag size="small" :type="currentDepartment?.archived ? 'info' : 'success'">{{ currentDepartment?.archived ? '归档' : '启用' }}</el-tag>
                        </div>
                      </div>
                      <div class="structure-item">
                        <span class="structure-label">专业</span>
                        <div class="structure-value">{{ currentMajor?.name || '未选择' }}</div>
                      </div>
                      <div class="structure-item">
                        <span class="structure-label">班级</span>
                        <div class="structure-value">{{ currentClass?.name || '未选择' }}</div>
                      </div>
                    </div>

                    <div class="class-list" v-if="classOptions.length">
                      <div class="class-list-title">当前专业下的班级</div>
                      <div
                        v-for="classItem in classOptions"
                        :key="classItem.name"
                        class="class-chip"
                        :class="{ active: selectedClass === classItem.name }"
                        @click="selectedClass = classItem.name"
                      >
                        {{ classItem.name }}
                        <span class="class-chip-count">{{ classItem.count }} 人</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>

              <el-col :span="14">
                <div class="section-card highlight-card">
                  <div class="section-header simple">
                    <div>
                      <h3>节点信息维护</h3>
                      <p>修改当前选中节点的名称、上级节点和状态。</p>
                    </div>
                  </div>

                  <el-form label-position="top" :model="orgForm">
                    <el-row :gutter="16">
                      <el-col :span="12">
                        <el-form-item label="节点名称">
                          <el-input v-model="orgForm.name" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="节点类型">
                          <el-select v-model="orgForm.type" style="width: 100%">
                            <el-option label="院系" value="院系" />
                            <el-option label="专业" value="专业" />
                            <el-option label="班级" value="班级" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                    </el-row>

                    <el-row :gutter="16">
                      <el-col :span="12">
                        <el-form-item label="上级节点">
                          <el-select v-model="orgForm.parent" style="width: 100%">
                            <el-option v-for="parent in parentOptions" :key="parent" :label="parent" :value="parent" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="节点状态">
                          <el-switch v-model="orgForm.enabled" active-text="启用" inactive-text="停用" />
                        </el-form-item>
                      </el-col>
                    </el-row>

                    <el-form-item label="备注">
                      <el-input v-model="orgForm.remark" type="textarea" :rows="4" />
                    </el-form-item>

                    <div class="action-row">
                      <el-button @click="resetOrgForm">取消</el-button>
                      <el-button type="primary" @click="saveOrgForm">保存修改</el-button>
                    </div>
                  </el-form>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="学期设置" name="term">
          <div class="p-4">
            <el-alert title="切换当前学期后，新发布考试将默认归属到该学期，历史成绩筛选也会按学期区分显示。" type="warning" show-icon class="mb-4" />

            <div class="section-card">
              <div class="section-header">
                <div>
                  <h3>当前学期设置</h3>
                  <p>设置系统当前使用的默认学期与对应时间范围，并可新增学期。</p>
                </div>
                <el-button type="primary" @click="openAddTermDialog">新增学期</el-button>
              </div>

              <el-form :model="termForm" label-width="120px">
                <el-form-item label="当前学期">
                  <el-select v-model="termForm.name" style="width: 320px">
                    <el-option v-for="term in termOptions" :key="term.name" :label="term.name" :value="term.name" />
                  </el-select>
                </el-form-item>
                <el-form-item label="学期时间">
                  <el-date-picker v-model="termForm.range" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 360px" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveCurrentTerm">保存并应用</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="登录页轮播图" name="ui">
          <div class="p-4">
            <div class="section-card">
              <div class="section-header">
                <div>
                  <h3>登录页轮播图管理</h3>
                  <p>维护登录页展示的轮播图片内容与排序。</p>
                </div>
                <el-button type="primary">新增轮播图</el-button>
              </div>

              <div class="carousel-list">
                <div class="carousel-item active">
                  <div class="carousel-title">校园迎新宣传海报</div>
                  <div class="carousel-actions">
                    <el-button link type="primary">编辑</el-button>
                    <el-button link type="danger">删除</el-button>
                  </div>
                </div>
                <div class="carousel-item">
                  <div class="carousel-title">诚信考试倡议海报</div>
                  <div class="carousel-actions">
                    <el-button link type="primary">编辑</el-button>
                    <el-button link type="danger">删除</el-button>
                  </div>
                </div>
                <div class="carousel-item empty">
                  <span>点击新增新的轮播图资源</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="操作日志" name="logs">
          <div class="p-4">
            <div class="section-card">
              <div class="section-header">
                <div>
                  <h3>操作日志</h3>
                  <p>记录系统关键操作，便于追踪高风险变更。</p>
                </div>
                <el-input v-model="logKeyword" placeholder="搜索操作人或日志内容" clearable style="width: 260px" />
              </div>

              <el-table :data="filteredAuditLogs" size="small" :border="false" stripe style="width: 100%">
                <el-table-column prop="time" label="操作时间" width="170" />
                <el-table-column prop="actor" label="操作人" width="160" />
                <el-table-column prop="action" label="操作内容" show-overflow-tooltip />
                <el-table-column prop="ip" label="来源 IP" width="140" />
                <el-table-column label="风险等级" width="110" align="center">
                  <template #default="scope">
                    <el-tag size="small" :type="scope.row.risk ? 'danger' : 'info'">
                      {{ scope.row.risk ? '高风险' : '常规' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="addTermDialogVisible" title="新增学期" width="520px">
      <el-form :model="addTermForm" label-position="top">
        <el-form-item label="学年起始年份">
          <el-select v-model="addTermForm.startYear" style="width: 100%">
            <el-option v-for="year in availableYears" :key="year" :label="`${year}-${year + 1} 学年`" :value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期类型">
          <el-select v-model="addTermForm.semester" style="width: 100%">
            <el-option label="第一学期" value="第一学期" />
            <el-option label="第二学期" value="第二学期" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期时间">
          <el-date-picker v-model="addTermForm.range" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addTermDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddTerm">确认新增</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addNodeDialogVisible" title="新增组织节点" width="520px">
      <el-form :model="addNodeForm" label-position="top">
        <el-form-item label="节点类型">
          <el-select v-model="addNodeForm.type" style="width: 100%">
            <el-option label="院系" value="院系" />
            <el-option label="专业" value="专业" />
            <el-option label="班级" value="班级" />
          </el-select>
        </el-form-item>

        <el-form-item label="节点名称">
          <el-input v-model="addNodeForm.name" placeholder="请输入节点名称" />
        </el-form-item>

        <el-form-item v-if="addNodeForm.type !== '院系'" :label="addNodeForm.type === '专业' ? '所属院系' : '所属专业'">
          <el-select v-model="addNodeForm.parent" style="width: 100%" :placeholder="addNodeForm.type === '专业' ? '请选择院系' : '请选择专业'">
            <el-option v-for="parent in addNodeParentOptions" :key="parent" :label="parent" :value="parent" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="addNodeForm.type === '班级'" label="班级人数">
          <el-input-number v-model="addNodeForm.count" :min="0" style="width: 100%" />
        </el-form-item>

        <el-form-item v-if="addNodeForm.type === '院系'" label="节点状态">
          <el-switch v-model="addNodeForm.enabled" active-text="启用" inactive-text="归档" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addNodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddNode">确认新增</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('org')
const logKeyword = ref('')
const addNodeDialogVisible = ref(false)
const addTermDialogVisible = ref(false)

const createDefaultAddNodeForm = () => ({
  type: '院系',
  name: '',
  parent: '',
  count: 0,
  enabled: true
})

const addNodeForm = ref(createDefaultAddNodeForm())

const orgTree = ref([
  {
    name: '计算机与信息学院',
    archived: false,
    majors: [
      {
        name: '软件工程',
        classes: [
          { name: '软件工程 1 班', count: 45 },
          { name: '软件工程 2 班', count: 47 }
        ]
      },
      {
        name: '计算机科学与技术',
        classes: [
          { name: '计科 1 班', count: 42 }
        ]
      }
    ]
  },
  {
    name: '理学院',
    archived: false,
    majors: [
      {
        name: '数学与应用数学',
        classes: [
          { name: '数学 1 班', count: 39 }
        ]
      }
    ]
  },
  {
    name: '历史归档院系',
    archived: true,
    majors: []
  }
])

const selectedDepartment = ref(orgTree.value[0]?.name || '')
const selectedMajor = ref(orgTree.value[0]?.majors?.[0]?.name || '')
const selectedClass = ref(orgTree.value[0]?.majors?.[0]?.classes?.[0]?.name || '')

const orgStats = computed(() => ({
  departments: orgTree.value.length,
  classes: orgTree.value.reduce((sum, department) => sum + department.majors.reduce((majorSum, major) => majorSum + major.classes.length, 0), 0)
}))

const orgForm = ref({
  name: '软件工程 1 班',
  type: '班级',
  parent: '计算机与信息学院',
  enabled: true,
  remark: '当前为软件工程专业核心班级，已关联 45 名学生。'
})

const departmentOptions = computed(() => orgTree.value)
const currentDepartment = computed(() => orgTree.value.find(item => item.name === selectedDepartment.value) || null)
const majorOptions = computed(() => currentDepartment.value?.majors || [])
const currentMajor = computed(() => majorOptions.value.find(item => item.name === selectedMajor.value) || null)
const classOptions = computed(() => currentMajor.value?.classes || [])
const currentClass = computed(() => classOptions.value.find(item => item.name === selectedClass.value) || null)

const parentOptions = computed(() => {
  if (orgForm.value.type === '院系') {
    return ['在线考试系统']
  }
  if (orgForm.value.type === '专业') {
    return orgTree.value.filter(item => !item.archived).map(item => item.name)
  }
  return majorOptions.value.map(item => item.name)
})

const addNodeParentOptions = computed(() => {
  if (addNodeForm.value.type === '专业') {
    return orgTree.value.map(item => item.name)
  }
  if (addNodeForm.value.type === '班级') {
    return orgTree.value.flatMap(item => item.majors.map(major => major.name))
  }
  return []
})

watch(currentDepartment, (department) => {
  if (!department) return
  selectedMajor.value = department.majors?.[0]?.name || ''
})

watch(currentMajor, (major) => {
  if (!major) {
    selectedClass.value = ''
    return
  }
  selectedClass.value = major.classes?.[0]?.name || ''
})

watch([currentDepartment, currentMajor, currentClass], ([department, major, classItem]) => {
  if (classItem) {
    orgForm.value = {
      name: classItem.name,
      type: '班级',
      parent: major?.name || '',
      enabled: !department?.archived,
      remark: `当前班级已关联 ${classItem.count} 名学生。`
    }
    return
  }

  if (major) {
    orgForm.value = {
      name: major.name,
      type: '专业',
      parent: department?.name || '',
      enabled: !department?.archived,
      remark: '当前专业包含多个教学班级。'
    }
    return
  }

  if (department) {
    orgForm.value = {
      name: department.name,
      type: '院系',
      parent: '在线考试系统',
      enabled: !department.archived,
      remark: department.archived ? '该院系已归档，仅保留历史数据。' : '当前院系正常启用。'
    }
  }
}, { immediate: true })

watch(() => addNodeForm.value.type, (type) => {
  addNodeForm.value.parent = ''
  if (type !== '班级') {
    addNodeForm.value.count = 0
  }
})

const openAddNodeDialog = () => {
  addNodeForm.value = createDefaultAddNodeForm()
  addNodeDialogVisible.value = true
}

const loadOrgStructure = async () => {
  try {
    const data = await request.get('/admin/org-structure')
    orgTree.value = data
    selectedDepartment.value = orgTree.value[0]?.name || ''
    selectedMajor.value = orgTree.value[0]?.majors?.[0]?.name || ''
    selectedClass.value = orgTree.value[0]?.majors?.[0]?.classes?.[0]?.name || ''
  } catch (e) {
    ElMessage.error('获取组织架构失败')
  }
}

const submitAddNode = async () => {
  if (!addNodeForm.value.name) {
    ElMessage.error('请输入节点名称')
    return
  }

  if (addNodeForm.value.type !== '院系' && !addNodeForm.value.parent) {
    ElMessage.error(`请选择所属${addNodeForm.value.type === '专业' ? '院系' : '专业'}`)
    return
  }

  try {
    await request.post('/admin/org-structure/nodes', addNodeForm.value)
    await loadOrgStructure()
    if (addNodeForm.value.type === '院系') {
      selectedDepartment.value = addNodeForm.value.name
      selectedMajor.value = ''
      selectedClass.value = ''
    } else if (addNodeForm.value.type === '专业') {
      selectedMajor.value = addNodeForm.value.name
      selectedClass.value = ''
    } else {
      selectedClass.value = addNodeForm.value.name
    }
    addNodeDialogVisible.value = false
    ElMessage.success('组织节点新增成功')
  } catch (e) {
    ElMessage.error('组织节点新增失败')
  }
}

const resetOrgForm = () => {
  const department = currentDepartment.value
  const major = currentMajor.value
  const classItem = currentClass.value

  if (classItem) {
    orgForm.value = {
      name: classItem.name,
      type: '班级',
      parent: major?.name || '',
      enabled: !department?.archived,
      remark: `当前班级已关联 ${classItem.count} 名学生。`
    }
    return
  }

  if (major) {
    orgForm.value = {
      name: major.name,
      type: '专业',
      parent: department?.name || '',
      enabled: !department?.archived,
      remark: '当前专业包含多个教学班级。'
    }
    return
  }

  if (department) {
    orgForm.value = {
      name: department.name,
      type: '院系',
      parent: '在线考试系统',
      enabled: !department.archived,
      remark: department.archived ? '该院系已归档，仅保留历史数据。' : '当前院系正常启用。'
    }
  }
}

const saveOrgForm = async () => {
  try {
    await request.put('/admin/org-structure/nodes', {
      type: orgForm.value.type,
      name: orgForm.value.name,
      enabled: orgForm.value.enabled,
      currentDepartment: currentDepartment.value?.name,
      currentMajor: currentMajor.value?.name,
      currentClass: currentClass.value?.name
    })
    await loadOrgStructure()
    if (orgForm.value.type === '院系') {
      selectedDepartment.value = orgForm.value.name
    } else if (orgForm.value.type === '专业') {
      selectedMajor.value = orgForm.value.name
    } else {
      selectedClass.value = orgForm.value.name
    }
    ElMessage.success('节点信息已保存')
  } catch (e) {
    ElMessage.error('节点信息保存失败')
  }
}

onMounted(() => {
  loadOrgStructure()
  loadTermSettings()
})

const termForm = ref({
  name: '2023-2024 学年第一学期',
  range: []
})

const availableYears = Array.from({ length: 8 }, (_, index) => 2020 + index)
const createDefaultTerms = () => {
  const terms = []
  availableYears.forEach(year => {
    terms.push({ name: `${year}-${year + 1} 学年第一学期`, range: [] })
    terms.push({ name: `${year}-${year + 1} 学年第二学期`, range: [] })
  })
  return terms
}

const termOptions = ref(createDefaultTerms())
const addTermForm = ref({
  startYear: 2023,
  semester: '第一学期',
  range: []
})

const loadTermSettings = async () => {
  try {
    const data = await request.get('/admin/terms')
    termOptions.value = data.terms || createDefaultTerms()
    termForm.value.name = data.currentTerm || termOptions.value[0]?.name || ''
    termForm.value.range = data.currentRange || []
  } catch (e) {
    ElMessage.error('获取学期设置失败')
  }
}

const openAddTermDialog = () => {
  addTermForm.value = {
    startYear: new Date().getFullYear(),
    semester: '第一学期',
    range: []
  }
  addTermDialogVisible.value = true
}

const submitAddTerm = async () => {
  try {
    await request.post('/admin/terms', addTermForm.value)
    await loadTermSettings()
    addTermDialogVisible.value = false
    ElMessage.success('学期新增成功')
  } catch (e) {
    ElMessage.error(e?.message || '学期新增失败')
  }
}

const saveCurrentTerm = async () => {
  try {
    await request.put('/admin/terms/current', {
      name: termForm.value.name,
      range: termForm.value.range
    })
    ElMessage.success('当前学期已更新')
  } catch (e) {
    ElMessage.error('当前学期保存失败')
  }
}

const auditLogs = ref([
  { time: '2023-11-28 14:32:01', actor: '管理员 admin1', action: '将当前学期切换为 2023-2024 学年第一学期', ip: '192.168.1.1', risk: true },
  { time: '2023-11-28 14:00:15', actor: '教师 teacherL', action: '查看试卷资源详情：JavaWeb 期中考试', ip: '10.0.8.44', risk: false },
  { time: '2023-11-28 13:59:12', actor: '管理员 admin1', action: '删除学生账户 100003', ip: '192.168.1.1', risk: true },
  { time: '2023-11-28 13:40:00', actor: '学生 studentA', action: '登录系统成功', ip: '233.12.99.1', risk: false }
])

const filteredAuditLogs = computed(() => {
  return auditLogs.value.filter(item =>
    !logKeyword.value || item.actor.includes(logKeyword.value) || item.action.includes(logKeyword.value)
  )
})
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.custom-padding :deep(.el-tabs__content) { padding: 0; }
:deep(.el-tabs__nav-wrap) { padding: 0 20px; border-bottom: 1px solid var(--glass-border); }
:deep(.el-tabs__item) { height: 50px; line-height: 50px; font-size: 15px; transition: color 0.3s; }
:deep(.el-tabs__item.is-active) { color: var(--primary-color) !important; font-weight: 600; }
:deep(.el-tabs__active-bar) { background-color: var(--primary-color) !important; height: 3px; border-radius: 3px; }

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
  min-width: 130px;
  padding: 16px 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.42);
  border: 1px solid var(--glass-border);
  text-align: center;
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

.p-4 { padding: 24px; }

.section-card {
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.32);
  border: 1px solid var(--glass-border);
}

.highlight-card {
  background: rgba(var(--primary-color-rgb), 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.section-header.simple {
  justify-content: flex-start;
}

.section-header h3 {
  margin: 0;
}

.section-header p {
  margin: 6px 0 0;
  color: var(--text-secondary);
}

.tree-panel {
  border-radius: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.28);
}

.org-selector-grid {
  margin-bottom: 16px;
}

.compact-list {
  margin-top: 12px;
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(var(--primary-color-rgb), 0.08);
  font-weight: 600;
}

.tree-node.root {
  background: rgba(var(--primary-color-rgb), 0.14);
  color: var(--primary-color);
}

.tree-node.sub {
  margin-top: 10px;
  background: rgba(255, 255, 255, 0.45);
  color: var(--text-primary);
}

.tree-children {
  margin-top: 12px;
  margin-left: 18px;
  padding-left: 16px;
  border-left: 1px dashed var(--glass-border);
}

.tree-block {
  margin-bottom: 14px;
}

.tree-block.archived {
  opacity: 0.65;
}

.tree-leaf {
  margin-top: 10px;
  padding-left: 8px;
  color: var(--text-secondary);
}

.structure-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.structure-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.4);
}

.structure-label {
  color: var(--text-secondary);
  font-size: 13px;
}

.structure-value {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.class-list {
  margin-top: 18px;
}

.class-list-title {
  margin-bottom: 10px;
  font-weight: 600;
}

.class-chip {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.35);
  border: 1px solid transparent;
  cursor: pointer;
  margin-bottom: 8px;
}

.class-chip.active,
.class-chip:hover {
  border-color: var(--primary-color);
  background: rgba(var(--primary-color-rgb), 0.08);
}

.class-chip-count {
  color: var(--text-secondary);
  font-size: 13px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.carousel-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.carousel-item {
  min-height: 120px;
  border-radius: 14px;
  padding: 16px;
  border: 1px dashed rgba(var(--primary-color-rgb), 0.35);
  background: rgba(var(--primary-color-rgb), 0.06);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.carousel-item.active {
  border-style: solid;
  background: rgba(var(--primary-color-rgb), 0.14);
}

.carousel-item.empty {
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.carousel-title {
  font-weight: 600;
}

.carousel-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }
</style>
