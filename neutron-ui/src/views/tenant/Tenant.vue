<script setup lang="ts">
import { ref } from 'vue'
import type Page from '@/api/Page.ts'
import { findTenantPage, saveTenant, type Tenant } from '@/api/Tenant.ts'

const resRef = ref<Page<Tenant>>()
const saveRef = ref()

const page = ref<number>(1)
const size = ref<number>(15)

function loadTenantPage() {
  findTenantPage(page.value, size.value)
    .then(data => (resRef.value = data))
    // eslint-disable-next-line no-alert
    .catch(err => alert(err))
}

const tenantRef = ref<Tenant>({ name: '', issuer: '' })

function save() {
  saveTenant(tenantRef.value)
    .then(data => (saveRef.value = data))
    // eslint-disable-next-line no-alert
    .catch(err => alert(err))
    .then(res => (saveRef.value = res))
    .then(() => loadTenantPage())
}

function prev() {
  page.value = page.value - 1
  //page.value = page.value < 1 ? 1 : page.value
  loadTenantPage()
}

function next() {
  page.value = page.value + 1
  page.value = page.value > resRef.value!.pages ? resRef.value!.pages : page.value
  loadTenantPage()
}
</script>

<template>
  <div>
    <button type="button" @click="loadTenantPage">LoadTenantPage</button>
  </div>
  <div>
    <p><label>name</label><input type="text" v-model="tenantRef.name" /></p>
    <p><label>issuer</label><input type="text" v-model="tenantRef.issuer" /></p>
    <button type="button" @click="save">Save</button>
    <p>{{ saveRef }}</p>
  </div>
  <table style="table-layout: fixed; width: 1500px; text-align: left">
    <thead>
      <tr>
        <th>id</th>
        <th>name</th>
        <th>issuer</th>
        <th>publicKey</th>
        <th>privateKey</th>
        <th>enabled</th>
        <th>createDate</th>
      </tr>
    </thead>
    <tbody v-if="resRef">
      <tr v-for="tenant in resRef.records" :key="tenant.id">
        <td>{{ tenant.id }}</td>
        <td>{{ tenant.name }}</td>
        <td>{{ tenant.issuer }}</td>
        <td style="width: 300px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis">
          {{ tenant.publicKey }}
        </td>
        <td style="width: 300px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis">
          {{ tenant.privateKey }}
        </td>
        <td>{{ tenant.enabled }}</td>
        <td>{{ tenant.createDate }}</td>
      </tr>
      <tr>
        <td colspan="5"></td>
        <td>第{{ resRef.current }}/{{ resRef.pages }}页</td>
        <td>共{{ resRef.total }}条</td>
      </tr>
      <tr>
        <td colspan="5"></td>
        <td>
          <button type="button" @click="prev">上一页</button>
        </td>
        <td>
          <button type="button" @click="next">下一页</button>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped></style>
