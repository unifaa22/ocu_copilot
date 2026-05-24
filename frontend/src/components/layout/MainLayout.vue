<script setup>
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col bg-morandi-sand transition-colors duration-200 dark:bg-morandi-dark-bg">
    <AppHeader />
    <div class="flex flex-1 overflow-hidden">
      <AppSidebar />
      <main class="flex-1 overflow-auto p-4">
        <RouterView />
      </main>
    </div>
  </div>
</template>
