<template>
  <div class="user-manage">
    <!-- View 1: Role Selection -->
    <div v-if="!currentRole" class="role-view">
      <div class="glass-card mb-4" style="padding: 20px;">
        <h2>系统用户管理</h2>
        <p style="color: var(--text-secondary); margin-top: 8px;">按用户角色分类浏览并管理系统人员账号与权限设置。</p>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="role in roles" :key="role.id">
          <div class="glass-card role-card" @click="enterRoleCategory(role)">
            <div class="icon-wrap" :style="{ background: role.color }">
              <i :class="role.icon"></i>
            </div>
            <div class="role-info">
              <h3>{{ role.name }}</h3>
              <p>系统注册人数: {{ role.count }} 名</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- View 2: Specific Role User Table -->
    <div v-else class="detail-view">
      <div class="glass-card mb-4 top-bar">
        <el-button icon="el-icon-back" circle @click="currentRole = null" />
        <h2 style="margin: 0;">{{ currentRole.name }} - 用户列表</h2>
      </div>

      <div class="glass-card filter-bar mb-4">
        <el-form :inline="true" :model="filters">
          <el-form-item label="搜索：">
            <el-input v-model="filters.keyword" placeholder="查询账号/姓名" clearable style="width: 140px" />
          </el-form-item>
          
          <!-- STUDENT ONLY FILTERS -->
          <template v-if="currentRole.id === 'student'">
            <el-form-item label="年级：">
              <el-select v-model="filters.grade" placeholder="全部" clearable style="width: 100px">
                <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade" />
              </el-select>
            </el-form-item>
            <el-form-item label="学院：">
              <el-select v-model="filters.college" placeholder="全部" clearable style="width: 120px">
                <el-option v-for="department in allDepartmentOptions" :key="department" :label="department" :value="department" />
              </el-select>
            </el-form-item>
            <el-form-item label="专业：">
              <el-select v-model="filters.major" placeholder="全部" clearable style="width: 120px">
                <el-option v-for="major in allMajorOptions" :key="major" :label="major" :value="major" />
              </el-select>
            </el-form-item>
            <el-form-item label="班级：">
              <el-select v-model="filters.class" placeholder="全部" clearable style="width: 100px">
                <el-option v-for="className in classLabelOptions" :key="className" :label="className" :value="className" />
              </el-select>
            </el-form-item>
          </template>

          <!-- TEACHER ONLY FILTERS -->
          <template v-if="currentRole.id === 'teacher'">
            <el-form-item label="所属学院：">
              <el-select v-model="filters.college" placeholder="全部学院" clearable style="width: 150px">
                <el-option v-for="department in allDepartmentOptions" :key="department" :label="department" :value="department" />
              </el-select>
            </el-form-item>
          </template>

          <el-form-item label="状态：">
            <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option label="正常 (1)" :value="1" />
              <el-option label="禁用 (0)" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item style="margin-right: 0; margin-left: auto;">
            <el-button type="success" @click="handleAddUser"><i class="el-icon-plus" /> 添加用户</el-button>
            <el-button plain type="primary">批量导入</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="glass-card p-0">
        <el-table :data="filteredUsers" style="width: 100%" class="custom-table" border="false">
          <el-table-column prop="id" label="用户 ID" width="130" align="center" />
          <el-table-column prop="username" label="登录账号" width="180" />
          <el-table-column prop="realName" label="姓名" width="150" />
          <el-table-column prop="role" label="用户角色" width="120">
            <template #default="scope">
              <el-tag :type="getRoleTagType(scope.row.role)">
                {{ getRoleName(scope.row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="className" label="所属班级/院系" show-overflow-tooltip />
          <el-table-column prop="status" label="账号状态" width="120">
            <template #default="scope">
              <el-switch :model-value="scope.row.status === 1" active-color="#10b981" inactive-color="#ef4444" @change="toggleStatus(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleEdit(scope.row)">修改</el-button>
              <el-button link type="warning" size="small" @click="handleResetPassword(scope.row)">重置密码</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination background layout="prev, pager, next, total" :total="filteredUsers.length" />
        </div>
      </div>
    </div>

    <!-- 弹窗：添加用户 -->
    <el-dialog v-model="addDialogVisible" title="添加新用户" width="500px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="登录账号">
          <el-input v-model="addForm.username" placeholder="请输入账号（学号/工号）" />
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="addForm.password" placeholder="默认123456" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="addForm.phone" placeholder="（选填）联系方式" />
        </el-form-item>
        <el-form-item label="角色分布">
          <el-radio-group v-model="addForm.role">
            <el-radio label="student">普通学生</el-radio>
            <el-radio label="teacher">教职人员</el-radio>
            <el-radio label="admin">系统管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="addForm.role === 'student'">
          <el-form-item label="年级">
            <el-select v-model="addForm.grade" placeholder="请选择年级" clearable style="width:100%">
              <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade" />
            </el-select>
          </el-form-item>
          <el-form-item label="学院">
            <el-select v-model="addForm.department" placeholder="请选择学院" clearable style="width:100%">
              <el-option v-for="department in departmentOptions" :key="department" :label="department" :value="department" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业">
            <el-select v-model="addForm.major" placeholder="请选择专业" clearable style="width:100%">
              <el-option v-for="major in majorOptions" :key="major" :label="major" :value="major" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="addForm.classId" placeholder="请选择班级" clearable style="width:100%">
              <el-option v-for="cls in classOptions" :key="cls.classId" :label="cls.className" :value="cls.classId" />
            </el-select>
          </el-form-item>
          <div style="margin-top:-10px; margin-bottom: 12px; color: var(--text-secondary); font-size: 12px;">
            学号前四位若为年份，将自动优先匹配对应年级。
          </div>
        </template>
        <el-form-item label="所属学院" v-else-if="addForm.role === 'teacher'">
          <el-select v-model="addForm.department" placeholder="请选择学院" clearable style="width:100%">
            <el-option v-for="department in allDepartmentOptions" :key="department" :label="department" :value="department" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddUser">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：编辑用户 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="登录账号">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <template v-if="editForm.role === 'student'">
          <el-form-item label="年级">
            <el-select v-model="editForm.grade" placeholder="请选择年级" clearable style="width:100%">
              <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade" />
            </el-select>
          </el-form-item>
          <el-form-item label="学院">
            <el-select v-model="editForm.department" placeholder="请选择学院" clearable style="width:100%">
              <el-option v-for="department in editDepartmentOptions" :key="department" :label="department" :value="department" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业">
            <el-select v-model="editForm.major" placeholder="请选择专业" clearable style="width:100%">
              <el-option v-for="major in editMajorOptions" :key="major" :label="major" :value="major" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="editForm.classId" placeholder="请选择班级" clearable style="width:100%">
              <el-option v-for="cls in editClassOptions" :key="cls.classId" :label="cls.className" :value="cls.classId" />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item label="所属学院" v-else-if="editForm.role === 'teacher'">
          <el-select v-model="editForm.department" placeholder="请选择学院" clearable style="width:100%">
            <el-option v-for="department in allDepartmentOptions" :key="department" :label="department" :value="department" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const currentRole = ref(null)
const filters = ref({ keyword: '', status: '', grade: '', college: '', major: '', class: '' })

// Edit dialog state
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)
const editForm = ref({ userId: null, username: '', realName: '', phone: '', status: 1, role: '', classId: null, grade: '', department: '', major: '' })
const addForm = ref({ username: '', password: '123456', realName: '', phone: '', role: '', classId: null, status: 1, grade: '', department: '', major: '' })
const suppressEditCascadeReset = ref(false)
const editFormPrefill = ref({ department: '', major: '', classId: null })

const roles = ref([
  { id: 'student', name: '学生用户', count: '多', icon: 'el-icon-reading', color: 'rgba(56, 189, 248, 0.2)' },
  { id: 'teacher', name: '教师名单', count: '多', icon: 'el-icon-user-solid', color: 'rgba(16, 185, 129, 0.2)' },
  { id: 'admin', name: '系统管理员', count: '多', icon: 'el-icon-s-custom', color: 'rgba(245, 158, 11, 0.2)' }
])

const allUsers = ref([])
const allClasses = ref([])
const earliestGradeYear = ref(2020)

const inferGradeFromUsername = (username) => {
  const matched = String(username || '').match(/^(20\d{2})/)
  return matched ? `${matched[1]}级` : ''
}

const getGradeFromClass = (cls) => {
  if (!cls?.createTime) return `${earliestGradeYear.value}级`
  return `${new Date(cls.createTime).getFullYear()}级`
}

const gradeOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  const startYear = Math.min(earliestGradeYear.value || 2020, currentYear)
  return Array.from({ length: currentYear - startYear + 1 }, (_, index) => `${startYear + index}级`)
})
const allDepartmentOptions = computed(() => [...new Set(allClasses.value.map(item => item.department).filter(Boolean))])
const allMajorOptions = computed(() => [...new Set(allClasses.value.map(item => item.major).filter(Boolean))])
const departmentOptions = computed(() => {
  return [...new Set(allClasses.value.map(item => item.department).filter(Boolean))]
})
const majorOptions = computed(() => {
  const { department } = addForm.value
  return [...new Set(allClasses.value
    .filter(item => !department || item.department === department)
    .map(item => item.major)
    .filter(Boolean))]
})
const classOptions = computed(() => {
  const { department, major } = addForm.value
  return allClasses.value.filter(item =>
    (!department || item.department === department) &&
    (!major || item.major === major)
  )
})
const classLabelOptions = computed(() => [...new Set(allClasses.value.map(item => item.className).filter(Boolean))])
const editDepartmentOptions = computed(() => {
  return [...new Set(allClasses.value.map(item => item.department).filter(Boolean))]
})
const editMajorOptions = computed(() => {
  const { department } = editForm.value
  return [...new Set(allClasses.value
    .filter(item => !department || item.department === department)
    .map(item => item.major)
    .filter(Boolean))]
})
const editClassOptions = computed(() => {
  const { department, major } = editForm.value
  return allClasses.value.filter(item =>
    (!department || item.department === department) &&
    (!major || item.major === major)
  )
})

