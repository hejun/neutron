<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { TableSortOrder } from 'element-plus/es/components/table/src/table/defaults'
import { ElMessage, type FormInstance, type FormRules, type TableColumnCtx } from 'element-plus'
import { findById, findPage, save, update, del, type TenantDetail, type TenantList } from '@/api/auth/Tenant.ts'

const tableQuery = reactive({
  loading: false,
  name: '',
  enabled: undefined,
  page: 0,
  size: 10,
  sort: ''
})

const tableData = reactive<Page<TenantList>>({
  page: { number: 0, size: 10, totalElements: 0, totalPages: 0 },
  content: []
})

const defaultEmptyTenant: TenantDetail = {
  id: '',
  name: '',
  issuer: '',
  copyright: '',
  enabled: true,
  createdDate: '',
  lastModifiedDate: ''
}

const tableEvent = {
  query: () => {
    tableQuery.loading = true
    findPage(tableQuery.name, tableQuery.enabled, tableQuery.page, tableQuery.size, tableQuery.sort)
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
    drawerData.tenant = { ...defaultEmptyTenant }
    drawerData.open = true
  },
  edit: (id: string) => {
    tableQuery.loading = true
    drawerData.tenant = { ...defaultEmptyTenant }
    findById(id)
      .then(tenant => {
        drawerData.open = true
        drawerData.tenant = tenant!
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
  sort: (data: { column: TableColumnCtx<TenantList>; prop: string | null; order: TableSortOrder | null }) => {
    tableQuery.sort = data.order ? `${data.prop},${data.order === 'descending' ? 'desc' : 'asc'}` : ''
    tableEvent.query()
  }
}

const tenantEditForm = ref<FormInstance>()
const drawerData = reactive({
  open: false,
  tenant: { ...defaultEmptyTenant },
  validRules: {
    name: [
      {
        required: true,
        message: '请输入租户名',
        trigger: 'blur'
      },
      {
        min: 2,
        max: 50,
        message: '名称应在2-50个字之间',
        trigger: 'blur'
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
        message: 'Issuer应在2-100个字之间',
        trigger: 'blur'
      },
      {
        validator: (rule, value, callback) =>
          /^(https?):\/\/[^\s?#]+(?::\d+)?(?:\/[^?#]*)?$/.test(value)
            ? callback()
            : callback(new Error('Issuer应为 http:// 或 https:// 开头的标准IP或域名, 且不可有参数')),
        trigger: 'blur'
      }
    ],
    copyright: [
      {
        max: 100,
        message: '描述应在100个字以内',
        trigger: 'blur'
      }
    ],
    enabled: [
      {
        required: true,
        trigger: 'blur'
      }
    ]
  } as FormRules<{ name: string; issuer: string; copyright: string }>
})

const drawerEvent = {
  submit: async () => {
    if (!tenantEditForm.value) return
    await tenantEditForm.value.validate(valid => {
      if (valid) {
        tableQuery.loading = true
        const fun = drawerData.tenant?.id ? update : save
        fun(drawerData.tenant)
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
    tenantEditForm.value?.resetFields()
    drawerData.tenant = { ...defaultEmptyTenant }
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
            <el-input v-model="tableQuery.name" clearable placeholder="租户名" />
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
        <el-table-column prop="name" label="名称" width="240" />
        <el-table-column prop="issuer" label="发行域名" show-overflow-tooltip />
        <el-table-column
          prop="enabled"
          label="是否启用"
          :formatter="row => (row.enabled ? '启用' : '禁用')"
          width="150"
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
    :title="drawerData.tenant?.id ? '编辑租户' : '新增租户'"
    @close="drawerEvent.resetDrawer"
  >
    <template #default>
      <el-form ref="tenantEditForm" :model="drawerData.tenant" label-width="auto" :rules="drawerData.validRules">
        <el-form-item label="名称" prop="name">
          <el-input v-model="drawerData.tenant.name" />
        </el-form-item>
        <el-form-item label="发行域名" prop="issuer">
          <el-input v-model="drawerData.tenant.issuer" />
        </el-form-item>
        <el-form-item label="版权" prop="copyright">
          <el-input v-model="drawerData.tenant.copyright" />
        </el-form-item>
        <el-form-item label="是否可用" prop="enabled">
          <el-switch v-model="drawerData.tenant.enabled" />
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <div style="flex: auto">
        <el-button @click="drawerData.open = false">取消</el-button>
        <el-button type="primary" @click="drawerEvent.submit">{{ drawerData.tenant?.id ? '更新' : '新增' }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>
