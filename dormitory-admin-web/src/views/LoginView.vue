<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456',
})

const onSubmit = async () => {
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    await router.replace('/dashboard')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.replace('/register')
}
</script>

<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="illustration-side">
        <div class="illustration-content">
          <div class="apple-mascot">🍏</div>
          <h1>Dorm Manager</h1>
          <p>Cute & Efficient Dormitory Management System</p>
          <div class="circles">
            <span class="circle c1"></span>
            <span class="circle c2"></span>
            <span class="circle c3"></span>
          </div>
        </div>
      </div>
      
      <div class="form-side">
        <div class="form-content">
          <h2>欢迎回来</h2>
          <p class="subtitle">请登录你的账号</p>
          
          <el-form :model="form" class="login-form" size="large">
            <el-form-item>
              <el-input 
                v-model="form.username" 
                placeholder="用户名" 
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item>
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码" 
                :prefix-icon="Lock"
                show-password 
                @keyup.enter="onSubmit"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" :loading="loading" @click="onSubmit">
                登录
              </el-button>
            </el-form-item>

            <div class="helper">
              <span>没有账号？</span>
              <el-button type="primary" link @click="goRegister">去注册</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0f2f1; /* Soft teal background */
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(126, 217, 87, 0.1) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(126, 217, 87, 0.1) 0%, transparent 20%);
}

.login-wrapper {
  display: flex;
  width: 900px;
  height: 500px;
  background: #ffffff;
  border-radius: 30px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.08);
  overflow: hidden;
  max-width: 90%;
}

.illustration-side {
  flex: 1;
  background: linear-gradient(135deg, #7ED957 0%, #5CB85C 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.illustration-content {
  text-align: center;
  z-index: 2;
  padding: 2rem;
}

.apple-mascot {
  font-size: 80px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
  text-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.illustration-content h1 {
  font-size: 2rem;
  margin-bottom: 10px;
  color: white;
}

.illustration-content p {
  color: rgba(255,255,255,0.9);
  font-size: 1.1rem;
}

.circles .circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
}

.c1 { width: 200px; height: 200px; top: -50px; left: -50px; }
.c2 { width: 300px; height: 300px; bottom: -100px; right: -50px; }
.c3 { width: 100px; height: 100px; top: 40%; left: 80%; }

.form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-content {
  width: 100%;
  max-width: 320px;
  text-align: center;
}

.form-content h2 {
  font-size: 1.8rem;
  color: var(--color-text);
  margin-bottom: 8px;
}

.subtitle {
  color: var(--color-text-light);
  margin-bottom: 30px;
  font-size: 0.95rem;
}

.login-btn {
  width: 100%;
  padding: 22px;
  font-size: 1.1rem;
  border-radius: 50px !important; /* Extra round for login button */
}

.helper {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 10px;
  color: var(--color-text-light);
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    height: auto;
    width: 90%;
  }
  
  .illustration-side {
    padding: 2rem 1rem;
  }
  
  .form-side {
    padding: 2rem;
  }
}
</style>