const syncStudentGrade = () => {
  if (addForm.value.role !== 'student') return

  const inferredGrade = inferGradeFromUsername(addForm.value.username)
  const currentYearGrade = `${new Date().getFullYear()}级`

  if (inferredGrade && gradeOptions.value.includes(inferredGrade)) {
    addForm.value.grade = inferredGrade
    return
  }

  if (gradeOptions.value.includes(currentYearGrade)) {
    addForm.value.grade = currentYearGrade
    return
  }

  addForm.value.grade = gradeOptions.value[0] || ''
}

watch(() => addForm.value.username, (username) => {
  if (addForm.value.role !== 'student') return
  syncStudentGrade()
})

watch(gradeOptions, () => {
  syncStudentGrade()
})

watch(() => addForm.value.grade, () => {
  if (addForm.value.role !== 'student') return
  addForm.value.department = ''
  addForm.value.major = ''
  addForm.value.classId = null
})

watch(() => addForm.value.department, () => {
  if (addForm.value.role === 'student') {
    addForm.value.major = ''
    addForm.value.classId = null
    return
  }

  if (addForm.value.role === 'teacher') {
    const departmentClass = allClasses.value.find(item => item.department === addForm.value.department)
    addForm.value.classId = departmentClass?.classId || null
  }
})

