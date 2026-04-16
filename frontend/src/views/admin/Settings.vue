<template>
  <div class="admin-settings">
    <div class="glass-card mb-4" style="padding: 20px;">
      <h2>系统规则与基础环境配置</h2>
      <p style="color: var(--text-secondary); margin-top: 8px;">负责在此模块完成如各院系专业班级组织架构维护、当前考务学期切换与系统操作审计日志等根基设置。</p>
    </div>

    <div class="glass-card p-0">
      <el-tabs v-model="activeTab" class="custom-tabs custom-padding">
        
        <!-- Tab 1: 组织架构 (Org Structure) -->
        <el-tab-pane label="院系专业层级管理" name="org">
          <div class="p-4">
            <el-row :gutter="30">
              <el-col :span="10">
                <div class="tree-header mb-4">
                  <h4 style="margin:0;">系统组织架构树</h4>
                  <el-button type="primary" size="small" plain>添加下级部门</el-button>
                </div>
                <!-- 模拟直观组织树 -->
                <div class="mock-tree-node">
                  <div class="node-label root">系统总校区结构 (Root)</div>
                  <div class="node-children">
                    <div class="mock-tree-node">
                      <div class="node-label">计算机与信息中心 <el-tag size="small" type="success">运营中</el-tag></div>
                      <div class="node-children">
                        <div class="node-leaf">- 计算机科学级 1班 (已关联 45名学生)</div>
                        <div class="node-leaf">- 软件工程先锋营 (已关联 92名学生)</div>
                      </div>
                    </div>
                    <div class="mock-tree-node">
                       <div class="node-label">理学应用基础院 <el-tag size="small" type="success">运营中</el-tag></div>
                    </div>
                    <div class="mock-tree-node">
                       <div class="node-label" style="opacity:0.5">历史裁撤归档院系名录 <el-tag size="small" type="info">已停办封存</el-tag></div>
                    </div>
                  </div>
                </div>
              </el-col>
              <el-col :span="14">
                <div class="glass-container" style="padding: 20px; border-radius: 12px; background: rgba(var(--primary-color-rgb), 0.03);">
                  <h4 style="margin-top:0;">选中部门详细信息修改</h4>
                  <el-form label-position="top">
                    <el-form-item label="当前部门名称 (如：软件工程先锋营)">
                      <el-input value="软件工程先锋营" />
                    </el-form-item>
                    <el-form-item label="所属上级单位">
                      <el-select value="计算机" style="width: 100%">
                        <el-option label="计算机与信息中心" value="计算机" />
                      </el-select>
                    </el-form-item>
                    <el-button type="primary" style="width: 100%">确认保存该架构的修改</el-button>
                  </el-form>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 学期设置 (Term Settings) -->
        <el-tab-pane label="当前激活学期设置" name="term">
          <div class="p-4">
            <el-alert title="注意：切换当前的系统学期将使得考试发布时的默认时段随之变化，且历史成绩将需要使用专门筛选可见！" type="warning" show-icon class="mb-4" />
            <el-form label-width="150px" label-position="left">
              <el-form-item label="全站默认主学期：">
                <el-select value="23-24 秋" style="width: 300px">
                  <el-option label="2023-2024 秋季常规学期" value="23-24 秋" />
                  <el-option label="2022-2023 春季结束学期" value="22-23 春" />
                </el-select>
              </el-form-item>
              <el-form-item label="该学期时间起止点：">
                <el-date-picker type="daterange" range-separator="至" start-placeholder="开学日期" end-placeholder="结业日期" style="width: 300px" />
              </el-form-item>
              <el-form-item>
                <el-button type="danger">切保存并应用为全系统默认学期</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 系统门面 (UI Settings) -->
        <el-tab-pane label="登录页轮播图管理" name="ui">
          <div class="p-4">
            <h4>验证及门户页面焦点图管理模块</h4>
            <div class="mock-carousel-admin mb-4">
              <div class="carousel-item active">首页轮播图1 [校园迎新系列海报] <div class="del">删除此页</div></div>
              <div class="carousel-item">首页轮播图2 [反作弊诚信考试倡议] <div class="del">删除此页</div></div>
              <div class="carousel-item empty"><i class="el-icon-plus"></i> 追加更多首页宣传图片资源</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- Tab 4: 日志审计 (Audit Logs) -->
        <el-tab-pane label="重要操作审计日志" name="logs">
          <div class="p-4">
            <el-table :data="auditLogs" size="small" border="false" stripe style="width: 100%">
              <el-table-column prop="time" label="操作时间" width="160" />
              <el-table-column prop="actor" label="操作人员" width="150" />
              <el-table-column prop="action" label="具体操作行为内容" show-overflow-tooltip />
              <el-table-column prop="ip" label="来源IP地址" width="130" />
              <el-table-column label="风险评估" width="100" align="center">
                <template #default="scope">
                  <el-tag size="mini" :type="scope.row.risk ? 'danger' : 'info'">{{ scope.row.risk ? '敏感操作' : '常规操作' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('org')

const auditLogs = ref([
  { time: '2023-11-28 14:32:01', actor: '管理员::admin1', action: '修改系统配置，将全平台默认学期切换为 [2023-2024 秋季]', ip: '192.168.1.1', risk: true },
  { time: '2023-11-28 14:00:15', actor: '教职工编号::teacherL', action: '读取并尝试修改考卷试题元数据 [试卷号 P0088_Java_X_32]', ip: '10.0.8.44', risk: false },
  { time: '2023-11-28 13:59:12', actor: '管理员::admin1', action: '执行高危操作：从数据库永久删除了学生用户 [凭证底码 100003]', ip: '192.168.1.1', risk: true },
  { time: '2023-11-28 13:40:00', actor: '学生账户::studentA', action: '客户端常规登录成功连接', ip: '233.12.99.1', risk: false },
])
</script>

<style scoped>
.mb-4 { margin-bottom: 20px; }
.custom-padding :deep(.el-tabs__content) { padding: 0; }
:deep(.el-tabs__nav-wrap) { padding: 0 20px; border-bottom: 1px solid var(--glass-border); }
:deep(.el-tabs__item) { height: 50px; line-height: 50px; font-size: 15px; transition: color 0.3s; }
:deep(.el-tabs__item.is-active) { color: var(--primary-color) !important; font-weight: 600; }
:deep(.el-tabs__active-bar) { background-color: var(--primary-color) !important; height: 3px; border-radius: 3px; }

.p-4 { padding: 24px; }

/* Mock Tree */
.tree-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px dashed var(--glass-border); padding-bottom: 12px; }
.mock-tree-node { font-size: 14px; margin-top: 10px; }
.node-label { font-weight: 500; padding: 8px 12px; border-radius: 6px; cursor: pointer; transition: background 0.3s; color: var(--text-primary); }
.node-label:hover { background: rgba(0,0,0,0.05); }
.node-label.root { font-size: 15px; font-weight: bold; background: rgba(var(--primary-color-rgb), 0.1); color: var(--primary-color); }
.node-children { margin-left: 20px; padding-left: 15px; border-left: 1px dashed var(--glass-border); }
.node-leaf { padding: 6px 12px; color: var(--text-secondary); cursor: pointer; }
.node-leaf:hover { color: var(--primary-color); }

/* Mock Carousel */
.mock-carousel-admin { display: flex; gap: 15px; }
.carousel-item { height: 120px; flex: 1; border-radius: 12px; background: rgba(var(--primary-color-rgb), 0.05); border: 2px dashed rgba(var(--primary-color-rgb), 0.3); display: flex; align-items: center; justify-content: center; position: relative; font-weight: 500; color: var(--text-primary); transition: all 0.3s; cursor: pointer; }
.carousel-item:hover { border-style: solid; }
.carousel-item.active { background: rgba(var(--primary-color-rgb), 0.15); border: 2px solid var(--primary-color); }
.carousel-item .del { position: absolute; top: 10px; right: 10px; font-size: 12px; background: var(--danger-color); color: white; padding: 2px 8px; border-radius: 4px; opacity: 0; transition: opacity 0.3s; }
.carousel-item:hover .del { opacity: 1; }
.carousel-item.empty { border-color: var(--glass-border); color: var(--text-secondary); }

:deep(.el-table) { background: transparent !important; }
:deep(.el-table tr), :deep(.el-table td), :deep(.el-table th.el-table__cell) { background: transparent !important; border-bottom: 1px dashed var(--glass-border); }
</style>
