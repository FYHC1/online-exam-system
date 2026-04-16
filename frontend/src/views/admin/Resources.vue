<template>
  <div class="admin-resources">
    <div class="glass-card mb-4" style="padding: 20px;">
      <h2>全平台核心资源库</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">跨越班级和院系限制，统一审计与维护分布在各个教师名下的公共题库及试卷底库信息。</p>
    </div>

    <div class="glass-card p-0">
      <el-tabs v-model="activeTab" class="custom-tabs" @tab-click="handleTabClick">
        <!-- 题库大盘 -->
        <el-tab-pane label="题目题源库" name="questions">
          <div style="padding: 20px;">
            <el-form :inline="true" :model="qFilters" class="mb-4">
              <el-form-item label="所属学科：">
                <el-select v-model="qFilters.subject" placeholder="全部分科" clearable>
                  <el-option label="高等数学" value="高等数学" />
                  <el-option label="大学物理" value="大学物理" />
                  <el-option label="Java核心栈" value="Java核心栈" />
                </el-select>
              </el-form-item>
              <el-form-item label="录入教师：">
                <el-input v-model="qFilters.creator" placeholder="搜索提交者姓名" clearable />
              </el-form-item>
            </el-form>

            <el-table :data="allQuestions" style="width: 100%" border="false">
              <el-table-column prop="qcId" label="系统流水号" width="160" />
              <el-table-column prop="content" label="题目描述简要" show-overflow-tooltip />
              <el-table-column prop="type" label="题目类型" width="100">
                <template #default="scope">
                  <el-tag size="small" type="info">{{ scope.row.type }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="subject" label="归属授课门类" width="140" />
              <el-table-column prop="creator" label="原始录入人" width="120" />
              <el-table-column label="资源强管理" width="180" align="center">
                <template #default="scope">
                  <el-button link type="primary" size="small">审计预览</el-button>
                  <el-button link type="danger" size="small">违规废除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination background layout="prev, pager, next" :total="1892" />
            </div>
          </div>
        </el-tab-pane>

        <!-- 试卷大盘 -->
        <el-tab-pane label="历史考卷快照池" name="exams">
          <div style="padding: 20px;">
            <el-form :inline="true" :model="eFilters" class="mb-4">
              <el-form-item label="试卷名称：">
                <el-input v-model="eFilters.title" placeholder="匹配考卷命名空间" clearable />
              </el-form-item>
              <el-form-item label="辐射大类院系：">
                <el-select v-model="eFilters.college" placeholder="全部范畴" clearable>
                  <el-option label="全部院系" value="" />
                  <el-option label="软件工程学院" value="软件工程学院" />
                  <el-option label="外国语学院" value="外国语学院" />
                </el-select>
              </el-form-item>
              <el-form-item label="目前状态节点：">
                <el-select v-model="eFilters.status" placeholder="全周期状态" clearable>
                  <el-option label="未发布储备" value="冷数据" />
                  <el-option label="正在进行中" value="热数据" />
                  <el-option label="已结束归档" value="历史归档" />
                </el-select>
              </el-form-item>
            </el-form>

            <el-table :data="allExams" style="width: 100%" border="false">
              <el-table-column prop="roomId" label="考场编码流水" width="140" />
              <el-table-column prop="title" label="生成试卷主题" show-overflow-tooltip />
              <el-table-column prop="college" label="波及目标学院" width="160" />
              <el-table-column prop="status" label="主频状态标识" width="100">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.status === '热数据' ? 'success' : 'warning'">{{ scope.row.status === '热数据' ? '进行中' : (scope.row.status === '冷数据' ? '储备中' : '已归档') }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" align="center">
                <template #default="scope">
                  <el-button link type="primary" size="small">试卷明细库</el-button>
                  <el-button link type="warning" size="small">吊销终止权</el-button>
                  <el-button link type="danger" size="small">备份移除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination background layout="prev, pager, next" :total="431" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('questions')

const qFilters = ref({ subject: '', creator: '' })
const eFilters = ref({ title: '', college: '', status: '' })

const allQuestions = ref([
  { qcId: 'QX-990-2A', content: '求矩阵的特征多项式的常用简化计算方法是...', type: '计算题', subject: '高等数学', creator: '李教授' },
  { qcId: 'QX-102-1B', content: 'Java语法体系内，抽象类与一般接口规范的最本质区别是...', type: '单选', subject: 'Java核心栈', creator: '王老师' },
  { qcId: 'QX-333-8C', content: '简述热力学第二定律“熵增原理”的物理定义公式推想...', type: '简答题', subject: '大学物理', creator: '钱老师' },
])

const allExams = ref([
  { roomId: 'EXM-A-1088', title: '23秋季下半学期公共课高等数学全局统考', college: '全校公共类不限', status: '热数据' },
  { roomId: 'EXM-B-0822', title: '针对计科院系内部设置的算法进阶模拟测', college: '计算机学院', status: '冷数据' },
  { roomId: 'EXM-S-9003', title: '全国高校外语基准水平词汇量底线测试专卷', college: '外国语学院', status: '历史归档' },
])

const handleTabClick = (tab) => {
  // refresh
}
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.custom-tabs { padding: 0; }
:deep(.el-tabs__nav-wrap) { padding: 0 20px; border-bottom: 1px solid var(--glass-border); }
:deep(.el-tabs__item) { height: 50px; line-height: 50px; font-size: 16px; transition: color 0.3s; }
:deep(.el-tabs__item.is-active) { color: var(--primary-color) !important; font-weight: 600; }
:deep(.el-tabs__active-bar) { background-color: var(--primary-color) !important; height: 3px; border-radius: 3px; }
:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0 0 0;
  border-top: 1px dashed var(--glass-border);
  margin-top: 20px;
}
</style>
