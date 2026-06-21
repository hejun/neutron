<script setup lang="ts">
import { userinfo } from '@/api/auth/Auth.ts'
import { reactive } from 'vue'

const data = reactive({
  user: ''
})

const sayHello = () => {
  const time = new Date()
  const hour = time.getHours()
  return hour < 9 ? '早上好' : hour <= 11 ? '上午好' : hour <= 13 ? '中午好' : hour <= 18 ? '下午好' : '晚上好'
}

userinfo().then(res => {
  data.user = res?.nickname || res?.name || ''
})
</script>

<template>
  <div v-if="data.user">
    <el-card shadow="never" class="!border-0">
      <p class="text-lg font-bold">{{ sayHello() + ', ' + data.user }}</p>
      <p class="color-light">欢迎回来</p>
    </el-card>
  </div>
</template>
