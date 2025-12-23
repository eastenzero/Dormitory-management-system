<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as dormApi from '../api/dorm'
import * as studentsApi from '../api/students'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref<dormApi.DormAssignment[]>([])
const total = ref(0)

const students = ref<studentsApi.Student[]>([])
const beds = ref<dormApi.DormBed[]>([])

const query = reactive({
  studentId: undefined as number | undefined,
  status: '',
})

const page = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  studentId: 0,
  bedId: 0,
  reason: '',
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  bedId: [{ required: true, message: '请选择床位', trigger: 'change' }],
}

const loadStudents = async () => {
  const resp = await studentsApi.listStudents({ page: 1, pageSize: 200, sortBy: 'id', sortOrder: 'desc' })
  if (resp.code === 0) {
    students.value = resp.data.list
  }
}

const loadAvailableBeds = async () => {
  const resp = await dormApi.listBeds({ status: 'AVAILABLE', page: 1, pageSize: 200, sortBy: 'id', sortOrder: 'desc' })
  if (resp.code === 0) {
    beds.value = resp.data.list
  }
}

const load = async () => {
  loading.value = true
  try {
    const resp = await dormApi.listAssignments({
      studentId: query.studentId,
      status: query.status || undefined,
      page: page.value,
      pageSize: pageSize.value,
      sortBy: 'start_at',
      sortOrder: 'desc',
    })
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '加载失败')
      return
    }
    rows.value = resp.data.list
    total.value = resp.data.total
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadStudents()
  await loadAvailableBeds()
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

const openCreate = async () => {
  await loadStudents()
  await loadAvailableBeds()
  form.studentId = students.value[0]?.id || 0
  form.bedId = beds.value[0]?.id || 0
  form.reason = ''
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  const resp = await dormApi.createAssignment({
    studentId: form.studentId,
    bedId: form.bedId,
    reason: form.reason || null,
  })
  if (resp.code !== 0) {
    ElMessage.error(resp.message || '办理入住失败')
    return
  }
  ElMessage.success('办理入住成功')
  dialogVisible.value = false
  await loadAvailableBeds()
  await load()
}

const end = async (row: dormApi.DormAssignment) => {
  await ElMessageBox.confirm(`确定为 ${row.studentName} 办理退宿吗？`, '提示', { type: 'warning' })
  const resp = await dormApi.endAssignment(row.id, { reason: 'checkout' })
  if (resp.code !== 0) {
    ElMessage.error(resp.message || '退宿失败')
    return
  }
  ElMessage.success('退宿成功')
  await loadAvailableBeds()
  await load()
}
</script>

<template>
  <div style="padding: 16px;">

    <el-card style="margin-top: 16px;">
      <template #header>
        <div style="display:flex; align-items:center; justify-content: space-between;">
          <div>入住管理</div>
          <el-button v-if="auth.hasPerm('dorm:assignment:write')" type="primary" @click="openCreate">办理入住</el-button>
        </div>
      </template>

      <div style="display:flex; gap: 12px; margin-bottom: 12px; flex-wrap: wrap;">
        <el-select v-model="query.studentId" placeholder="学生" style="width: 260px" clearable filterable>
          <el-option v-for="s in students" :key="s.id" :label="`${s.studentNo}-${s.name}`" :value="s.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" style="width: 160px" clearable>
          <el-option label="在住" value="ACTIVE" />
          <el-option label="已结束" value="ENDED" />
        </el-select>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="buildingCode" label="楼栋" width="120" />
        <el-table-column prop="roomNo" label="房间" width="120" />
        <el-table-column prop="bedNo" label="床位" width="100" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="startAt" label="开始时间" width="200" />
        <el-table-column prop="endAt" label="结束时间" width="200" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
              v-if="auth.hasPerm('dorm:assignment:write') && scope.row.status === 'ACTIVE'"
              size="small"
              type="warning"
              @click="end(scope.row)"
            >
              退宿
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

    <el-dialog v-model="dialogVisible" title="办理入住" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" style="width: 100%" filterable>
            <el-option v-for="s in students" :key="s.id" :label="`${s.studentNo}-${s.name}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位" prop="bedId">
          <el-select v-model="form.bedId" style="width: 100%" filterable>
            <el-option
              v-for="b in beds"
              :key="b.id"
              :label="`${b.buildingCode}-${b.roomNo}-${b.bedNo}`"
              :value="b.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="form.reason" placeholder="可选" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="auth.hasPerm('dorm:assignment:write')" type="primary" @click="submit">确认入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>
