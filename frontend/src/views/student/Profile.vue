<template>
  <div class="profile-container">
    <div class="glass-card mb-4" style="padding: 20px;">
      <h2>个人资料</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">在这里查看和设置您的基础学习信息与系统头像。</p>
    </div>

    <div class="glass-card p-4 main-content">
      <el-row :gutter="40" style="width: 100%; align-items: stretch;">
        <el-col :span="8" class="text-center">
          <div class="avatar-section">
            <el-avatar :size="120" class="profile-avatar" :src="userInfo.avatar">
              {{ userInfo.realName?.charAt(0) || 'U' }}
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
          <el-form label-width="100px" :model="userInfo" class="profile-form" label-position="left">
            <el-form-item label="姓名">
              <el-input v-model="userInfo.realName" disabled />
            </el-form-item>
            <el-form-item label="学号/账号">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="入学年份">
              <el-select v-model="userInfo.grade" class="w-full">
                <el-option label="2021级 (大四)" value="2021级" />
                <el-option label="2022级 (大三)" value="2022级" />
                <el-option label="2023级 (大二)" value="2023级" />
                <el-option label="2024级 (大一)" value="2024级" />
              </el-select>
            </el-form-item>
            <el-form-item label="所属专业">
              <el-input v-model="userInfo.major" placeholder="例如：软件工程" />
            </el-form-item>
            <el-form-item label="班级编号">
              <el-input v-model="userInfo.class" placeholder="例如：2班" />
            </el-form-item>
            <el-form-item class="action-buttons">
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="8">
        <div class="glass-card stat-panel primary-stat">
          <div class="stat-icon"><i class="el-icon-data-line"></i></div>
          <div class="stat-info">
            <div class="val">18</div>
            <div class="label">已参与考试总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-panel success-stat">
          <div class="stat-icon"><i class="el-icon-trophy"></i></div>
          <div class="stat-info">
            <div class="val">86.5分</div>
            <div class="label">历史平均得分</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card stat-panel warning-stat">
          <div class="stat-icon"><i class="el-icon-warning-outline"></i></div>
          <div class="stat-info">
            <div class="val">32题</div>
            <div class="label">错题本收录量</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const userInfo = ref({
  username: 'student',
  realName: '张三',
  grade: '2021级',
  major: '软件工程',
  class: '2班',
  avatar: ''
})

const parseDataFromLocal = () => {
  const localUser = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if(localUser.username) {
    userInfo.value.username = localUser.username || 'student'
    userInfo.value.realName = localUser.realName || '张三'
    userInfo.value.grade = localUser.grade || '2021级'
    userInfo.value.avatar = localUser.avatar || ''
    
    if (localUser.majorClass) {
      if (localUser.majorClass.includes('|')) {
         const parts = localUser.majorClass.split('|')
         userInfo.value.major = parts[0].trim()
         userInfo.value.class = parts[1].trim()
      } else {
         const match = localUser.majorClass.match(/(\d+班)/)
         if (match) {
            userInfo.value.major = localUser.majorClass.replace(match[0], '').trim()
            userInfo.value.class = match[0]
         } else {
            userInfo.value.major = localUser.majorClass
            userInfo.value.class = ''
         }
      }
    }
  }
}

onMounted(() => {
  parseDataFromLocal()
})

const resetProfile = () => {
  parseDataFromLocal()
  ElMessage.info('已恢复为当前保存的数据')
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
  localUser.grade = userInfo.value.grade
  localUser.majorClass = userInfo.value.class ? `${userInfo.value.major} | ${userInfo.value.class}` : userInfo.value.major
  localUser.realName = userInfo.value.realName
  localUser.avatar = userInfo.value.avatar
  
  localStorage.setItem('userInfo', JSON.stringify(localUser))
  
  ElMessage.success('个人资料已成功保存！请刷新页面以同步导航栏头像。')
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

.profile-form { max-width: 450px; padding-left: 40px; padding-top: 10px; }
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

.mt-4 { margin-top: 20px; }

.stat-panel {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
}
.stat-icon {
  width: 60px; height: 60px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 32px;
}
.stat-info .val { font-size: 28px; font-weight: bold; color: var(--text-primary); margin-bottom: 4px; }
.stat-info .label { font-size: 14px; color: var(--text-secondary); }

.primary-stat .stat-icon { background: rgba(56, 189, 248, 0.1); color: #38bdf8; }
.success-stat .stat-icon { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.warning-stat .stat-icon { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
</style>
