<script setup lang="ts">
import { computed, toRefs } from 'vue'
import { useRoute } from 'vue-router'

export type AsideProps = {
  collapse?: boolean
}

const props = withDefaults(defineProps<AsideProps>(), { collapse: false })
const { collapse } = toRefs(props)

const route = useRoute()
const active = computed(() => route.fullPath)
</script>

<template>
  <el-aside class="border-r border-(--el-border-color)">
    <el-scrollbar>
      <el-menu router :collapse="collapse" :default-active="active" class="!border-0">
        <el-menu-item index="/dashboard">
          <el-icon>
            <i-ep-platform />
          </el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-sub-menu index="setting">
          <template #title>
            <el-icon>
              <i-ep-tools />
            </el-icon>
            <span>设置</span>
          </template>
          <el-menu-item index="/setting/tenant">租户</el-menu-item>
          <el-menu-item index="/setting/client">客户端</el-menu-item>
          <el-menu-item index="/setting/user">用户</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
  </el-aside>
</template>