watch(() => addForm.value.major, () => {
  if (addForm.value.role !== 'student') return
  addForm.value.classId = null
})

watch(() => addForm.value.role, (role) => {
  addForm.value.classId = null
  addForm.value.department = ''
  addForm.value.major = ''
  if (role === 'student') {
    syncStudentGrade()
  } else {
    addForm.value.grade = ''
  }
})

watch(() => editForm.value.grade, () => {
  if (editForm.value.role !== 'student') return
  if (suppressEditCascadeReset.value) return
  editForm.value.department = ''
  editForm.value.major = ''
  editForm.value.classId = null
  editFormPrefill.value = { department: '', major: '', classId: null }
})

watch(() => editForm.value.department, () => {
  if (suppressEditCascadeReset.value) return
  if (editForm.value.role === 'student') {
    editForm.value.major = ''
    editForm.value.classId = null
    editFormPrefill.value.classId = null
    return
  }

  if (editForm.value.role === 'teacher') {
    const departmentClass = allClasses.value.find(item => item.department === editForm.value.department)
    editForm.value.classId = departmentClass?.classId || null
  }
})

watch(() => editForm.value.major, () => {
  if (editForm.value.role !== 'student') return
  if (suppressEditCascadeReset.value) return
  editForm.value.classId = null
  editFormPrefill.value.classId = null
})

