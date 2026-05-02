<template>
  <div class="profile-container">
    <div class="glass-card mb-4" style="padding: 20px;">
      <h2>个人资料</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">查看学生身份信息，并修改头像、联系方式和登录密码。</p>
    </div>

    <div class="glass-card p-4 main-content">
      <el-row :gutter="40" style="width: 100%; align-items: stretch;">
        <el-col :span="8" class="text-center">
          <div class="avatar-section">
            <el-avatar :size="120" class="profile-avatar" :src="form.avatar">{{ form.realName?.charAt(0) || 'U' }}</el-avatar>
            <el-upload class="upload-btn" action="#" :auto-upload="false" :show-file-list="false" @change="handleAvatarChange" accept="image/jpeg,image/png,image/gif">
              <el-button type="primary" plain round>更换头像</el-button>
            </el-upload>
            <div class="tip">支持 JPG、PNG 格式，图片大小不超过 2MB</div>
          </div>
        </el-col>

        <el-col :span="16">
          <el-form label-width="110px" :model="form" class="profile-form" label-position="left">
            <el-form-item label="姓名">
              <el-input :model-value="form.realName" disabled />
            </el-form-item>
            <el-form-item label="学号/账号">
              <el-input :model-value="form.username" disabled />
            </el-form-item>
            <el-form-item label="所属学院">
              <el-input :model-value="form.department" disabled />
            </el-form-item>
            <el-form-item label="所属专业">
              <el-input :model-value="form.major" disabled />
            </el-form-item>
            <el-form-item label="所在班级">
              <el-input :model-value="form.className" disabled />
            </el-form-item>
            <el-form-item label="入学年份">
              <el-input :model-value="form.enrollmentYear" disabled />
            </el-form-item>
            <el-form-item label="联系方式">
              <el-input v-model="form.phone" placeholder="请输入手机号码或联系方式" />
            </el-form-item>
            <el-form-item class="action-buttons">
              <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </div>

    <div class="glass-card p-4 mt-4">
      <h3 style="margin-top: 0;">修改密码</h3>
      <el-form label-width="110px" :model="passwordForm" class="password-form">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="changingPassword" @click="changePassword">确认修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const form = reactive({ username: '', realName: '', department: '', major: '', className: '', enrollmentYear: '', phone: '', avatar: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const saving = ref(false)
const changingPassword = ref(false)

const syncLocalUser = () => {
  const localUser = JSON.parse(localStorage.getItem('userInfo') || '{}')
  localUser.realName = form.realName
  localUser.avatar = form.avatar
  localUser.department = form.department
  localUser.majorClass = [form.major, form.className].filter(Boolean).join(' | ')
  localUser.grade = form.grade
  localStorage.setItem('userInfo', JSON.stringify(localUser))
}

const loadProfile = async () => {
  const data = await request.get('/profile/me')
  Object.assign(form, data)
}

onMounted(async () => {
  try {
    await loadProfile()
  } catch {
    ElMessage.error('加载个人资料失败')
  }
})

const handleAvatarChange = (file) => {
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片过大，请保持在 2MB 以内')
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    form.avatar = reader.result
    ElMessage.success('头像已更新预览，保存后生效')
  }
  reader.readAsDataURL(file.raw)
}

const saveProfile = async () => {
  const phone = String(form.phone || '').trim()
  if (phone && !/^\d{11}$/.test(phone)) {
    ElMessage.error('联系方式需为11位纯数字手机号')
    return
  }
  form.phone = phone
  saving.value = true
  try {
    const data = await request.put('/profile/me', { phone: form.phone, avatar: form.avatar })
    Object.assign(form, data)
    syncLocalUser()
    ElMessage.success('个人资料已保存')
  } catch {
    ElMessage.error('保存资料失败')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.error('请完整填写密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  changingPassword.value = true
  try {
    await request.put('/profile/password', { oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码修改成功，请牢记新密码')
  } catch (e) {
    ElMessage.error(e?.message || '密码修改失败')
  } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.mt-4 { margin-top: 20px; }
.p-4 { padding: 40px; }
.main-content { min-height: 360px; display: flex; align-items: stretch; }
.avatar-section { display:flex; flex-direction:column; align-items:center; justify-content:center; padding-right:40px; border-right:1px dashed var(--glass-border); height:100%; }
.profile-avatar { background:var(--primary-color); font-size:40px; color:#fff; margin-bottom:24px; box-shadow:0 8px 16px rgba(0,0,0,.1); border:4px solid rgba(255,255,255,.5); }
.upload-btn { margin-bottom: 12px; }
.tip { font-size:13px; color:var(--text-secondary); text-align:center; line-height:1.6; }
.profile-form, .password-form { max-width: 520px; }
:deep(.el-input__wrapper) { background-color: rgba(255,255,255,.4) !important; box-shadow:none !important; border:1px solid var(--glass-border); }
:deep(.el-input.is-disabled .el-input__wrapper) { background-color: rgba(0,0,0,.05) !important; }
.action-buttons { margin-top: 18px; }
</style>
