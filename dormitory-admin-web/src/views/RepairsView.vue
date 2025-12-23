<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as repairsApi from '../api/repairs'
import * as sysApi from '../api/sys'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref<repairsApi.RepairOrder[]>([])
const total = ref(0)

const users = ref<sysApi.SysUser[]>([])

const query = reactive({
  status: '',
  priority: '',
  assigneeUserId: undefined as number | undefined,
  keyword: '',
  sortBy: 'id',
  sortOrder: 'desc',
})

const page = ref(1)
const pageSize = ref(20)

const formatDateTime = (s?: string | null) => {
  if (!s) return ''
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) return s
  return d.toLocaleString()
}

const isFinal = (status: string) => {
  const s = (status || '').toUpperCase()
  return s === 'DONE' || s === 'REJECTED'
}

const extractErrMsg = (e: any) => {
  const msg = e?.response?.data?.message
  if (typeof msg === 'string' && msg.trim()) return msg
  if (typeof e?.message === 'string' && e.message.trim()) return e.message
  return '操作失败'
}

const loadUsers = async () => {
  try {
    const resp = await sysApi.listUsers()
    if (resp.code === 0) {
      users.value = resp.data
    }
  } catch (e) {
    return
  }
}

const load = async () => {
  loading.value = true
  try {
    const resp = await repairsApi.listRepairs({
      status: query.status || undefined,
      priority: query.priority || undefined,
      assigneeUserId: query.assigneeUserId,
      keyword: query.keyword || undefined,
      page: page.value,
      pageSize: pageSize.value,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder,
    })
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '加载失败')
      return
    }
    rows.value = resp.data.list
    total.value = resp.data.total
  } catch (e) {
    ElMessage.error(extractErrMsg(e))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadUsers()
  await load()
})

const onSearch = async () => {
  page.value = 1
  await load()
}

const onSizeChange = async (val: number) => {
  pageSize.value = val
  page.value = 1
  await load()
}

const onCurrentChange = async (val: number) => {
  page.value = val
  await load()
}

const createVisible = ref(false)
const createFormRef = ref()
const createForm = reactive({
  title: '',
  priority: 'MEDIUM',
  description: '',
})

const createRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
}

const openCreate = () => {
  createForm.title = ''
  createForm.priority = 'MEDIUM'
  createForm.description = ''
  createVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()

  try {
    const resp = await repairsApi.createRepair({
      title: createForm.title,
      priority: createForm.priority,
      description: createForm.description || null,
    })
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '创建失败')
      return
    }
    ElMessage.success('创建成功')
    createVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error(extractErrMsg(e))
  }
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<repairsApi.RepairOrderDetail | null>(null)
const logsLoading = ref(false)
const logs = ref<repairsApi.RepairLog[]>([])

const hasLogPerm = computed(() => auth.hasPerm('repair:log:read'))

const openDetail = async (id: number) => {
  detailVisible.value = true
  detail.value = null
  logs.value = []

  detailLoading.value = true
  try {
    const resp = await repairsApi.getRepair(id)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '加载失败')
      return
    }
    detail.value = resp.data
  } catch (e) {
    ElMessage.error(extractErrMsg(e))
  } finally {
    detailLoading.value = false
  }

  if (hasLogPerm.value) {
    logsLoading.value = true
    try {
      const resp = await repairsApi.listRepairLogs(id)
      if (resp.code !== 0) {
        ElMessage.error(resp.message || '加载日志失败')
        return
      }
      logs.value = resp.data
    } catch (e) {
      ElMessage.error(extractErrMsg(e))
    } finally {
      logsLoading.value = false
    }
  }
}

const assignVisible = ref(false)
const assignFormRef = ref()
const assignTargetId = ref<number | null>(null)
const assignForm = reactive({
  assigneeUserId: 0,
  content: '',
})

const assignRules = {
  assigneeUserId: [{ required: true, message: '请选择指派人', trigger: 'change' }],
}

const openAssign = async (row: repairsApi.RepairOrder) => {
  await loadUsers()
  assignTargetId.value = row.id
  assignForm.assigneeUserId = users.value[0]?.id || 0
  assignForm.content = ''
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!assignFormRef.value || assignTargetId.value == null) return
  await assignFormRef.value.validate()

  try {
    const resp = await repairsApi.assignRepair(assignTargetId.value, {
      assigneeUserId: assignForm.assigneeUserId,
      content: assignForm.content || null,
    })
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '指派失败')
      return
    }
    ElMessage.success('指派成功')
    assignVisible.value = false

    if (detail.value?.id === assignTargetId.value) {
      await openDetail(assignTargetId.value)
    }
    await load()
  } catch (e) {
    ElMessage.error(extractErrMsg(e))
  }
}

const transitionVisible = ref(false)
const transitionFormRef = ref()
const transitionTargetId = ref<number | null>(null)
const transitionCurrentStatus = ref<repairsApi.RepairStatus | ''>('')
const transitionForm = reactive({
  status: 'IN_PROGRESS',
  content: '',
})

const transitionRules = {
  status: [{ required: true, message: '请选择目标状态', trigger: 'change' }],
}

const allowedTransitionStatuses = computed(() => {
  const cur = (transitionCurrentStatus.value || '').toUpperCase()
  if (cur === 'SUBMITTED') return ['IN_PROGRESS', 'REJECTED']
  if (cur === 'IN_PROGRESS') return ['DONE', 'REJECTED']
  return []
})

const openTransition = (row: repairsApi.RepairOrder) => {
  transitionTargetId.value = row.id
  transitionCurrentStatus.value = row.status
  transitionForm.content = ''

  const allow = allowedTransitionStatuses.value
  transitionForm.status = allow[0] || 'IN_PROGRESS'

  transitionVisible.value = true
}

