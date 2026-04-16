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
          还没有账号？<a href="javascript:void(0)" @click="openRegisterDialog">立即注册</a>
        </div>
      </el-form>
    </div>

    <el-dialog v-model="registerVisible" title="学生注册" width="560px">
      <el-form :model="registerForm" label-position="top">
        <el-form-item label="学号">
          <el-input v-model="registerForm.username" placeholder="请输入学校下发的学号" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="请设置登录密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入登录密码" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="registerForm.grade" style="width: 100%" placeholder="请选择年级">
            <el-option v-for="grade in registerGrades" :key="grade" :label="grade" :value="grade" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select v-model="registerForm.department" style="width: 100%" placeholder="请选择学院">
            <el-option v-for="department in registerDepartments" :key="department" :label="department" :value="department" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="registerForm.major" style="width: 100%" placeholder="请选择专业">
            <el-option v-for="major in registerMajors" :key="major" :label="major" :value="major" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="registerForm.classId" style="width: 100%" placeholder="请选择班级">
            <el-option v-for="cls in registerClassOptions" :key="cls.classId" :label="cls.className" :value="cls.classId" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="submitRegister">确认注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')
const registerVisible = ref(false)
const registerLoading = ref(false)
const registerClasses = ref([])
const registerGrades = ref([])

const form = reactive({
  username: '',
  password: '',
  role: 'student',
  code: '',
  uuid: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  grade: '',
  department: '',
  major: '',
  classId: null,
  className: ''
})

const registerDepartments = computed(() => [...new Set(registerClasses.value.map(item => item.department).filter(Boolean))])
const registerMajors = computed(() => [...new Set(registerClasses.value.filter(item => !registerForm.department || item.department === registerForm.department).map(item => item.major).filter(Boolean))])
const registerClassOptions = computed(() => registerClasses.value.filter(item => (!registerForm.department || item.department === registerForm.department) && (!registerForm.major || item.major === registerForm.major)))

watch(() => registerForm.username, (username) => {
  const matched = String(username || '').match(/^(20\d{2})/)
  const inferredGrade = matched ? `${matched[1]}级` : ''
  if (inferredGrade && registerGrades.value.includes(inferredGrade)) {
    registerForm.grade = inferredGrade
    return
  }
  const currentYearGrade = `${new Date().getFullYear()}级`
  if (registerGrades.value.includes(currentYearGrade)) {
    registerForm.grade = currentYearGrade
  }
})

watch(() => registerForm.department, () => {
  registerForm.major = ''
  registerForm.classId = null
  registerForm.className = ''
})

watch(() => registerForm.major, () => {
  registerForm.classId = null
  registerForm.className = ''
})

watch(() => registerForm.classId, (classId) => {
  const selectedClass = registerClassOptions.value.find(item => String(item.classId) === String(classId))
  registerForm.className = selectedClass?.className || ''
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

const openRegisterDialog = async () => {
  try {
    const res = await request.get('/auth/register/options')
    registerClasses.value = res.classes || []
    registerGrades.value = res.grades || []
    Object.assign(registerForm, {
      username: '',
      password: '',
      confirmPassword: '',
      realName: '',
      grade: registerGrades.value[0] || '',
      department: '',
      major: '',
      classId: null,
      className: ''
    })
    registerVisible.value = true
  } catch (e) {
    ElMessage.error('获取注册信息失败')
  }
}

const submitRegister = async () => {
  if (!registerForm.username || !registerForm.password || !registerForm.realName || !registerForm.classId) {
    ElMessage.error('请完整填写注册信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  registerLoading.value = true
  try {
    await request.post('/auth/register', {
      username: registerForm.username,
      password: registerForm.password,
      realName: registerForm.realName,
      role: 'student',
      classId: registerForm.classId,
      className: registerForm.className,
      grade: registerForm.grade,
      department: registerForm.department,
      major: registerForm.major
    })
    registerVisible.value = false
    ElMessage.success('注册申请已提交，请等待管理员启用账号后再登录')
  } catch (e) {
    ElMessage.error(e?.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
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