watch(editDepartmentOptions, (options) => {
  if (!editFormPrefill.value.department || !options.includes(editFormPrefill.value.department)) return
  editForm.value.department = editFormPrefill.value.department
})

watch(editMajorOptions, (options) => {
  if (!editFormPrefill.value.major || !options.includes(editFormPrefill.value.major)) return
  editForm.value.major = editFormPrefill.value.major
})

watch(editClassOptions, (options) => {
  if (!editFormPrefill.value.classId || !options.some(item => item.classId === editFormPrefill.value.classId)) return
  editForm.value.classId = editFormPrefill.value.classId
})

const fetchRoleCounts = async () => {
  try {
    const [studentUsers, teacherUsers, adminUsers] = await Promise.all([
      request.get('/admin/users?role=student'),
      request.get('/admin/users?role=teacher'),
      request.get('/admin/users?role=admin')
    ])

    roles.value = roles.value.map(role => ({
      ...role,
      count: role.id === 'student' ? studentUsers.length : (role.id === 'teacher' ? teacherUsers.length : adminUsers.length)
    }))
  } catch (e) {
    ElMessage.error('获取用户统计失败')
  }
}

const fetchClasses = async () => {
  try {
    allClasses.value = await request.get('/admin/classes')
  } catch (e) {
    ElMessage.error('获取班级信息失败')
  }
}

const fetchGradeRange = async () => {
  try {
    const termSettings = await request.get('/admin/terms')
    const termNames = (termSettings.terms || []).map(item => item.name)
    const years = termNames
      .map(name => Number(String(name).match(/^(20\d{2})-/)?.[1]))
      .filter(year => !Number.isNaN(year))

    if (years.length) {
      earliestGradeYear.value = Math.min(...years)
    }
  } catch (e) {
    earliestGradeYear.value = 2020
  }
}

const fetchUsers = async () => {
  if (!currentRole.value) return;
  try {
    // Backend expects 'student', 'teacher', 'admin' - not ROLE_ prefix
    const res = await request.get(`/admin/users?role=${currentRole.value.id}`)
    allUsers.value = res.map(u => {
      const classInfo = allClasses.value.find(item => item.classId === u.classId)
      return {
        id: u.userId,
        username: u.username,
        realName: u.realName,
        phone: u.phone || '',
        role: currentRole.value.id,
        grade: classInfo ? getGradeFromClass(classInfo) : '',
        department: classInfo?.department || '',
        major: classInfo?.major || '',
        classOnly: classInfo?.className || '',
        college: classInfo?.department || '未分配',
        className: classInfo
          ? (currentRole.value.id === 'teacher'
              ? classInfo.department
              : `${getGradeFromClass(classInfo)} / ${classInfo.department} / ${classInfo.major} / ${classInfo.className}`)
          : '未分配',
        status: u.status,
        classId: u.classId
      }
    })
  } catch(e) { 
    ElMessage.error('获取用户列表失败') 
  }
}

const filteredUsers = computed(() => {
  if (!currentRole.value) return []
  return allUsers.value.filter(u => {
    return (!filters.value.keyword || u.username.includes(filters.value.keyword) || u.realName.includes(filters.value.keyword)) &&
           (filters.value.status === '' || u.status === filters.value.status) &&
           (!filters.value.grade || u.grade === filters.value.grade) &&
           (!filters.value.college || u.department === filters.value.college) &&
           (!filters.value.major || u.major === filters.value.major) &&
           (!filters.value.class || u.classOnly === filters.value.class)
  })
})

const enterRoleCategory = (role) => {
  currentRole.value = role
  filters.value = { keyword: '', status: '', grade: '', college: '', major: '', class: '' }
  fetchUsers()
}

const getRoleTagType = (role) => {
  if (role === 'student') return 'info'
  if (role === 'teacher') return 'success'
  return 'warning'
}