const submitTransition = async () => {
  if (!transitionFormRef.value || transitionTargetId.value == null) return
  await transitionFormRef.value.validate()

  try {
    await ElMessageBox.confirm('确认进行状态流转吗？', '提示', { type: 'warning' })
    const resp = await repairsApi.transitionRepair(transitionTargetId.value, {
      status: transitionForm.status,
      content: transitionForm.content || null,
    })
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '流转失败')
      return
    }
    ElMessage.success('流转成功')
    transitionVisible.value = false

    if (detail.value?.id === transitionTargetId.value) {
      await openDetail(transitionTargetId.value)
    }
    await load()
  } catch (e) {
    ElMessage.error(extractErrMsg(e))
  }
}
</script>

<template>
  <div style="padding: 16px;">

    <el-card style="margin-top: 16px;">
      <template #header>
        <div style="display:flex; align-items:center; justify-content: space-between;">
          <div>报修工单</div>
          <el-button v-if="auth.hasPerm('repair:order:write')" type="primary" @click="openCreate">创建工单</el-button>
        </div>
      </template>

      <div style="display:flex; gap: 12px; margin-bottom: 12px; flex-wrap: wrap;">
        <el-select v-model="query.status" placeholder="状态" style="width: 160px" clearable>
          <el-option label="已提交" value="SUBMITTED" />
          <el-option label="处理中" value="IN_PROGRESS" />
          <el-option label="已完成" value="DONE" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>

        <el-select v-model="query.priority" placeholder="优先级" style="width: 160px" clearable>
          <el-option label="低" value="LOW" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="高" value="HIGH" />
        </el-select>

        <el-select v-model="query.assigneeUserId" placeholder="指派人" style="width: 220px" clearable filterable>
          <el-option v-for="u in users" :key="u.id" :label="`${u.username}${u.realName ? '-' + u.realName : ''}`" :value="u.id" />
        </el-select>

        <el-input v-model="query.keyword" placeholder="关键字（标题/描述）" style="width: 240px" clearable />

        <el-select v-model="query.sortBy" placeholder="排序字段" style="width: 140px">
          <el-option label="ID" value="id" />
          <el-option label="创建时间" value="createdAt" />
        </el-select>

        <el-select v-model="query.sortOrder" placeholder="排序" style="width: 120px">
          <el-option label="降序" value="desc" />
          <el-option label="升序" value="asc" />
        </el-select>

        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="priority" label="优先级" width="120" />
        <el-table-column prop="status" label="状态" width="140" />
        <el-table-column label="指派人" width="200">
          <template #default="scope">
            <span v-if="scope.row.assigneeUserId">
              {{ scope.row.assigneeUsername }}<span v-if="scope.row.assigneeRealName">-{{ scope.row.assigneeRealName }}</span>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="200">
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row.id)">详情</el-button>
            <el-button
              v-if="auth.hasPerm('repair:order:assign') && !isFinal(scope.row.status)"
              size="small"
              type="primary"
              @click="openAssign(scope.row)"
            >
              指派
            </el-button>
            <el-button
              v-if="auth.hasPerm('repair:order:write') && !isFinal(scope.row.status)"
              size="small"
              type="warning"
              @click="openTransition(scope.row)"
            >
              流转
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex; justify-content: flex-end; margin-top: 12px;">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          @size-change="onSizeChange"
          @current-change="onCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="创建报修工单" width="560px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="createForm.priority" style="width: 100%">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button v-if="auth.hasPerm('repair:order:write')" type="primary" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="工单详情" size="50%">
      <div v-loading="detailLoading" style="padding-right: 12px;">
        <el-descriptions v-if="detail" :column="2" border>
          <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ detail.priority }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="指派人" :span="2">
            <span v-if="detail.assigneeUserId">
              {{ detail.assigneeUsername }}<span v-if="detail.assigneeRealName">-{{ detail.assigneeRealName }}</span>
            </span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="学生" :span="2">
            <span v-if="detail.studentId">{{ detail.studentNo }}-{{ detail.studentName }}</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="位置" :span="2">
            <span v-if="detail.buildingId || detail.roomId">
              {{ detail.buildingCode }}-{{ detail.roomNo }}
            </span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 16px;">
          <div style="display:flex; align-items:center; justify-content: space-between; margin-bottom: 8px;">
            <div>日志</div>
          </div>

          <div v-if="!hasLogPerm">
            <el-alert title="无权限查看日志" type="info" show-icon />
          </div>

          <el-table v-else :data="logs" v-loading="logsLoading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="action" label="动作" width="160" />
            <el-table-column prop="content" label="内容" />
            <el-table-column label="时间" width="200">
              <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="assignVisible" title="指派工单" width="520px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="90px">
        <el-form-item label="指派给" prop="assigneeUserId">
          <el-select v-model="assignForm.assigneeUserId" style="width: 100%" filterable>
            <el-option v-for="u in users" :key="u.id" :label="`${u.username}${u.realName ? '-' + u.realName : ''}`" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.content" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button v-if="auth.hasPerm('repair:order:assign')" type="primary" @click="submitAssign">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transitionVisible" title="状态流转" width="520px">
      <el-form ref="transitionFormRef" :model="transitionForm" :rules="transitionRules" label-width="90px">
        <el-form-item label="目标状态" prop="status">
          <el-select v-model="transitionForm.status" style="width: 100%">
            <el-option v-for="s in allowedTransitionStatuses" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="transitionForm.content" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="transitionVisible = false">取消</el-button>
        <el-button v-if="auth.hasPerm('repair:order:write')" type="primary" @click="submitTransition">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>
