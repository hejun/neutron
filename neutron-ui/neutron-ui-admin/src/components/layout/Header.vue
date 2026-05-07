<script setup lang="ts">
import { toRefs, watchEffect } from 'vue'
import useSystemStore from '@/store/SystemStore.ts'
import { useDark } from '@vueuse/core'
import { logout } from '@/api/auth/Auth.ts'

export type HeaderProps = {
  name?: string
  user?: string
  avatar?: string
}

const props = withDefaults(defineProps<HeaderProps>(), {})
const { avatar } = toRefs(props)

const systemStore = useSystemStore()
const darkMode = useDark()
watchEffect(() => {
  darkMode.value = systemStore.theme !== 'light'
})
</script>

<template>
  <el-header class="border-b border-(--el-border-color)" style="--el-header-padding: 0;">
    <el-menu mode="horizontal" :ellipsis="false" class="w-full header-menu">
      <el-menu-item index="client" class="text-2xl">{{ name }}</el-menu-item>
      <el-menu-item index="theme" @click="systemStore.toggleTheme">
        <el-icon>
          <i-ep-sunny v-if="darkMode" />
          <i-ep-moon-night v-else />
        </el-icon>
      </el-menu-item>
      <el-sub-menu index="profile">
        <template #title>
          <el-avatar shape="square" :size="30" :src="avatar">
            <i-ep-user />
          </el-avatar>
          <span class="ml-2">{{ user }}</span>
        </template>
        <el-menu-item index="account">
          <el-icon>
            <i-ep-user />
          </el-icon>
          <span>我的</span>
        </el-menu-item>
        <el-menu-item index="logout" class="border-t border-(--el-border-color)" @click="logout">
          <el-icon>
            <i-ep-switch-button />
          </el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
  </el-header>
</template>

<style scoped lang="scss">
.header-menu {
  & > .el-menu-item {
    border-bottom: none;

    &:first-child {
      --el-menu-active-color: var(--el-menu-text-color);
      --el-menu-hover-bg-color: transparent;
    }

    &:nth-child(1) {
      margin-right: auto;
    }

    &.is-active {
      --el-menu-bg-color: var(--el-menu-text-color);
      --el-menu-hover-bg-color: transparent;
      --el-menu-active-color: var(--el-menu-text-color);
    }
  }

  & > :deep(.el-sub-menu).is-active {
    .el-sub-menu__title {
      border-bottom: none !important;
      color: var(--el-menu-text-color);
    }
  }
}
</style>
