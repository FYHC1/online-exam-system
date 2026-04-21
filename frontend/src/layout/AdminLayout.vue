<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="aside-menu glass-container">
      <div class="logo">
        <span>超级控制台</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="custom-menu"
        background-color="transparent"
      >
        <el-menu-item index="/admin/dashboard"><i class="el-icon-odometer" style="margin-right: 8px;"></i>平台总览</el-menu-item>
        <el-menu-item index="/admin/users"><i class="el-icon-user" style="margin-right: 8px;"></i>用户管理</el-menu-item>
        <el-menu-item index="/admin/announcements"><i class="el-icon-bell" style="margin-right: 8px;"></i>公告资讯</el-menu-item>
        <el-menu-item index="/admin/resources"><i class="el-icon-folder-opened" style="margin-right: 8px;"></i>题库与考试</el-menu-item>
        <el-menu-item index="/admin/analytics"><i class="el-icon-data-analysis" style="margin-right: 8px;"></i>数据监控</el-menu-item>
        <el-menu-item index="/admin/settings"><i class="el-icon-setting" style="margin-right: 8px;"></i>系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="glass-container header">
        <div class="breadcrumb">当前位置：<span>{{ route.meta.title || '系统' }}</span></div>
        <div class="user-profile">
          <el-dropdown trigger="click">
            <span class="el-dropdown-link name-trigger" style="display:flex; align-items:center; cursor:pointer; outline:none;">
              <el-avatar :size="32" style="background:var(--danger-color);color:white;margin-right:8px;">A</el-avatar>
              <span class="name">超级管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <div style="text-align:center; padding: 5px 0;">
                    <div style="font-weight:bold; color:var(--text-primary);">Admin</div>
                    <el-tag size="small" type="danger" style="margin-top:5px;">系统超管</el-tag>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided>系统资料</el-dropdown-item>
                <el-dropdown-item @click="logout" divided style="color: var(--danger-color)">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
/* 同 StudentLayout 的样式复用  */
.layout-container { height: 100vh; background-color: var(--bg-color); padding: 16px; gap: 16px; }
.aside-menu { border-radius: 16px !important; display: flex; flex-direction: column; overflow: hidden; border-right: none; }
.logo { height: 70px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: 700; color: var(--danger-color); border-bottom: 1px solid var(--glass-border); }
.custom-menu { border-right: none; flex: 1; padding: 12px 0; }
:deep(.el-menu-item) { margin: 8px 16px; border-radius: 8px; transition: all 0.3s; }
:deep(.el-menu-item.is-active) { background: var(--primary-light) !important; color: var(--primary-color) !important; font-weight: 600; }
.header { height: 64px !important; border-radius: 12px !important; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; margin-bottom: 16px; }
.breadcrumb { font-size: 14px; color: var(--text-secondary); }
.breadcrumb span { color: var(--text-primary); font-weight: 500; }
.user-profile { display: flex; align-items: center; gap: 16px; }
.name { font-weight: 600; color: var(--primary-color); }
.main-content { padding: 0; overflow-y: auto; border-radius: 16px; }
</style>
