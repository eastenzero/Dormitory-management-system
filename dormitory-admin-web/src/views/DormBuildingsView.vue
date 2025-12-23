<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as dormApi from '../api/dorm'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref<dormApi.DormBuilding[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  genderLimit: '',
})

const page = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const form = reactive({
  code: '',
  name: '',
  genderLimit: 'UNLIMITED',
  address: '',
  status: 'ACTIVE',
})

const rules = {
  code: [{ required: true, message: '请输入楼栋编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
  genderLimit: [{ required: true, message: '请选择性别限制', trigger: 'change' }],
}

const load = async () => {
  loading.value = true
  try {
    const resp = await dormApi.listBuildings({
      keyword: query.keyword || undefined,
      genderLimit: query.genderLimit || undefined,
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

const openCreate = () => {
  editingId.value = null
  form.code = ''
  form.name = ''
  form.genderLimit = 'UNLIMITED'
  form.address = ''
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

const openEdit = (row: dormApi.DormBuilding) => {
  editingId.value = row.id
  form.code = row.code
  form.name = row.name
  form.genderLimit = row.genderLimit
  form.address = row.address || ''
  form.status = row.status
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  const payload = {
    code: form.code,
    name: form.name,
    genderLimit: form.genderLimit,
    address: form.address || null,
    status: form.status,
  }

  if (editingId.value == null) {
    const resp = await dormApi.createBuilding(payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '创建失败')
      return
    }
    ElMessage.success('创建成功')
  } else {
    const resp = await dormApi.updateBuilding(editingId.value, payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '更新失败')
      return
    }
    ElMessage.success('更新成功')
  }

  dialogVisible.value = false
  await load()
}

const onDelete = async (row: dormApi.DormBuilding) => {
  await ElMessageBox.confirm(`确定删除楼栋 ${row.name} 吗？`, '提示', { type: 'warning' })
  const resp = await dormApi.deleteBuilding(row.id)
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
          <div class="header-title">🏢 楼栋管理</div>
          <el-button v-if="auth.hasPerm('dorm:building:write')" type="primary" @click="openCreate">新增楼栋</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="关键字（编码/名称）" style="width: 220px" clearable />
        <el-select v-model="query.genderLimit" placeholder="性别限制" style="width: 160px" clearable>
          <el-option label="不限" value="UNLIMITED" />
          <el-option label="男" value="MALE" />
          <el-option label="女" value="FEMALE" />
        </el-select>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" style="width: 100%" stripe header-cell-class-name="table-header">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="code" label="编码" width="140">
           <template #default="{ row }">
             <el-tag effect="plain">{{ row.code }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="genderLimit" label="性别限制" width="120">
          <template #default="{ row }">
             <el-tag :type="row.genderLimit === 'MALE' ? 'primary' : row.genderLimit === 'FEMALE' ? 'danger' : 'info'" round>
               {{ row.genderLimit === 'MALE' ? '👦 男' : row.genderLimit === 'FEMALE' ? '👧 女' : '不限' }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
                <el-switch
                    v-model="row.status"
                    active-value="ACTIVE"
                    inactive-value="INACTIVE"
                    disabled
                    style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
                />
            </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="auth.hasPerm('dorm:building:write')"
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

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增楼栋' : '编辑楼栋'" width="520px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="dialog-form">
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="如: BLD-01" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如: 北苑1号楼" />
        </el-form-item>
        <el-form-item label="性别限制" prop="genderLimit">
          <el-radio-group v-model="form.genderLimit">
             <el-radio-button label="UNLIMITED">不限</el-radio-button>
             <el-radio-button label="MALE">男生宿舍</el-radio-button>
             <el-radio-button label="FEMALE">女生宿舍</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
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
        <el-button v-if="auth.hasPerm('dorm:building:write')" type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
