<script setup lang="ts">
import { reactive } from 'vue'
import { ElNotification } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { userinfo } from '@/api/auth/Auth'
import { connect } from '@/api/notify/Msg'

const BASE_URL = import.meta.env.VITE_BASE_URL || ''

const data = reactive({
  loading: true,
  name: '',
  user: '',
  avatar: ''
})

userinfo()
  .then(res => {
    data.loading = false
    data.name = res?.aud_name || ''
    data.user = res?.nickname || res?.name || ''
    data.avatar = res?.picture ? `${BASE_URL}${res.picture}` : ''
  })
  .then(() =>
    connect().then(es => {
      es.onmessage = msg =>
        ElNotification.success({
          title: '通知',
          message: JSON.parse(msg.data).data
        })
    })
  )
</script>

<template>
  <el-config-provider :locale="zhCn">
    <el-container direction="vertical" class="h-full" v-loading.fullscreen.lock="data.loading">
      <Header v-if="!data.loading" :name="data.name" :user="data.user" :avatar="data.avatar" />
      <el-container v-if="!data.loading" direction="horizontal" style="height: calc(100% - 60px)">
        <Aside />
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<style scoped lang="scss">
.main {
  background-color: #f5f7fd;
}

html.dark .main {
  background-color: #373739;
}
</style>
