<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as dormApi from '../api/dorm'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref<dormApi.DormBed[]>([])
const total = ref(0)

const buildings = ref<dormApi.DormBuilding[]>([])
const rooms = ref<dormApi.DormRoom[]>([])

const query = reactive({
  buildingId: undefined as number | undefined,
  roomId: undefined as number | undefined,
  status: '',
  keyword: '',
})

const page = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const form = reactive({
  roomId: 0,
  bedNo: '',
  status: 'AVAILABLE',
})

const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  bedNo: [{ required: true, message: '请输入床位号', trigger: 'blur' }],
}

const loadBuildings = async () => {
  const resp = await dormApi.listBuildings({ page: 1, pageSize: 200, sortBy: 'id', sortOrder: 'desc' })
  if (resp.code === 0) {
    buildings.value = resp.data.list
  }
}

const loadRooms = async () => {
  const resp = await dormApi.listRooms({ page: 1, pageSize: 200, sortBy: 'id', sortOrder: 'desc' })
  if (resp.code === 0) {
    rooms.value = resp.data.list
  }
}

const load = async () => {
  loading.value = true
  try {
    const resp = await dormApi.listBeds({
      buildingId: query.buildingId,
      roomId: query.roomId,
      status: query.status || undefined,
      keyword: query.keyword || undefined,
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
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadBuildings()
  await loadRooms()
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

const openCreate = () => {
  editingId.value = null
  form.roomId = rooms.value[0]?.id || 0
  form.bedNo = ''
  form.status = 'AVAILABLE'
  dialogVisible.value = true
}

const openEdit = (row: dormApi.DormBed) => {
  editingId.value = row.id
  form.roomId = row.roomId
  form.bedNo = row.bedNo
  form.status = row.status
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  const payload = {
    roomId: form.roomId,
    bedNo: form.bedNo,
    status: form.status,
  }

  if (editingId.value == null) {
    const resp = await dormApi.createBed(payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '创建失败')
      return
    }
    ElMessage.success('创建成功')
  } else {
    const resp = await dormApi.updateBed(editingId.value, payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '更新失败')
      return
    }
    ElMessage.success('更新成功')
  }

  dialogVisible.value = false
  await load()
}

const onDelete = async (row: dormApi.DormBed) => {
  await ElMessageBox.confirm(`确定删除床位 ${row.buildingCode}-${row.roomNo}-${row.bedNo} 吗？`, '提示', { type: 'warning' })
  const resp = await dormApi.deleteBed(row.id)
  if (resp.code !== 0) {
    ElMessage.error(resp.message || '删除失败')
    return
  }
  ElMessage.success('删除成功')
  await load()
}
</script>

<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-title">🛏️ 床位管理</div>
          <el-button v-if="auth.hasPerm('dorm:bed:write')" type="primary" @click="openCreate">新增床位</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-select v-model="query.buildingId" placeholder="楼栋" style="width: 200px" clearable>
          <el-option v-for="b in buildings" :key="b.id" :label="`${b.code}-${b.name}`" :value="b.id" />
        </el-select>
        <el-select v-model="query.roomId" placeholder="房间" style="width: 200px" clearable>
          <el-option v-for="r in rooms" :key="r.id" :label="`${r.buildingCode}-${r.roomNo}`" :value="r.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" style="width: 160px" clearable>
          <el-option label="空闲" value="AVAILABLE" />
          <el-option label="占用" value="OCCUPIED" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="关键字" style="width: 200px" clearable />
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="buildingCode" label="楼栋" width="120">
             <template #default="{ row }">
                 <el-tag type="info" effect="plain">{{ row.buildingCode }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间" width="120">
            <template #default="{ row }">
                 <span style="font-weight: bold;">{{ row.roomNo }}</span>
             </template>
        </el-table-column>
        <el-table-column prop="bedNo" label="床位" width="120">
            <template #default="{ row }">
                 <el-tag effect="dark" type="success">{{ row.bedNo }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
             <template #default="{ row }">
                 <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'warning'">{{ row.status === 'AVAILABLE' ? '空闲' : '占用' }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="auth.hasPerm('dorm:bed:write')"
              size="small"
              type="danger"
              plain
              @click="onDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
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

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增床位' : '编辑床位'" width="520px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="房间" prop="roomId">
          <el-select v-model="form.roomId" style="width: 100%">
            <el-option v-for="r in rooms" :key="r.id" :label="`${r.buildingCode}-${r.roomNo}`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位号" prop="bedNo">
          <el-input v-model="form.bedNo" placeholder="如：A/B/C" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="空闲" value="AVAILABLE" />
            <el-option label="占用" value="OCCUPIED" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="auth.hasPerm('dorm:bed:write')" type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
