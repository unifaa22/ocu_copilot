<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { teamApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useLayoutStore } from '@/stores/layout'

const route = useRoute()
const authStore = useAuthStore()
const layoutStore = useLayoutStore()
const teamSummary = ref(null)

const collapsed = computed(() => layoutStore.sidebarCollapsed)

const navItems = [
  { name: 'home', path: '/home', label: '首页', icon: 'fa-house' },
  { name: 'knowledge', path: '/knowledge', label: '知识库', icon: 'fa-book' },
  { name: 'chat-personal', path: '/chat/personal', label: '个人问答', icon: 'fa-comments' },
  { name: 'chat-team', path: '/chat/team', label: '团队问答', icon: 'fa-users-rays' },
  { name: 'notes', path: '/notes', label: '学习笔记', icon: 'fa-note-sticky' },
  { name: 'team', path: '/team', label: '团队管理', icon: 'fa-people-group' },
  { name: 'profile', path: '/profile', label: '个人中心', icon: 'fa-user-gear' },
]

const shareLabel = computed(() => {
  if (!teamSummary.value) return '暂无团队'
  return teamSummary.value.isShare === 1 ? '共享已开启' : '共享未开启'
})

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

async function loadTeamWidget() {
  try {
    const data = await teamApi.joined({ page: 1, size: 5 })
    const list = data?.list || []
    teamSummary.value = list.find((t) => t.isShare === 1) || list[0] || null
  } catch {
    teamSummary.value = null
  }
}

onMounted(loadTeamWidget)
</script>

<template>
  <aside
    class="relative flex shrink-0 flex-col border-r border-morandi-peach/20 bg-white transition-[width] duration-300 ease-in-out dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
    :class="collapsed ? 'w-[4.25rem]' : 'w-48'"
    :aria-expanded="!collapsed"
  >
    <div class="flex items-center border-b border-morandi-peach/15 px-2 py-2" :class="collapsed ? 'justify-center' : 'justify-end'">
      <button
        type="button"
        class="flex h-8 w-8 cursor-pointer items-center justify-center rounded-lg text-neutral-500 transition-colors duration-200 hover:bg-morandi-sand hover:text-morandi-clay dark:hover:bg-morandi-dark-bg dark:hover:text-morandi-peach"
        :aria-label="collapsed ? '展开侧边栏' : '收起侧边栏'"
        :title="collapsed ? '展开侧边栏' : '收起侧边栏'"
        @click="layoutStore.toggleSidebar()"
      >
        <i
          class="fa-solid text-sm transition-transform duration-300"
          :class="collapsed ? 'fa-angles-right' : 'fa-angles-left'"
          aria-hidden="true"
        />
      </button>
    </div>

    <nav class="flex-1 space-y-0.5 p-2" aria-label="主导航">
      <RouterLink
        v-for="item in navItems"
        :key="item.name"
        :to="item.path"
        class="flex cursor-pointer items-center rounded-lg py-2 text-sm font-medium transition-colors duration-200"
        :class="[
          collapsed ? 'justify-center px-0' : 'gap-2.5 px-2.5',
          isActive(item.path)
            ? 'bg-morandi-clay/15 text-morandi-clay dark:bg-morandi-clay/25 dark:text-morandi-peach'
            : 'text-neutral-600 hover:bg-morandi-sand dark:text-neutral-300 dark:hover:bg-morandi-dark-bg',
        ]"
        :aria-current="isActive(item.path) ? 'page' : undefined"
        :title="collapsed ? item.label : undefined"
      >
        <i class="fa-solid w-4 shrink-0 text-center text-[13px]" :class="item.icon" aria-hidden="true" />
        <span
          class="overflow-hidden whitespace-nowrap transition-[opacity,width] duration-300"
          :class="collapsed ? 'w-0 opacity-0' : 'w-auto opacity-100'"
        >
          {{ item.label }}
        </span>
      </RouterLink>
    </nav>

    <div class="border-t border-morandi-peach/15 p-2">
      <div
        v-if="!collapsed"
        class="rounded-lg border border-morandi-peach/20 bg-morandi-sand/50 p-3 dark:border-morandi-peach/10 dark:bg-morandi-dark-bg"
      >
        <div class="mb-2 flex items-center gap-2 text-xs font-semibold text-morandi-clay">
          <i class="fa-solid fa-share-nodes" aria-hidden="true" />
          <span>团队共享</span>
        </div>
        <template v-if="teamSummary">
          <p class="truncate text-sm font-medium text-morandi-charcoal dark:text-morandi-dark-text">
            {{ teamSummary.teamName }}
          </p>
          <p class="mt-1 text-xs text-neutral-500">{{ shareLabel }}</p>
        </template>
        <p v-else class="text-xs text-neutral-500">加入团队后可使用团队问答</p>
        <RouterLink
          to="/team"
          class="mt-2 inline-flex cursor-pointer items-center gap-1 text-xs text-morandi-clay transition-colors duration-200 hover:text-morandi-clay-hover"
        >
          <span>管理团队</span>
          <i class="fa-solid fa-arrow-right text-[10px]" aria-hidden="true" />
        </RouterLink>
      </div>

      <RouterLink
        v-else
        to="/team"
        class="mx-auto flex h-9 w-9 cursor-pointer items-center justify-center rounded-lg text-morandi-clay transition-colors duration-200 hover:bg-morandi-sand dark:hover:bg-morandi-dark-bg"
        title="团队共享"
      >
        <i class="fa-solid fa-share-nodes" aria-hidden="true" />
      </RouterLink>

      <p v-if="!collapsed" class="mt-2 truncate text-center text-[10px] text-neutral-400">
        {{ authStore.user?.username }}
      </p>
    </div>
  </aside>
</template>
