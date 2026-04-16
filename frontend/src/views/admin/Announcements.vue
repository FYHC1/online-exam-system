<template>
  <div class="admin-announcements">
    <div class="glass-card mb-4 top-bar">
      <div>
        <h2 style="margin: 0 0 8px 0;">系统公告与资讯管理</h2>
        <p style="margin: 0; color: var(--text-secondary); font-size: 14px;">面向全平台用户撰写、下发重要的系统维护通知、考试安排或纪律通报。</p>
      </div>
      <!-- Add Button -->
      <div style="margin-left: auto;">
        <el-button type="primary" size="large" @click="handleEdit(null)" icon="el-icon-plus">
          发布新公告
        </el-button>
      </div>
    </div>

    <div class="glass-card filter-bar mb-4" style="padding: 20px 20px 0 20px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键字：">
          <el-input v-model="filters.keyword" placeholder="搜索公告标题内容" clearable />
        </el-form-item>
        <el-form-item label="分类筛选：">
          <el-select v-model="filters.type" placeholder="全部分类" clearable style="width: 150px">
            <el-option label="系统通知" value="系统" />
            <el-option label="考试安排" value="考试" />
            <el-option label="全站通报" value="纪律" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card p-0">
      <el-table :data="filteredAnnouncements" class="custom-table" border="false">
        <el-table-column prop="id" label="公告编号" width="130" />
        <el-table-column prop="title" label="公告标题" />
        <el-table-column prop="type" label="标签类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布署名" width="150" />
        <el-table-column prop="date" label="发布时间" width="180" />
        <el-table-column prop="isTop" label="当前置顶" width="120" align="center">
          <template #default="scope">
            <el-switch :model-value="scope.row.isTop" active-color="#ef4444" inactive-color="#d1d5db" @change="toggleTop(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="管理操作" width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination background layout="prev, pager, next, total" :total="filteredAnnouncements.length" />
      </div>
    </div>

    <!-- 弹窗：编辑公告 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '修改系统公告内容' : '撰写全新系统公告'" width="60%" custom-class="glass-dialog">
      <el-form :model="form" label-width="120px" label-position="left">
        <el-form-item label="公告大标题：">
          <el-input v-model="form.title" placeholder="请输入直观明确的标题..." />
        </el-form-item>
        <el-form-item label="核心分类属性：">
          <el-radio-group v-model="form.type">
            <el-radio-button label="系统">系统维护类</el-radio-button>
            <el-radio-button label="考试">重大考务类</el-radio-button>
            <el-radio-button label="纪律">违规通报类</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="强效置顶选项：">
          <el-switch v-model="form.isTop" active-color="#ef4444" />
          <span style="font-size:12px; color:var(--text-secondary); margin-left: 10px;">(将长期展示在首页最顶端重要醒目位置)</span>
        </el-form-item>
        <el-form-item label="图文详情内容：">
          <el-input type="textarea" :rows="8" v-model="form.content" placeholder="输入公告的正文详细内容，支持链接与换行..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消关闭</el-button>
          <el-button type="primary" @click="submitSave">立即发布保存至各终端</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const filters = ref({ keyword: '', type: '' })
const dialogVisible = ref(false)

const form = ref({ id: null, title: '', type: '系统', content: '', isTop: false })

const announcementsList = ref([])

const fetchAnnouncements = async () => {
  try {
    const res = await request.get('/admin/announcements')
    announcementsList.value = res.map(a => ({
      id: a.id,                 // Entity uses 'id' not 'announcementId'
      title: a.title,
      type: a.title.includes('维护') || a.title.includes('升级') ? '系统' :
            a.title.includes('考试') || a.title.includes('成绩') ? '考试' : '系统',
      content: a.content,
      publisher: '系统管理员',
      date: a.createTime ? new Date(a.createTime).toLocaleString() : '',
      isTop: a.isTop === 1
    }))
  } catch(e) {
    ElMessage.error('无法加载公告列表')
  }
}

onMounted(() => {
  fetchAnnouncements()
})

const filteredAnnouncements = computed(() => {
  return announcementsList.value.filter(a => {
    return (!filters.value.keyword || a.title.includes(filters.value.keyword)) &&
           (!filters.value.type || a.type === filters.value.type)
  })
})

const getTypeTag = (type) => {
  if (type === '系统') return 'warning'
  if (type === '考试') return 'success'
  if (type === '纪律') return 'danger'
  return 'info'
}

const toggleTop = async (row) => {
  try {
    const payload = { 
      announcementId: row.id,
      title: row.title,
      type: row.type,
      content: row.content,
      isTop: !row.isTop ? 1 : 0 
    }
    await request.put('/admin/announcements', payload)
    row.isTop = !row.isTop
    ElMessage.success(`公告 [${row.title}] 置顶状态已变更为：${row.isTop}`)
  } catch(e) { ElMessage.error('状态更新失败') }
}

const handleEdit = (row) => {
  if (row) {
    form.value = { ...row, isTop: row.isTop }
  } else {
    form.value = { id: null, title: '', type: '系统', content: '', isTop: false }
  }
  dialogVisible.value = true
}

const submitSave = async () => {
  try {
    const payload = {
      id: form.value.id,        // Must match entity field name 'id'
      title: form.value.title,
      content: form.value.content,
      isTop: form.value.isTop ? 1 : 0,
      createBy: 1 // hardcoded admin ID
    }
    
    if (form.value.id) {
      await request.put('/admin/announcements', payload)
      ElMessage.success('公告更新成功')
    } else {
      await request.post('/admin/announcements', payload)
      ElMessage.success('系统公告编写完毕且已成功下发存储池！')
    }
    dialogVisible.value = false
    fetchAnnouncements()
  } catch(e) { ElMessage.error('保存失败') }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要从全站记录中永久删除此篇公告内容吗？该操作无法恢复。', '删除警告', {
    type: 'error',
    confirmButtonText: '确认删除'
  }).then(async () => {
    try {
      await request.delete(`/admin/announcements/${id}`)
      ElMessage.success('系统通告数据已成功删除。')
      fetchAnnouncements()
    } catch(e) { ElMessage.error('删除失败') }
  }).catch(() => {})
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.top-bar { padding: 24px; display: flex; align-items: center; }

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

/* 弹窗覆写 */
:deep(.el-overlay) { background-color: rgba(0,0,0,0.6); backdrop-filter: blur(5px); }
:deep(.glass-dialog) { background: var(--glass-bg); backdrop-filter: blur(12px); border-radius: 12px; }
</style>
