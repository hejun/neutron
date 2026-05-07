<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { sendMsg } from '@/api/notify/Msg.ts'

const send = () =>
  sendMsg('*', 'Hello, World!')
    .then()
    .catch(error => ElMessage.error(error))
</script>

<template>
  <div>
    <p>Home</p>
    <el-button type="primary" @click="send">发送消息</el-button>
  </div>
</template>
