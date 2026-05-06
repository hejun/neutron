<script setup lang="ts">
import { toRefs, watchEffect } from 'vue'
import useSystemStore from '@/store/SystemStore.ts'
import { useDark } from '@vueuse/core'
import { logout } from '@/api/auth/Auth'

export type HeaderProps = {
  logo?: string
  user: string
}

const props = withDefaults(defineProps<HeaderProps>(), {})
const { logo } = toRefs(props)

const systemStore = useSystemStore()
const darkMode = useDark()
watchEffect(() => {
  darkMode.value = systemStore.theme !== 'light'
})
</script>

<template>
  <el-header class="flex justify-between items-center border-b border-(--el-border-color)">
    <div class="flex items-center justify-between">
      <el-avatar shape="square" :src="logo" :class="{ 'bg-transparent': !!logo }">
        <i-ep-shop />
      </el-avatar>
      <label class="ml-2 text-lg font-semibold">{{ systemStore.client?.clientName }}</label>
    </div>
    <div>
      <el-button link @click="systemStore.toggleTheme">
        <el-icon class="text-lg">
          <i-ep-sunny v-if="darkMode" />
          <i-ep-moon-night v-else />
        </el-icon>
      </el-button>
      <el-dropdown class="ml-4">
        <span class="text-lg outline-none cursor-pointer">
          <el-icon>
            <i-ep-user />
          </el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>
              <template #icon>
                <i-ep-house />
              </template>
              <span>我的</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click="logout">
              <template #icon>
                <i-ep-switch-button />
              </template>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>
