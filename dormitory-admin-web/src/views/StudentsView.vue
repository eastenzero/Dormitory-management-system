<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import * as studentsApi from '../api/students'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref<studentsApi.Student[]>([])
const total = ref(0)

const query = reactive({
  gender: '',
  status: '',
  keyword: '',
})

const page = ref(1)
const pageSize = ref(20)

const dialogVisible = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const form = reactive({
  studentNo: '',
  name: '',
  gender: 'MALE',
  college: '',
  major: '',
  className: '',
  phone: '',
  status: 'IN_SCHOOL',
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
}

const load = async () => {
  loading.value = true
  try {
    const resp = await studentsApi.listStudents({
      gender: query.gender || undefined,
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
  form.studentNo = ''
  form.name = ''
  form.gender = 'MALE'
  form.college = ''
  form.major = ''
  form.className = ''
  form.phone = ''
  form.status = 'IN_SCHOOL'
  dialogVisible.value = true
}

const openEdit = (row: studentsApi.Student) => {
  editingId.value = row.id
  form.studentNo = row.studentNo
  form.name = row.name
  form.gender = row.gender
  form.college = row.college || ''
  form.major = row.major || ''
  form.className = row.className || ''
  form.phone = row.phone || ''
  form.status = row.status
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  const payload = {
    studentNo: form.studentNo,
    name: form.name,
    gender: form.gender,
    college: form.college || null,
    major: form.major || null,
    className: form.className || null,
    phone: form.phone || null,
    status: form.status,
  }

  if (editingId.value == null) {
    const resp = await studentsApi.createStudent(payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '创建失败')
      return
    }
    ElMessage.success('创建成功')
  } else {
    const resp = await studentsApi.updateStudent(editingId.value, payload)
    if (resp.code !== 0) {
      ElMessage.error(resp.message || '更新失败')
      return
    }
    ElMessage.success('更新成功')
  }

  dialogVisible.value = false
  await load()
}

const onDelete = async (row: studentsApi.Student) => {
  await ElMessageBox.confirm(`确定删除学生 ${row.name}（${row.studentNo}）吗？`, '提示', { type: 'warning' })
  const resp = await studentsApi.deleteStudent(row.id)
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
          <div class="header-title">🎓 学生管理</div>
          <el-button v-if="auth.hasPerm('student:write')" type="primary" @click="openCreate">新增学生</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-select v-model="query.gender" placeholder="性别" style="width: 160px" clearable>
          <el-option label="男" value="MALE" />
          <el-option label="女" value="FEMALE" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" style="width: 180px" clearable>
          <el-option label="在校" value="IN_SCHOOL" />
          <el-option label="毕业" value="GRADUATED" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="关键字（学号/姓名/电话）" style="width: 240px" clearable />
        <el-button type="primary" @click="onSearch">查询</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="studentNo" label="学号" width="140">
            <template #default="{ row }">
                 <el-tag effect="plain" type="info">{{ row.studentNo }}</el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120">
             <template #default="{ row }">
                 <span style="font-weight: 600;">{{ row.name }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="120">
          <template #default="{ row }">
             <el-tag :type="row.gender === 'MALE' ? 'primary' : 'danger'" round>
               {{ row.gender === 'MALE' ? '👦 男' : '👧 女' }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="160" />
        <el-table-column prop="status" label="状态" width="140">
           <template #default="{ row }">
                 <el-tag :type="row.status === 'IN_SCHOOL' ? 'success' : 'info'">{{ row.status === 'IN_SCHOOL' ? '在校' : '毕业' }}</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="auth.hasPerm('student:write')"
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

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增学生' : '编辑学生'" width="560px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" style="width: 100%">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="form.college" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="在校" value="IN_SCHOOL" />
            <el-option label="毕业" value="GRADUATED" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="auth.hasPerm('student:write')" type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
