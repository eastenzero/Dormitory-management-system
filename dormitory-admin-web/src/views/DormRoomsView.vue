<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as dormApi from '../api/dorm'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref<dormApi.DormRoom[]>([])
const total = ref(0)

const buildings = ref<dormApi.DormBuilding[]>([])

const query = reactive({
  buildingId: undefined as number | undefined,
  floorNo: undefined as number | undefined,
  roomNo: '',
  status: '',
  keyword: '',
})

const page = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const form = reactive({
  buildingId: 0,
  floorNo: 1,
  roomNo: '',
  roomType: '',
  genderLimit: 'UNLIMITED',
  status: 'ACTIVE',
})

const rules = {
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floorNo: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
}

const loadBuildings = async () => {
  const resp = await dormApi.listBuildings({ page: 1, pageSize: 200, sortBy: 'id', sortOrder: 'desc' })
  if (resp.code === 0) {
    buildings.value = resp.data.list
  }
}

const load = async () => {
  loading.value = true
  try {
    const resp = await dormApi.listRooms({
      buildingId: query.buildingId,
      floorNo: query.floorNo,
      roomNo: query.roomNo || undefined,
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
  form.buildingId = buildings.value[0]?.id || 0
  form.floorNo = 1
  form.roomNo = ''
  form.roomType = ''
  form.genderLimit = 'UNLIMITED'
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

const openEdit = (row: dormApi.DormRoom) => {
  editingId.value = row.id
  form.buildingId = row.buildingId
  form.floorNo = row.floorNo
  form.roomNo = row.roomNo
  form.roomType = row.roomType || ''
  form.genderLimit = row.genderLimit
  form.status = row.status
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  const payload = {
    buildingId: form.buildingId,
    floorNo: Number(form.floorNo),
    roomNo: form.roomNo,
    roomType: form.roomType || null,
    genderLimit: form.genderLimit,
    status: form.status,
  }

  if (editingId.value == null) {
    const resp = await dormApi.createRoom(payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '创建失败')
      return
    }
    ElMessage.success('创建成功')
  } else {
    const resp = await dormApi.updateRoom(editingId.value, payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '更新失败')
      return
    }
    ElMessage.success('更新成功')
  }

  dialogVisible.value = false
  await load()
}

const onDelete = async (row: dormApi.DormRoom) => {
  await ElMessageBox.confirm(`确定删除房间 ${row.buildingCode}-${row.roomNo} 吗？`, '提示', { type: 'warning' })
  const resp = await dormApi.deleteRoom(row.id)
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
          <div class="header-title">🚪 房间管理</div>
          <el-button v-if="auth.hasPerm('dorm:room:write')" type="primary" @click="openCreate">新增房间</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-select v-model="query.buildingId" placeholder="楼栋" style="width: 200px" clearable>
          <el-option v-for="b in buildings" :key="b.id" :label="`${b.code}-${b.name}`" :value="b.id" />
        </el-select>
        <el-input-number v-model="query.floorNo" :min="1" placeholder="楼层" style="width: 120px" />
        <el-input v-model="query.roomNo" placeholder="房间号" style="width: 120px" clearable />
        <el-select v-model="query.status" placeholder="状态" style="width: 120px" clearable>
          <el-option label="启用" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="关键字" style="width: 180px" clearable />
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="buildingCode" label="楼栋" width="120">
             <template #default="{ row }">
                 <el-tag type="info" effect="plain">{{ row.buildingCode }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="120">
            <template #default="{ row }">
                 <span style="font-weight: bold; color: var(--color-primary)">{{ row.roomNo }}</span>
             </template>
        </el-table-column>
        <el-table-column prop="floorNo" label="楼层" width="100" />
        <el-table-column prop="roomType" label="类型" />
        <el-table-column prop="genderLimit" label="性别限制" width="120">
          <template #default="{ row }">
             <el-tag :type="row.genderLimit === 'MALE' ? 'primary' : row.genderLimit === 'FEMALE' ? 'danger' : 'info'" round>
               {{ row.genderLimit === 'MALE' ? '👦 男' : row.genderLimit === 'FEMALE' ? '👧 女' : '不限' }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
             <template #default="{ row }">
                 <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="auth.hasPerm('dorm:room:write')"
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

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增房间' : '编辑房间'" width="520px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="`${b.code}-${b.name}`" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floorNo">
          <el-input-number v-model="form.floorNo" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="房间号" prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="如: 101" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="form.roomType" placeholder="如：4人间" />
        </el-form-item>
        <el-form-item label="性别限制">
          <el-radio-group v-model="form.genderLimit">
             <el-radio-button label="UNLIMITED">不限</el-radio-button>
             <el-radio-button label="MALE">男</el-radio-button>
             <el-radio-button label="FEMALE">女</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
           <el-switch
                v-model="form.status"
                active-value="ACTIVE"
                inactive-value="INACTIVE"
                active-text="启用"
                inactive-text="停用"
            />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="auth.hasPerm('dorm:room:write')" type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
