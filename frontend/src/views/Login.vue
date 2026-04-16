<template>
  <div class="login-container animated-bg">
    <div class="glass-container login-box">
      <div class="header">
        <h2 class="title">在线考试系统</h2>
        <p class="subtitle">登录进入您的数字学习平台</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="role">
          <el-radio-group v-model="form.role" class="role-selector">
            <el-radio-button label="student">学生端</el-radio-button>
            <el-radio-button label="teacher">教师端</el-radio-button>
            <el-radio-button label="admin">超管端</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入账号 / 学号 / 工号" 
            size="large"
            :prefix-icon="'User'"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            size="large"
            :prefix-icon="'Lock'"
            show-password
          />
        </el-form-item>

        <el-form-item prop="code">
          <div class="captcha-box">
            <el-input 
              v-model="form.code" 
              placeholder="请输入验证码" 
              size="large"
              :prefix-icon="'Key'"
              style="flex: 1; margin-right: 12px;"
              @keyup.enter="handleLogin"
            />
            <img 
              v-if="captchaImg" 
              :src="captchaImg" 
              @click="getCaptcha" 
              class="captcha-img" 
              title="点击刷新"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleLogin" 
            class="submit-btn" 
            size="large" 
            :loading="loading">
            登录系统
          </el-button>
        </el-form-item>

        <div class="form-footer" v-if="form.role === 'student'">
          还没有账号？<a href="javascript:void(0)" @click="showMessage('目前暂未开放自助注册')">立即注册</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')

const form = reactive({
  username: '',
  password: '',
  role: 'student',
  code: '',
  uuid: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const getCaptcha = async () => {
  try {
    const res = await request.get('/auth/captcha')
    form.uuid = res.uuid
    captchaImg.value = res.img
  } catch (error) {
    console.error('Failed to get captcha', error)
    form.uuid = 'dummy'
    captchaImg.value = ''
  }
}

onMounted(() => {
  getCaptcha()
})

const showMessage = (msg) => {
  ElMessage.info(msg)
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/auth/login', form)
        
        // 存储 Token 和 UserInfo
        localStorage.setItem('token', res.token)
        localStorage.setItem('userInfo', JSON.stringify(res.userInfo))
        
        ElMessage.success('登录成功，欢迎回来！')
        router.push(`/${res.userInfo.role}/dashboard`)
      } catch (error) {
        // 请求失败自动刷新验证码
        getCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.login-box {
  width: 440px;
  padding: 40px;
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.login-form {
  margin-top: 20px;
}

.role-selector {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.role-selector .el-radio-button__inner) {
  padding: 10px 24px;
  border-radius: 6px;
  background: rgba(255,255,255,0.4);
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 20px;
}

.form-footer a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s;
}

.form-footer a:hover {
  color: var(--primary-hover);
}

@keyframes slideUp {
  0% { transform: translateY(40px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

.captcha-box {
  display: flex;
  width: 100%;
  align-items: center;
}

.captcha-img {
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid var(--border-color);
  background: #fff;
}
</style>
