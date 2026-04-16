<template>
  <div class="admin-analytics">
    <div class="glass-card mb-4" style="padding: 20px; display:flex; justify-content:space-between; align-items:center;">
      <div>
        <h2>全局数据监控与视图分析</h2>
        <p style="color: var(--text-secondary); margin-top: 8px;">汇总查看全校级综合考试报告、学科之间通过率对比图表及各院系统计排行概览。</p>
      </div>
      <div>
        <el-button type="primary" plain icon="el-icon-download">导出完整数据分析报告 (PDF)</el-button>
      </div>
    </div>

    <!-- 图表矩阵 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="glass-card p-4 mb-4">
          <div class="chart-header">
            <h3>校级期末主干课程通过率统计图</h3>
            <el-select size="small" style="width: 120px;" placeholder="23-24 秋季">
              <el-option label="23-24 秋季" value="23-24 秋" />
              <el-option label="22-23 春季" value="22-23 春" />
            </el-select>
          </div>
          <div class="mock-large-chart">
            <div class="chart-axis-y">
              <span>100%</span>
              <span>80%</span>
              <span>60%</span>
              <span>40%</span>
              <span>20%</span>
              <span>0%</span>
            </div>
            <div class="chart-grid">
              <div class="bar-col">
                <div class="bar-inner" style="height: 85%; background: var(--success-color);"></div>
                <div class="bar-label">高等数学</div>
              </div>
              <div class="bar-col">
                <div class="bar-inner" style="height: 92%; background: var(--primary-color);"></div>
                <div class="bar-label">大学英语</div>
              </div>
              <div class="bar-col">
                <div class="bar-inner" style="height: 65%; background: var(--warning-color);"></div>
                <div class="bar-label">大学物理</div>
              </div>
              <div class="bar-col">
                <div class="bar-inner" style="height: 78%; background: var(--primary-color);"></div>
                <div class="bar-label">计算机基础</div>
              </div>
              <div class="bar-col">
                <div class="bar-inner" style="height: 98%; background: var(--success-color);"></div>
                <div class="bar-label">思政理论课</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="8">
        <div class="glass-card p-4 mb-4" style="height: calc(100% - 20px);">
          <h3 style="margin-top:0;">各学院本月考试活跃度排行</h3>
          <ul class="rank-list">
            <li v-for="(item, index) in rankings" :key="index">
              <span class="rank-num" :class="{'top3': index < 3}">{{ index + 1 }}</span>
              <span class="rank-name">{{ item.name }}</span>
              <span class="rank-score">{{ item.score }} 分</span>
            </li>
          </ul>
        </div>
      </el-col>
    </el-row>

    <!-- 离散点数据与异变侦测 (Anomaly Detection) -->
    <div class="glass-card p-4">
      <h3 style="margin-top:0; border-bottom:1px dashed var(--glass-border); padding-bottom:12px; color:var(--danger-color);">考试异常作弊与异动指标监控清单</h3>
      <p style="font-size:13px; color:var(--text-secondary); margin-bottom: 16px;">利用系统防作弊模型计算捕获的考试极高雷同率或答题用时畸少的不正常记录。</p>
      
      <el-table :data="anomalies" class="custom-table" border="false">
        <el-table-column prop="rid" label="所属考试流水" width="160" />
        <el-table-column prop="student" label="相关学生身份" width="120" />
        <el-table-column prop="reason" label="被判定为异常的主因" show-overflow-tooltip />
        <el-table-column prop="severity" label="作弊嫌疑等级" width="140">
          <template #default="scope">
            <el-progress :percentage="scope.row.severity" status="exception" />
          </template>
        </el-table-column>
        <el-table-column label="后续处理动作" width="150" align="center">
          <template #default="scope">
            <el-button link type="danger" size="small">提交通报并作废</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const rankings = ref([
  { name: '计算机与信息工程学院', score: 9845 },
  { name: '外国语先锋语言学院', score: 8521 },
  { name: '理学院 (应用数理系)', score: 7920 },
  { name: '商管经贸与物流学院', score: 6200 },
  { name: '土木与建筑工程学院', score: 4801 },
  { name: '传媒人文与艺术修养系', score: 3822 },
])

const anomalies = ref([
  { rid: 'EXM-2100A', student: '赵四 (23级)', reason: '整张试卷交卷耗时过短，仅历时2分15秒即取得全对高分', severity: 99 },
  { rid: 'EXM-2155F', student: '批量机房群', reason: '大量终端IP特征高度集中重叠，有组织代考舞弊违纪嫌疑', severity: 88 },
  { rid: 'EXM-3211C', student: '李华 (22级)', reason: '客观题部分提交频率和时间间隔严格规律，疑似使用脚本', severity: 75 },
])
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.p-4 { padding: 24px; }

.chart-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px dashed var(--glass-border); padding-bottom: 12px; margin-bottom: 20px; }
.chart-header h3 { margin: 0; }

.mock-large-chart { display: flex; height: 250px; }
.chart-axis-y { width: 40px; display: flex; flex-direction: column; justify-content: space-between; font-size: 12px; color: var(--text-secondary); text-align: right; padding-right: 10px; border-right: 1px solid var(--glass-border); }
.chart-grid { flex: 1; display: flex; justify-content: space-around; align-items: flex-end; padding-top: 10px; }
.bar-col { flex: 1; display: flex; flex-direction: column; justify-content: flex-end; align-items: center; height: 100%; }
.bar-inner { width: 40px; border-radius: 6px 6px 0 0; transition: height 0.8s ease-out; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
.bar-label { margin-top: 10px; font-size: 13px; color: var(--text-secondary); font-weight: 500; }

.rank-list { padding: 0; margin: 0; list-style: none; }
.rank-list li { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px dashed rgba(255,255,255,0.1); }
.rank-list li:last-child { border-bottom: none; }
.rank-num { width: 28px; height: 28px; border-radius: 50%; background: rgba(0,0,0,0.1); display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 15px; color: var(--text-secondary); }
.rank-num.top3 { background: var(--primary-color); color: white; box-shadow: 0 0 10px rgba(var(--primary-color-rgb), 0.5); }
.rank-name { flex: 1; color: var(--text-primary); font-weight: 500; }
.rank-score { font-weight: 600; color: var(--primary-color); }

:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; }
</style>
