<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as visitorsApi from '../api/visitors'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref<visitorsApi.VisitorRecord[]>([])
const total = ref(0)

const query = reactive({
  status: '',
  keyword: '',
  timeRange: null as [Date, Date] | null,
})

const page = ref(1)
const pageSize = ref(20)

const hasWritePerm = computed(() => auth.hasPerm('visitor:record:write'))

const pad2 = (n: number) => String(n).padStart(2, '0')

const formatLocalDateTime = (d: Date) => {
  const y = d.getFullYear()
  const m = pad2(d.getMonth() + 1)
  const day = pad2(d.getDate())
  const hh = pad2(d.getHours())
  const mm = pad2(d.getMinutes())
  const ss = pad2(d.getSeconds())
  return `${y}-${m}-${day}T${hh}:${mm}:${ss}`
}

const formatDateTime = (s?: string | null) => {
  if (!s) return ''
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) return s
  return d.toLocaleString()
}

const extractErrMsg = (e: any) => {
  const msg = e?.response?.data?.message
  if (typeof msg === 'string' && msg.trim()) return msg
  if (typeof e?.message === 'string' && e.message.trim()) return e.message
  return '操作失败'
}

const load = async () => {
  loading.value = true
  try {
    const range = query.timeRange
    const fromAt = range && range.length === 2 && range[0] ? formatLocalDateTime(range[0]) : undefined
    const toAt = range && range.length === 2 && range[1] ? formatLocalDateTime(range[1]) : undefined

    const resp = await visitorsApi.listVisitors({
      status: query.status || undefined,
      keyword: query.keyword || undefined,
      fromAt,
      toAt,
      page: page.value,
      pageSize: pageSize.value,
      sortBy: 'id',
      sortOrder: 'desc',
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

onMounted(load)

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
  studentId: undefined as number | undefined,
  visitorName: '',
  phone: '',
  visitReason: '',
  visitAt: null as Date | null,
})

const createRules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
}

const openCreate = () => {
  createForm.studentId = undefined
  createForm.visitorName = ''
  createForm.phone = ''
  createForm.visitReason = ''
  createForm.visitAt = null
  createVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()

  try {
    const resp = await visitorsApi.createVisitor({
      studentId: createForm.studentId == null ? null : createForm.studentId,
      visitorName: createForm.visitorName,
      phone: createForm.phone || null,
      visitReason: createForm.visitReason || null,
      visitAt: createForm.visitAt ? formatLocalDateTime(createForm.visitAt) : null,
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
const detail = ref<visitorsApi.VisitorRecord | null>(null)

const openDetail = async (id: number) => {
  detailVisible.value = true
  detail.value = null

  detailLoading.value = true
  try {
    const resp = await visitorsApi.getVisitor(id)
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
}

const onLeave = async (row: visitorsApi.VisitorRecord) => {
  await ElMessageBox.confirm(`确定登记离开：${row.visitorName} 吗？`, '提示', { type: 'warning' })

  try {
    const resp = await visitorsApi.leaveVisitor(row.id)
    if (resp.code !== 0) {
      if (resp.code === 40901) {
        ElMessage.warning('该访客已登记离开')
        await load()
        return
      }
      ElMessage.error(resp.message || '操作失败')
      return
    }
    ElMessage.success('登记离开成功')
    await load()
  } catch (e: any) {
    const code = e?.response?.data?.code
    if (code === 40901) {
      ElMessage.warning('该访客已登记离开')
      await load()
      return
    }
    ElMessage.error(extractErrMsg(e))
  }
}

const statusTagType = (s: string) => {
  const v = (s || '').toUpperCase()
  if (v === 'IN') return 'success'
  if (v === 'OUT') return 'info'
  return 'warning'
}
</script>

<template>
  <div style="padding: 16px;">

    <el-card style="margin-top: 16px;">
      <template #header>
        <div style="display:flex; align-items:center; justify-content: space-between;">
          <div>访客登记</div>
          <el-button v-if="hasWritePerm" type="primary" @click="openCreate">新增登记</el-button>
        </div>
      </template>

      <div style="display:flex; gap: 12px; margin-bottom: 12px; flex-wrap: wrap;">
        <el-select v-model="query.status" placeholder="状态" style="width: 160px" clearable>
          <el-option label="在访" value="IN" />
          <el-option label="已离开" value="OUT" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="关键字（姓名/电话/原因/学号等）" style="width: 260px" clearable />
        <el-date-picker
          v-model="query.timeRange"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 360px"
          clearable
        />
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="visitorName" label="访客姓名" width="140" />
        <el-table-column prop="phone" label="电话" width="160" />
        <el-table-column prop="visitReason" label="来访原因" min-width="160" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column label="到访时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.visitAt) }}
          </template>
        </el-table-column>
        <el-table-column label="离开时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.leaveAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row.id)">查看</el-button>
            <el-button
              v-if="hasWritePerm && String(scope.row.status).toUpperCase() === 'IN'"
              size="small"
              type="warning"
              @click="onLeave(scope.row)"
            >
              登记离开
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

    <el-dialog v-model="createVisible" title="新增访客登记" width="560px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="关联学生ID">
          <el-input-number v-model="createForm.studentId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="createForm.visitorName" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="createForm.phone" />
        </el-form-item>
        <el-form-item label="来访原因">
          <el-input v-model="createForm.visitReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="到访时间">
          <el-date-picker v-model="createForm.visitAt" type="datetime" style="width: 100%" clearable />
        </el-form-item>
      </el-form>

      <template #footer>
        <div style="display:flex; justify-content: flex-end; gap: 12px;">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">提交</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="访客详情" width="720px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="detail" :column="2" border>
          <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>

          <el-descriptions-item label="访客姓名">{{ detail.visitorName }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detail.phone }}</el-descriptions-item>

          <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ detail.studentName }}</el-descriptions-item>

          <el-descriptions-item label="来访原因" :span="2">{{ detail.visitReason }}</el-descriptions-item>

          <el-descriptions-item label="到访时间">{{ formatDateTime(detail.visitAt) }}</el-descriptions-item>
          <el-descriptions-item label="离开时间">{{ formatDateTime(detail.leaveAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <div style="display:flex; justify-content: flex-end;">
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
