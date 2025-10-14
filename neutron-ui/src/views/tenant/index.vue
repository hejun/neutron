<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormRules } from 'element-plus'
import type Page from '@/api/Page.ts'
import { findTenantPage, saveTenant, type Tenant } from '@/api/Tenant.ts'

const loadingRef = ref(true)
const resRef = ref<Page<Tenant>>()
const showEditDialogRef = ref(false)
const editDialogLoadingRef = ref(false)

const queryRef = ref({
  current: 1,
  size: 15,
  name: undefined,
  enabled: undefined
})

const editRef = ref<Tenant>({
  id: undefined,
  name: '',
  issuer: '',
  enabled: true
})

function loadTenantPage() {
  loadingRef.value = true
  findTenantPage(queryRef.value.current, queryRef.value.size, queryRef.value.name, queryRef.value.enabled)
    .then(data => (resRef.value = data))
    // eslint-disable-next-line no-alert
    .catch(err => alert(err))
    .finally(() => (loadingRef.value = false))
}

function handleCurrentChange(current: number) {
  queryRef.value.current = current
  loadTenantPage()
}

function handleSizeChange(size: number) {
  queryRef.value.size = size
  loadTenantPage()
}

function editTenant(id?: string) {
  if (id) {
    editRef.value.id = id
  }
  showEditDialogRef.value = true
}

function submitEditTenant() {
  editDialogLoadingRef.value = true
  saveTenant(editRef.value)
    // eslint-disable-next-line no-alert
    .catch(err => alert(err))
    .finally(() => (editDialogLoadingRef.value = false))
    .then(() => (showEditDialogRef.value = false))
    .then(() => loadTenantPage())
}

const editValidRules = reactive<FormRules<Tenant>>({
  name: [
    {
      required: true,
      message: '请输入租户名',
      trigger: 'blur'
    },
    {
      min: 2,
      max: 50,
      message: '名称应在2-50个字之间'
    }
  ],
  issuer: [
    {
      required: true,
      message: '请输入Issuer',
      trigger: 'blur'
    },
    {
      min: 2,
      max: 100,
      message: 'Issuer应在2-100个字之间'
    },
    {
      validator: (rule, value, callback) =>
        /^(https?):\/\/[^\s?#]+(?::\d+)?(?:\/[^?#]*)?$/.test(value)
          ? callback()
          : callback(new Error('Issuer应为 http:// 或 https:// 开头的标准IP或域名, 且不可有参数')),
      trigger: 'blur'
    }
  ],
  enabled: [
    {
      required: true
    }
  ]
})

onMounted(() => loadTenantPage())
</script>

<template>
  <el-card shadow="never" v-loading.fullscreen.lock="editDialogLoadingRef">
    <el-form :inline="true" :model="queryRef" class="query-form">
      <el-form-item label="名称">
        <el-input v-model="queryRef.name" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="是否可用">
        <el-select v-model="queryRef.enabled" placeholder="请输入" clearable>
          <el-option label="是" value="true" />
          <el-option label="否" value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTenantPage">
          <el-icon>
            <i-mdi-search />
          </el-icon>
          <span>查询</span>
        </el-button>
      </el-form-item>
    </el-form>
    <el-button plain type="primary" @click="editTenant()">
      <el-icon>
        <i-mdi-add />
      </el-icon>
      <span>新增</span>
    </el-button>
    <el-table v-loading="loadingRef" element-loading-text="查询中..." :data="resRef?.records" class="table">
      <el-table-column prop="id" label="ID" width="176" />
      <el-table-column prop="name" label="名称" width="240" />
      <el-table-column prop="issuer" label="Issuer" show-overflow-tooltip />
      <el-table-column label="是否可用" width="80">
        <template #default="scope">
          <el-tag type="primary" v-if="scope.row.enabled">是</el-tag>
          <el-tag type="danger" v-else>否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createDate" label="创建时间" width="160" />
      <el-table-column fixed="right" label="操作" width="184">
        <template #default="scope">
          <el-button link type="primary" size="small">详情</el-button>
          <el-button link type="primary" size="small" @click="editTenant(scope.row.id)">修改</el-button>
          <el-button link type="primary" size="small">禁用</el-button>
          <el-button link type="primary" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      layout="->, total, sizes, prev, pager, next"
      :page-sizes="[10, 15, 50, 100, 200]"
      :current-page="resRef?.current"
      :page-size="resRef?.size"
      :total="resRef?.total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination"
    />
  </el-card>
  <el-dialog v-model="showEditDialogRef">
    <template #footer>
      <div class="dialog-footer">
        <el-form :model="editRef" label-width="auto" :rules="editValidRules">
          <el-form-item label="名称" prop="name">
            <el-input v-model="editRef.name" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="Issuer" prop="issuer">
            <el-input v-model="editRef.issuer" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="是否启用" prop="enabled">
            <el-switch v-model="editRef.enabled" />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="submitEditTenant">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.query-form {
  .el-input {
    --el-input-width: 220px;
  }

  .el-select {
    --el-select-width: 220px;
  }
}

.table {
  margin-top: 32px;
}

.pagination {
  margin-top: 16px;
}
</style>
