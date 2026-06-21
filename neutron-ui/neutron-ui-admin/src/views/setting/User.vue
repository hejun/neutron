<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { TableSortOrder } from 'element-plus/es/components/table/src/table/defaults'
import type { FormInstance, FormRules, TableColumnCtx, UploadFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import { upload } from '@/api/fs/FileStorage.ts'
import { findPage as findTenantPage } from '@/api/auth/Tenant.ts'
import { del, findById, findPage, save, update, type UserDetail, type UserList } from '@/api/auth/User.ts'

const BASE_URL = import.meta.env.VITE_BASE_URL || ''

const tableQuery = reactive({
  loading: false,
  tenantLoading: false,
  username: '',
  enabled: '',
  tenantId: '',
  tenantSelectOptions: [] as { value: string; label: string }[],
  page: 0,
  size: 10,
  sort: ''
})

const tableData = reactive<Page<UserList>>({
  page: { number: 0, size: 10, totalElements: 0, totalPages: 0 },
  content: []
})

const defaultEmptyUser: UserDetail = {
  id: '',
  username: '',
  password: '',
  email: '',
  emailVerified: false,
  phoneNumber: '',
  phoneNumberVerified: false,
  nickname: '',
  gender: 0,
  avatar: '',
  birthdate: '',
  enabled: true,
  tenant: {
    id: '',
    name: '',
    issuer: '',
    enabled: true,
    createdDate: '',
    lastModifiedDate: ''
  },
  createdDate: '',
  lastModifiedDate: ''
}

const tableEvent = {
  queryTenantForSearch: (query: string) => {
    tableQuery.tenantLoading = true
    findTenantPage(query, true)
      .then(data => {
        if (data?.content) {
          tableQuery.tenantSelectOptions = data.content.map(t => ({ value: t.id!, label: t.name }))
        }
      })
      .finally(() => (tableQuery.tenantLoading = false))
  },
  query: () => {
    tableQuery.loading = true
    findPage(
      tableQuery.username,
      tableQuery.enabled,
      tableQuery.tenantId,
      tableQuery.page,
      tableQuery.size,
      tableQuery.sort
    )
      .then(data => {
        if (data) {
          tableData.page = data.page
          tableData.content = data.content
        }
      })
      .catch(() => ElMessage.error('加载失败，请稍后重试'))
      .finally(() => {
        tableQuery.loading = false
      })
  },
  currentChange: (current: number) => {
    tableQuery.page = current - 1
    tableEvent.query()
  },
  sizeChange: (size: number) => {
    tableQuery.page = 0
    tableQuery.size = size
    tableEvent.query()
  },
  add: () => {
    drawerData.user = { ...defaultEmptyUser }
    drawerData.tenantSelectOptions = []
    drawerData.open = true
  },
  edit: (id: string) => {
    tableQuery.loading = true
    drawerData.user = { ...defaultEmptyUser }
    findById(id)
      .then(user => {
        if (user) {
          drawerData.user = user
          drawerData.tenantSelectOptions = [{ value: user.tenant.id!, label: user.tenant.name }]
        }
        drawerData.open = true
      })
      .finally(() => {
        tableQuery.loading = false
      })
  },
  del: (id: string) => {
    tableQuery.loading = true
    del(id)
      .then(() => {
        ElMessage.success('删除成功！')
        tableEvent.query()
      })
      .catch(() => ElMessage.error('删除失败，请稍后重试'))
      .finally(() => {
        tableQuery.loading = false
      })
  },
  sort: (data: { column: TableColumnCtx<UserList>; prop: string | null; order: TableSortOrder | null }) => {
    tableQuery.sort = data.order ? `${data.prop},${data.order === 'descending' ? 'desc' : 'asc'}` : ''
    tableEvent.query()
  }
}

const userEditForm = ref<FormInstance>()
const drawerData = reactive({
  open: false,
  tenantLoading: false,
  tenantSelectOptions: [] as { value: string; label: string }[],
  user: { ...defaultEmptyUser },
  changedAvatar: null as File | null,
  validRules: {
    'tenant.id': [
      {
        required: true,
        message: '请选择所属租户',
        trigger: 'blur'
      }
    ],
    username: [
      {
        required: true,
        message: '请输入客户端ID',
        trigger: 'blur'
      },
      {
        min: 2,
        max: 50,
        message: '名称应在2-50个字之间',
        trigger: 'blur'
      }
    ],
    email: [
      {
        type: 'email',
        message: '请输入正确的邮箱地址',
        trigger: 'blur'
      }
    ],
    phoneNumber: [
      {
        pattern: /^1[3-9]\d{9}$/,
        message: '请输入正确的手机号码',
        trigger: 'blur'
      }
    ],
    enabled: [
      {
        required: true,
        trigger: 'blur'
      }
    ]
  } as FormRules<{
    tenantId: string
    clientId: string
    name: string
    redirectUris: string[]
    postLogoutRedirectUris: string[]
    enabled: boolean
    accessTokenTimeToLive: number
    refreshTokenTimeToLive: number
  }>
})

const drawerEvent = {
  queryTenantForSearch: (query: string) => {
    drawerData.tenantLoading = true
    findTenantPage(query, true)
      .then(data => {
        if (data?.content) {
          drawerData.tenantSelectOptions = data.content.map(t => ({ value: t.id!, label: t.name }))
        }
      })
      .finally(() => (drawerData.tenantLoading = false))
  },
  generateUserSecret: () => (drawerData.user.password = Math.random().toString(36).slice(2)),
  clearUserSecret: () => (drawerData.user.password = undefined),
  previewAvatarBeforeUpload: (uploadFile: UploadFile) => {
    const file = uploadFile.raw
    if (file) {
      drawerData.user.avatar = URL.createObjectURL(file as Blob)
      drawerData.changedAvatar = file
    }
  },
  submit: async () => {
    if (!userEditForm.value) return
    await userEditForm.value.validate(async valid => {
      if (valid) {
        tableQuery.loading = true
        try {
          if (drawerData.changedAvatar) {
            const uploadedAvatar = await upload(drawerData.changedAvatar, true)
            drawerData.user.avatar = uploadedAvatar ?? drawerData.user.avatar
          }
          if (drawerData.user?.id) {
            await update(drawerData.user)
          } else {
            await save(drawerData.user)
          }
          drawerData.open = false
          tableEvent.query()
        } catch (error) {
          ElMessage.error((error as FailResult<undefined>).msg ?? JSON.stringify(error))
        } finally {
          tableQuery.loading = false
        }
      }
    })
  },
  resetDrawer: () => {
    userEditForm.value?.resetFields()
    drawerData.user = { ...defaultEmptyUser }
    drawerData.changedAvatar = null
  }
}

onMounted(() => {
  tableEvent.query()
})
</script>

<template>
  <el-card shadow="never" class="h-full !border-0">
    <el-row>
      <el-col :span="18">
        <el-form inline>
          <el-form-item>
            <el-select
              v-model="tableQuery.tenantId"
              :remote-method="tableEvent.queryTenantForSearch"
              :loading="tableQuery.tenantLoading"
              remote
              filterable
              clearable
              placeholder="所属租户"
            >
              <el-option
                v-for="item in tableQuery.tenantSelectOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-input v-model="tableQuery.username" clearable placeholder="用户名" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="tableQuery.enabled" clearable placeholder="是否启用" style="width: 100px">
              <el-option label="启用" value="true" />
              <el-option label="禁用" value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="tableEvent.query">
              <el-icon>
                <i-ep-search />
              </el-icon>
              <span>查询</span>
            </el-button>
          </el-form-item>
        </el-form>
      </el-col>
      <el-col :span="6" class="text-right">
        <el-button type="primary" @click="tableEvent.add">
          <el-icon>
            <i-ep-plus />
          </el-icon>
          <span>新增</span>
        </el-button>
      </el-col>
    </el-row>
    <el-row class="mt-4">
      <el-table v-loading="tableQuery.loading" :data="tableData.content" @sort-change="tableEvent.sort">
        <el-table-column prop="username" label="账户" width="152" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="phoneNumber" label="手机号" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" :formatter="row => (row.gender === 1 ? '男' : '女')" width="152" />
        <el-table-column
          prop="enabled"
          label="是否启用"
          :formatter="row => (row.enabled ? '启用' : '禁用')"
          width="152"
        />
        <el-table-column prop="tenant.name" label="所属租户" width="152" />
        <el-table-column prop="createdDate" label="创建时间" sortable="custom" width="152" />
        <el-table-column prop="lastModifiedDate" label="最后更新时间" width="152" />
        <el-table-column label="操作" fixed="right" width="152">
          <template #default="scope">
            <el-button size="small" @click="tableEvent.edit(scope.row.id)">编辑</el-button>
            <el-popconfirm title="确认删除吗？" @confirm="tableEvent.del(scope.row.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
              <template #actions="{ cancel, confirm }">
                <el-button text size="small" @click="cancel">取消</el-button>
                <el-button type="danger" size="small" @click="confirm">确认</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-row>
    <el-row class="mt-4">
      <el-col :span="24">
        <el-pagination
          :current-page="tableData.page.number + 1"
          :page-size="tableData.page.size"
          :total="tableData.page.totalElements"
          :page-sizes="[10, 20, 50, 100]"
          layout="-> ,total, prev, pager, next, sizes"
          @current-change="tableEvent.currentChange"
          @size-change="tableEvent.sizeChange"
        />
      </el-col>
    </el-row>
  </el-card>
  <el-drawer
    v-model="drawerData.open"
    :title="drawerData.user?.id ? '编辑用户' : '新增用户'"
    @close="drawerEvent.resetDrawer"
  >
    <template #default>
      <el-form ref="userEditForm" :model="drawerData.user" label-width="auto" :rules="drawerData.validRules">
        <el-form-item label="所属租户" prop="tenant.id">
          <el-select
            v-model="drawerData.user.tenant.id"
            :remote-method="drawerEvent.queryTenantForSearch"
            :loading="drawerData.tenantLoading"
            remote
            filterable
            clearable
          >
            <el-option
              v-for="item in drawerData.tenantSelectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="账户" prop="username">
          <el-input v-model="drawerData.user.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="drawerData.user.password">
            <template #append>
              <el-button-group>
                <el-button @click="drawerEvent.generateUserSecret">生成</el-button>
                <el-button @click="drawerEvent.clearUserSecret">清空</el-button>
              </el-button-group>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="drawerData.user.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="drawerData.user.phoneNumber" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="drawerData.user.nickname" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-switch
            v-model="drawerData.user.gender"
            :inactive-value="1"
            inactive-text="男"
            :active-value="2"
            active-text="女"
          />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            :on-change="drawerEvent.previewAvatarBeforeUpload"
            accept="image/jpeg,image/png"
            :class="{ 'border border-dashed border-(--el-border-color) rounded leading-0': !drawerData.user.avatar }"
          >
            <el-avatar
              shape="square"
              class="size-16"
              v-if="drawerData.user.avatar"
              scr
              :src="BASE_URL + drawerData.user.avatar"
            />
            <el-icon v-else class="size-16">
              <i-ep-plus />
            </el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="生日" prop="birthdate">
          <el-date-picker
            v-model="drawerData.user.birthdate"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="(date: Date) => date.getTime() > Date.now()"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="是否可用" prop="enabled">
          <el-switch v-model="drawerData.user.enabled" />
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="drawerData.open = false">取消</el-button>
        <el-button type="primary" @click="drawerEvent.submit">{{ drawerData.user?.id ? '更新' : '新增' }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>
