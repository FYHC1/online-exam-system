<template>
  <div class="profile-container">
    <div class="glass-card mb-4" style="padding: 20px;">
      <h2>教师个人资料</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">在此管理您的执教信息与系统头像。</p>
    </div>

    <div class="glass-card p-4 main-content">
      <el-row :gutter="40" style="width: 100%; align-items: stretch;">
        <el-col :span="8" class="text-center">
          <div class="avatar-section">
            <el-avatar :size="120" class="profile-avatar" :src="userInfo.avatar">
              {{ userInfo.realName?.charAt(0) || 'T' }}
            </el-avatar>
            <el-upload
              class="upload-btn"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              @change="handleAvatarChange"
              accept="image/jpeg,image/png,image/gif"
            >
              <el-button type="primary" plain round>更换头像</el-button>
            </el-upload>
            <div class="tip">支持 JPG、PNG 格式<br/>图片大小不超过 2MB</div>
          </div>
        </el-col>

        <el-col :span="16">
          <el-form label-width="130px" :model="userInfo" class="profile-form" label-position="left">
            <el-form-item label="姓名">
              <el-input v-model="userInfo.realName" disabled />
            </el-form-item>
            <el-form-item label="所属学校/单位">
              <el-input v-model="userInfo.school" placeholder="例如：某某大学" />
            </el-form-item>
            <el-form-item label="所属学院/部门">
              <el-input v-model="userInfo.department" placeholder="例如：计算机与信息学院" />
            </el-form-item>
            <el-form-item label="负责科目">
              <el-select v-model="userInfo.subjects" multiple placeholder="请选择您执教的科目" class="w-full">
                <el-option label="高等数学" value="高等数学" />
                <el-option label="大学英语" value="大学英语" />
                <el-option label="JavaWeb" value="JavaWeb" />
                <el-option label="数据结构" value="数据结构" />
              </el-select>
            </el-form-item>
            <el-form-item label="负责班级">
              <el-select v-model="userInfo.classes" multiple placeholder="请选择负责的班级" class="w-full">
                <el-option label="软件工程1班" value="软件工程1班" />
                <el-option label="软件工程2班" value="软件工程2班" />
                <el-option label="计算机科学1班" value="计算机科学1班" />
                <el-option label="大数据1班" value="大数据1班" />
              </el-select>
            </el-form-item>
            <el-form-item class="action-buttons">
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const userInfo = ref({
  realName: '李四',
  school: '测试大学',
  department: '计算机与信息学院',
  subjects: ['JavaWeb', '数据结构'],
  classes: ['软件工程1班', '软件工程2班'],
  avatar: ''
})

const parseDataFromLocal = () => {
  const localUser = JSON.parse(localStorage.getItem('userInfo') || '{}')
  // We apply the localUser preferences if role matches logic
  userInfo.value.realName = localUser.realName || '李四'
  userInfo.value.school = localUser.school || '测试大学'
  userInfo.value.department = localUser.department || '计算机与信息学院'
  userInfo.value.subjects = localUser.subjects || ['JavaWeb', '数据结构']
  userInfo.value.classes = localUser.classes || ['软件工程1班', '软件工程2班']
  userInfo.value.avatar = localUser.avatar || ''
}

onMounted(() => {
  parseDataFromLocal()
})

const resetProfile = () => {
  parseDataFromLocal()
  ElMessage.info('已恢复为初始数据')
}

const handleAvatarChange = (file) => {
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片过大，请保持在 2MB 以内！')
    return
  }
  userInfo.value.avatar = URL.createObjectURL(file.raw)
  ElMessage.success('头像预览成功，点击"保存修改"后永久生效！')
}

const saveProfile = () => {
  const localUser = JSON.parse(localStorage.getItem('userInfo') || '{}')
  localUser.realName = userInfo.value.realName
  localUser.school = userInfo.value.school
  localUser.department = userInfo.value.department
  localUser.subjects = userInfo.value.subjects
  localUser.classes = userInfo.value.classes
  localUser.avatar = userInfo.value.avatar
  
  localStorage.setItem('userInfo', JSON.stringify(localUser))
  
  ElMessage.success('个人资料已成功保存！请刷新页面以同步右上角导航栏信息。')
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 40px; }
.w-full { width: 100%; }
.main-content { min-height: 400px; display: flex; align-items: stretch; }

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-right: 40px;
  border-right: 1px dashed var(--glass-border);
  height: 100%;
}

.profile-avatar {
  background: var(--primary-color);
  font-size: 40px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
  border: 4px solid rgba(255,255,255,0.5);
  transition: all 0.3s ease;
}

.profile-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 24px rgba(0,0,0,0.15);
}

.upload-btn { margin-bottom: 12px; }
.tip { font-size: 13px; color: var(--text-secondary); text-align: center; line-height: 1.6; }

.profile-form { max-width: 500px; padding-left: 40px; padding-top: 10px; }
:deep(.el-input__wrapper), :deep(.el-select__wrapper) {
  background-color: rgba(255, 255, 255, 0.4) !important;
  box-shadow: none !important;
  border: 1px solid var(--glass-border);
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.05) !important;
  color: var(--text-secondary);
}

.action-buttons { margin-top: 30px; }
</style>
