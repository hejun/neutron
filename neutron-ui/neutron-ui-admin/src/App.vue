<script setup lang="ts">
import { reactive } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { userinfo } from '@/api/auth/Auth.ts'

const data = reactive({
  loading: true,
  logo: '',
  user: ''
})

userinfo().then(res => {
  data.user = res?.sub || ''
  data.logo = res?.aud_logo || ''
  data.loading = false
})
</script>

<template>
  <el-config-provider :locale="zhCn">
    <el-container direction="vertical" class="h-full" v-loading.fullscreen.lock="data.loading">
      <Header :logo="data.logo" :user="data.user" />
      <el-container direction="horizontal" style="height: calc(100% - 60px)">
        <Aside />
        <el-main>
          <!--          <router-view />-->
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>
