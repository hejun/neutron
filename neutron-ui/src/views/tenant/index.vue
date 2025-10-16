<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type Page from '@/api/Page.ts'
import { findTenantById, findTenantPage, saveTenant, type Tenant, updateTenant } from '@/api/Tenant.ts'

const tableLoadingRef = ref(true)
const tableDataRef = ref<Page<Tenant>>()

const editDialogVisibleRef = ref(false)
const editDialogLoadingRef = ref(false)
const editDialogTitleRef = ref('')
const editTenantFormRef = useTemplateRef<FormInstance>('editTenantFormRef')

const queryRef = ref({
  current: 1,
  size: 15,
  name: undefined,
  enabled: undefined
})

const editRef = ref<Tenant>()

function loadTenantPage() {
  tableLoadingRef.value = true
  findTenantPage(queryRef.value.current, queryRef.value.size, queryRef.value.name, queryRef.value.enabled)
    .then(data => (tableDataRef.value = data))
    // eslint-disable-next-line no-alert
    .catch(err => alert(err))
    .finally(() => (tableLoadingRef.value = false))
}

function handleCurrentChange(current: number) {
  queryRef.value.current = current
  loadTenantPage()
}

function handleSizeChange(size: number) {
  queryRef.value.size = size
  loadTenantPage()
}

async function editTenant(id?: string) {
  resetTenantForm()
  editDialogTitleRef.value = '新增租户'
  if (id) {
    editDialogTitleRef.value = '修改租户'
    editRef.value = await findTenantById(id)
  }
  editDialogVisibleRef.value = true
}

function resetTenantForm() {
  editRef.value = {
    id: undefined,
    name: '',
    issuer: '',
    termsOfServiceTitle: '',
    termsOfServiceDesc: '',
    termsOfServiceContent: '',
    privacyPolicyTitle: '',
    privacyPolicyDesc: '',
    privacyPolicyContent: '',
    enabled: true
  }
}

async function submitEditTenant(editTenantForm: FormInstance | null) {
  if (!editTenantForm) return
  await editTenantForm.validate(valid => {
    if (valid) {
      editDialogLoadingRef.value = true
      editDialogVisibleRef.value = false
      let resp
      if (editRef?.value?.id) {
        resp = updateTenant(editRef.value)
      } else {
        resp = saveTenant(editRef.value!)
      }
      resp
        // eslint-disable-next-line no-alert
        .catch(err => alert(err))
        .then(() => resetTenantForm())
        .finally(() => (editDialogLoadingRef.value = false))
        .then(() => loadTenantPage())
    }
  })
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
  ],
  termsOfServiceTitle: [
    {
      max: 50,
      message: '标题应在50个字以内'
    }
  ],
  termsOfServiceDesc: [
    {
      max: 100,
      message: '描述应在100个字以内'
    }
  ],
  privacyPolicyTitle: [
    {
      max: 50,
      message: '标题应在50个字以内'
    }
  ],
  privacyPolicyDesc: [
    {
      max: 100,
      message: '描述应在100个字以内'
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
    <el-table v-loading="tableLoadingRef" element-loading-text="查询中..." :data="tableDataRef?.records" class="table">
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
      <el-table-column prop="lastModifiedDate" label="最后更新时间" width="160" />
      <el-table-column fixed="right" label="操作" width="144">
        <template #default="scope">
          <el-button link type="primary" size="small">详情</el-button>
          <el-button link type="primary" size="small" @click="editTenant(scope.row.id)">修改</el-button>
          <el-button link type="primary" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      layout="->, total, sizes, prev, pager, next"
      :page-sizes="[10, 15, 50, 100, 200]"
      :current-page="tableDataRef?.current"
      :page-size="tableDataRef?.size"
      :total="tableDataRef?.total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination"
    />
  </el-card>
  <el-dialog v-model="editDialogVisibleRef" :title="editDialogTitleRef" destroy-on-close>
    <template #footer>
      <div class="dialog-footer">
        <el-form ref="editTenantFormRef" :model="editRef" label-width="auto" :rules="editValidRules">
          <el-form-item label="名称" prop="name">
            <el-input v-model="editRef!.name" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="Issuer" prop="issuer">
            <el-input v-model="editRef!.issuer" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="是否启用" prop="enabled">
            <el-switch v-model="editRef!.enabled" />
          </el-form-item>
          <el-form-item label="服务协议标题" prop="termsOfServiceTitle">
            <el-input v-model="editRef!.termsOfServiceTitle" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="服务协议描述" prop="termsOfServiceDesc">
            <el-input v-model="editRef!.termsOfServiceDesc" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="服务协议内容" prop="termsOfServiceContent">
            <el-input v-model="editRef!.termsOfServiceContent" type="textarea" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="隐私条款标题" prop="privacyPolicyTitle">
            <el-input v-model="editRef!.privacyPolicyTitle" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="隐私条款描述" prop="privacyPolicyDesc">
            <el-input v-model="editRef!.privacyPolicyDesc" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="隐私条款内容" prop="privacyPolicyContent">
            <el-input v-model="editRef!.privacyPolicyContent" type="textarea" placeholder="请输入" />
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="submitEditTenant(editTenantFormRef)">确认</el-button>
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
