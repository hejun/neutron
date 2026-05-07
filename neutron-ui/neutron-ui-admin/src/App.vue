<script setup lang="ts">
import { reactive } from 'vue'
import { ElNotification } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { userinfo } from '@/api/auth/Auth'
import { connect } from '@/api/notify/Msg'

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
    data.avatar = res?.picture || ''
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
      <Header :name="data.name" :user="data.user" :avatar="data.avatar" />
      <el-container direction="horizontal" style="height: calc(100% - 60px)">
        <Aside />
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>
