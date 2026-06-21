<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { TableSortOrder } from 'element-plus/es/components/table/src/table/defaults'
import { ElMessage, type FormInstance, type FormRules, type TableColumnCtx } from 'element-plus'
import { findPage as findTenantPage } from '@/api/auth/Tenant.ts'
import { type ClientDetail, type ClientList, del, findById, findPage, save, update } from '@/api/auth/Client.ts'

const tableQuery = reactive({
  loading: false,
  tenantLoading: false,
  clientId: '',
  clientName: '',
  enabled: '',
  tenantId: '',
  tenantSelectOptions: [] as { value: string; label: string }[],
  page: 0,
  size: 10,
  sort: ''
})

const tableData = reactive<Page<ClientList>>({
  page: { number: 0, size: 10, totalElements: 0, totalPages: 0 },
  content: []
})

const defaultSupportData = {
  clientAuthenticationMethods: [
    'none',
    'client_secret_basic',
    'client_secret_post',
    'client_secret_jwt',
    'private_key_jwt',
    'tls_client_auth',
    'self_signed_tls_client_auth'
  ],
  authorizationGrantTypes: [
    'authorization_code',
    'refresh_token',
    'client_credentials',
    'password',
    'urn:ietf:params:oauth:grant-type:jwt-bearer',
    'urn:ietf:params:oauth:grant-type:device_code',
    'urn:ietf:params:oauth:grant-type:token-exchange'
  ],
  scopes: ['openid', 'profile', 'email', 'address', 'phone']
}
const defaultEmptyClient: ClientDetail = {
  id: '',
  clientId: '',
  clientSecret: '',
  clientName: '',
  clientAuthenticationMethods: [],
  authorizationGrantTypes: [],
  redirectUris: [],
  postLogoutRedirectUris: [],
  scopes: [],
  requireProofKey: true,
  requireAuthorizationConsent: true,
  accessTokenTimeToLive: undefined,
  refreshTokenTimeToLive: undefined,
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
      tableQuery.clientId,
      tableQuery.clientName,
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
    drawerData.client = { ...defaultEmptyClient }
    drawerData.tenantSelectOptions = []
    drawerData.open = true
  },
  edit: (id: string) => {
    tableQuery.loading = true
    drawerData.client = { ...defaultEmptyClient }
    findById(id)
      .then(client => {
        if (client) {
          drawerData.client = client
          drawerData.tenantSelectOptions = [{ value: client.tenant.id!, label: client.tenant.name }]
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
  sort: (data: { column: TableColumnCtx<ClientList>; prop: string | null; order: TableSortOrder | null }) => {
    tableQuery.sort = data.order ? `${data.prop},${data.order === 'descending' ? 'desc' : 'asc'}` : ''
    tableEvent.query()
  }
}

const clientEditForm = ref<FormInstance>()
const drawerData = reactive({
  open: false,
  tenantLoading: false,
  tenantSelectOptions: [] as { value: string; label: string }[],
  client: { ...defaultEmptyClient },
  validRules: {
    'tenant.id': [
      {
        required: true,
        message: '请选择所属租户',
        trigger: 'blur'
      }
    ],
    clientId: [
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
    name: [
      {
        required: true,
        message: '请输入客户端名称',
        trigger: 'blur'
      },
      {
        min: 2,
        max: 50,
        message: '客户端名称应在2-50个字之间',
        trigger: 'blur'
      }
    ],
    redirectUris: [
      {
        validator: (rule, value: string[] | undefined, callback) => {
          if (!value || value.length <= 0) {
            callback()
            return
          }
          const valid = value?.map(item => /^(https?):\/\/[^\s?#]+(?::\d+)?.+$/.test(item)).reduce((a, b) => a && b)
          if (valid) {
            callback()
          } else {
            callback(new Error('每个回调地址均应为 http:// 或 https:// 开头的标准IP或域名'))
          }
        },
        trigger: 'blur'
      }
    ],
    postLogoutRedirectUris: [
      {
        validator: (rule, value: string[] | undefined, callback) => {
          if (!value || value.length <= 0) {
            callback()
            return
          }
          const valid = value?.map(item => /^(https?):\/\/[^\s?#]+(?::\d+)?.+$/.test(item)).reduce((a, b) => a && b)
          if (valid) {
            callback()
          } else {
            callback(new Error('每个跳转地址均应为 http:// 或 https:// 开头的标准IP或域名'))
          }
        },
        trigger: 'blur'
      }
    ],
    enabled: [
      {
        required: true,
        trigger: 'blur'
      }
    ],
    accessTokenTimeToLive: [
      {
        type: 'number',
        min: 0,
        message: 'AccessToken存活时间不可小于0',
        trigger: 'blur'
      }
    ],
    refreshTokenTimeToLive: [
      {
        type: 'number',
        min: 0,
        message: 'RefreshToken存活时间不可小于0',
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
  generateClientSecret: () => (drawerData.client.clientSecret = Math.random().toString(36).slice(2)),
  clearClientSecret: () => (drawerData.client.clientSecret = undefined),
  submit: async () => {
    if (!clientEditForm.value) return
    await clientEditForm.value.validate(valid => {
      if (valid) {
        tableQuery.loading = true
        const fun = drawerData.client?.id ? update : save
        fun(drawerData.client)
          .then(() => {
            drawerData.open = false
            tableEvent.query()
          })
          .catch(error => {
            ElMessage.error(error.msg ?? JSON.stringify(error))
          })
          .finally(() => {
            tableQuery.loading = false
          })
      }
    })
  },
  resetDrawer: () => {
    clientEditForm.value?.resetFields()
    drawerData.client = { ...defaultEmptyClient }
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
            <el-input v-model="tableQuery.clientId" clearable placeholder="客户端ID" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="tableQuery.clientName" clearable placeholder="客户端名" />
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
        <el-table-column prop="clientId" label="ID" width="152" />
        <el-table-column prop="clientName" label="名称" width="240" />
        <el-table-column
          prop="authorizationGrantTypes"
          label="授权方法"
          :formatter="row => row.authorizationGrantTypes.join(',')"
          show-overflow-tooltip
        />
        <el-table-column prop="tenant.name" label="所属租户" width="152" />
        <el-table-column
          prop="enabled"
          label="是否启用"
          :formatter="row => (row.enabled ? '启用' : '禁用')"
          width="152"
        />
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
    :title="drawerData.client?.id ? '编辑客户端' : '新增客户端'"
    @close="drawerEvent.resetDrawer"
  >
    <template #default>
      <el-form ref="clientEditForm" :model="drawerData.client" label-width="auto" :rules="drawerData.validRules">
        <el-form-item label="所属租户" prop="tenant.id">
          <el-select
            v-model="drawerData.client.tenant.id"
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
        <el-form-item label="客户端ID" prop="clientId">
          <el-input v-model="drawerData.client.clientId" />
        </el-form-item>
        <el-form-item label="客户端密钥" prop="clientSecret">
          <el-input v-model="drawerData.client.clientSecret" readonly>
            <template #append>
              <el-button-group>
                <el-button @click="drawerEvent.generateClientSecret">生成</el-button>
                <el-button @click="drawerEvent.clearClientSecret">清空</el-button>
              </el-button-group>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="客户端名称" prop="clientName">
          <el-input v-model="drawerData.client.clientName" />
        </el-form-item>
        <el-form-item label="认证方式" prop="clientAuthenticationMethods">
          <el-select v-model="drawerData.client.clientAuthenticationMethods" multiple>
            <el-option v-for="item in defaultSupportData.clientAuthenticationMethods" :key="item" :value="item"
              >{{ item }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="授权方式" prop="authorizationGrantTypes">
          <el-select v-model="drawerData.client.authorizationGrantTypes" multiple>
            <el-option v-for="item in defaultSupportData.authorizationGrantTypes" :key="item" :value="item"
              >{{ item }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="回调地址" prop="redirectUris">
          <el-input-tag v-model="drawerData.client.redirectUris" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="登出后跳转地址" prop="postLogoutRedirectUris">
          <el-input-tag v-model="drawerData.client.postLogoutRedirectUris" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="授权范围" prop="scopes">
          <el-select v-model="drawerData.client.scopes" multiple>
            <el-option v-for="item in defaultSupportData.scopes" :key="item" :value="item">{{ item }}</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="启用PKCE" prop="requireProofKey">
          <el-switch v-model="drawerData.client.requireProofKey" />
        </el-form-item>
        <el-form-item label="需要授权确认" prop="requireAuthorizationConsent">
          <el-switch v-model="drawerData.client.requireAuthorizationConsent" />
        </el-form-item>
        <el-form-item label="访问令牌存活时间" prop="accessTokenTimeToLive">
          <el-input type="number" v-model.number="drawerData.client.accessTokenTimeToLive" placeholder="请输入">
            <template #append>秒</template>
          </el-input>
        </el-form-item>
        <el-form-item label="刷新令牌存活时间" prop="refreshTokenTimeToLive">
          <el-input type="number" v-model.number="drawerData.client.refreshTokenTimeToLive" placeholder="请输入">
            <template #append>秒</template>
          </el-input>
        </el-form-item>
        <el-form-item label="是否可用" prop="enabled">
          <el-switch v-model="drawerData.client.enabled" />
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="drawerData.open = false">取消</el-button>
        <el-button type="primary" @click="drawerEvent.submit">{{ drawerData.client?.id ? '更新' : '新增' }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>