const getRoleName = (role) => {
  if (role === 'student') return '学生'
  if (role === 'teacher') return '教职工'
  return '系统管理'
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put('/admin/users', { userId: row.id, status: newStatus })
    row.status = newStatus
    ElMessage.success(`账户 ${row.username} 已${newStatus === 1 ? '启用' : '禁用'}`)
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

const handleAddUser = () => {
  addForm.value = { username: '', password: '123456', realName: '', phone: '', role: currentRole.value.id, classId: null, status: 1, grade: '', department: '', major: '' }
  if (currentRole.value.id === 'student') {
    syncStudentGrade()
  }
  addDialogVisible.value = true
}

const submitAddUser = async () => {
  if (!addForm.value.username || !addForm.value.realName) {
    ElMessage.error('账号和姓名为必填项')
    return
  }
  if (addForm.value.role === 'student' && !addForm.value.classId) {
    ElMessage.error('请为学生选择完整的年级、学院、专业和班级')
    return
  }
  if (addForm.value.role === 'teacher' && !addForm.value.classId) {
    ElMessage.error('请为教师选择所属学院')
    return
  }
  try {
    const payload = {
      username: addForm.value.username,
      password: addForm.value.password,
      realName: addForm.value.realName,
      phone: addForm.value.phone,
      role: addForm.value.role,
      classId: addForm.value.role === 'student' ? addForm.value.classId : (addForm.value.role === 'teacher' ? addForm.value.classId : null),
      status: addForm.value.status
    }
    await request.post('/admin/users', payload)
    ElMessage.success('用户添加成功')
    addDialogVisible.value = false
    fetchRoleCounts()
    fetchUsers()
  } catch (e) {
    ElMessage.error('添加失败: ' + (e?.message || ''))
  }
}

const handleEdit = (row) => {
  suppressEditCascadeReset.value = true
  editFormPrefill.value = {
    department: row.department || '',
    major: row.major || '',
    classId: row.classId || null
  }
  editForm.value = {
    userId: row.id,
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    status: row.status,
    role: row.role,
    classId: row.classId,
    grade: row.grade || '',
    department: row.department || '',
    major: row.major || ''
  }
  setTimeout(() => {
    suppressEditCascadeReset.value = false
  }, 0)
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    if (editForm.value.role === 'student' && !editForm.value.classId) {
      ElMessage.error('请为学生选择完整的年级、学院、专业和班级')
      return
    }
    if (editForm.value.role === 'teacher' && !editForm.value.classId) {
      ElMessage.error('请为教师选择所属学院')
      return
    }

    await request.put('/admin/users', {
      userId: editForm.value.userId,
      realName: editForm.value.realName,
      phone: editForm.value.phone,
      status: editForm.value.status,
      classId: editForm.value.role === 'admin' ? null : editForm.value.classId
    })
    ElMessage.success('用户信息已更新')
    editDialogVisible.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const handleResetPassword = (row) => {
  ElMessageBox.prompt('请输入新密码（留空则重置为 123456）', `重置 ${row.username} 的密码`, {
    confirmButtonText: '确认重置',
    cancelButtonText: '取消',
    inputValue: '123456'
  }).then(async ({ value }) => {
    try {
      await request.put('/admin/users', { userId: row.id, password: value || '123456' })
      ElMessage.success(`${row.username} 的密码已重置成功`)
    } catch(e) {
      ElMessage.error('重置失败')
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('删除此账户后相关数据将无法恢复，确定要删除吗？', '删除确认', {
    type: 'error',
    confirmButtonText: '确认删除'
  }).then(async () => {
    try {
      await request.delete(`/admin/users/${row.id}`)
      allUsers.value = allUsers.value.filter(u => u.id !== row.id)
      ElMessage.success('用户已成功删除。')
      fetchRoleCounts()
    } catch(e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(async () => {
  await fetchClasses()
  await fetchGradeRange()
  await fetchRoleCounts()
})
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.top-bar { padding: 20px; display: flex; align-items: center; gap: 16px; }

.role-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 20px;
}

.role-card:hover {
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

.role-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.role-info p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

:deep(.filter-bar) { padding: 20px 20px 0 20px; }
:deep(.el-form-item) { margin-bottom: 20px; }
:deep(.el-table) { background: transparent !important; border:none; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px dashed var(--glass-border);
}
</style>
