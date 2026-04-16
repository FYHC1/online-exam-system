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
                <el-option label="2021级" value="2021级" />
                <el-option label="2022级" value="2022级" />
                <el-option label="2023级" value="2023级" />
              </el-select>
            </el-form-item>
            <el-form-item label="学院：">
              <el-select v-model="filters.college" placeholder="全部" clearable style="width: 120px">
                <el-option label="计算机学院" value="计算机学院" />
                <el-option label="软件学院" value="软件学院" />
              </el-select>
            </el-form-item>
            <el-form-item label="专业：">
              <el-select v-model="filters.major" placeholder="全部" clearable style="width: 120px">
                <el-option label="计算机科学" value="计算机科学" />
                <el-option label="软件工程" value="软件工程" />
              </el-select>
            </el-form-item>
            <el-form-item label="班级：">
              <el-select v-model="filters.class" placeholder="全部" clearable style="width: 100px">
                <el-option label="1班" value="1班" />
                <el-option label="2班" value="2班" />
                <el-option label="A班" value="A班" />
                <el-option label="B班" value="B班" />
              </el-select>
            </el-form-item>
          </template>

          <!-- TEACHER ONLY FILTERS -->
          <template v-if="currentRole.id === 'teacher'">
            <el-form-item label="所属学院：">
              <el-select v-model="filters.college" placeholder="全部学院" clearable style="width: 150px">
                <el-option label="计算机与信息学院" value="计算机与信息学院" />
                <el-option label="外语学院" value="外语学院" />
                <el-option label="理学院" value="理学院" />
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
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级ID" v-if="addForm.role === 'student'">
          <el-input-number v-model="addForm.classId" :min="1" controls-position="right" style="width:100%" />
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
      <el-form-item label="账号状态">
        <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const currentRole = ref(null)
const filters = ref({ keyword: '', status: '', grade: '', college: '', major: '', class: '' })

// Edit dialog state
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)
const editForm = ref({ userId: null, username: '', realName: '', phone: '', status: 1 })
const addForm = ref({ username: '', password: '123456', realName: '', phone: '', role: '', classId: null, status: 1 })

const roles = ref([
  { id: 'student', name: '学生用户', count: '多', icon: 'el-icon-reading', color: 'rgba(56, 189, 248, 0.2)' },
  { id: 'teacher', name: '教师名单', count: '多', icon: 'el-icon-user-solid', color: 'rgba(16, 185, 129, 0.2)' },
  { id: 'admin', name: '系统管理员', count: '多', icon: 'el-icon-s-custom', color: 'rgba(245, 158, 11, 0.2)' }
])

const allUsers = ref([])

const fetchUsers = async () => {
  if (!currentRole.value) return;
  try {
    // Backend expects 'student', 'teacher', 'admin' - not ROLE_ prefix
    const res = await request.get(`/admin/users?role=${currentRole.value.id}`)
    allUsers.value = res.map(u => ({
      id: u.userId,
      username: u.username,
      realName: u.realName,
      phone: u.phone || '',
      role: currentRole.value.id,
      college: u.classId ? `班级ID:${u.classId}` : '未分配',
      className: u.classId ? `班级ID:${u.classId}` : '未分配',
      status: u.status,
      classId: u.classId
    }))
  } catch(e) { 
    ElMessage.error('获取用户列表失败') 
  }
}

const filteredUsers = computed(() => {
  if (!currentRole.value) return []
  return allUsers.value.filter(u => {
    return (!filters.value.keyword || u.username.includes(filters.value.keyword) || u.realName.includes(filters.value.keyword)) &&
           (filters.value.status === '' || u.status === filters.value.status)
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
  addForm.value = { username: '', password: '123456', realName: '', phone: '', role: currentRole.value.id, classId: null, status: 1 }
  addDialogVisible.value = true
}

const submitAddUser = async () => {
  if (!addForm.value.username || !addForm.value.realName) {
    ElMessage.error('账号和姓名为必填项')
    return
  }
  try {
    await request.post('/admin/users', addForm.value)
    ElMessage.success('用户添加成功')
    addDialogVisible.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error('添加失败: ' + (e?.message || ''))
  }
}

const handleEdit = (row) => {
  editForm.value = { userId: row.id, username: row.username, realName: row.realName, phone: row.phone, status: row.status }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await request.put('/admin/users', editForm.value)
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
    } catch(e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}
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
