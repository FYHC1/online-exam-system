<template>
  <div class="not-found">
    <div class="glass-container error-card">
      <h1>404</h1>
      <p>哎呀，您访问的页面不存在</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--bg-color);
}
.error-card {
  padding: 60px;
  text-align: center;
  border-radius: 20px;
}
.error-card h1 {
  font-size: 80px;
  color: var(--primary-color);
  margin-bottom: 20px;
}
.error-card p {
  color: var(--text-secondary);
  margin-bottom: 30px;
  font-size: 18px;
}
</style>
