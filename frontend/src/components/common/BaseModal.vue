<script setup>
defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: 'max-w-md' },
})
const emit = defineEmits(['close'])
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/40" @click="emit('close')" />
        <div
          class="relative w-full rounded-xl border border-morandi-peach/20 bg-white shadow-2xl dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
          :class="width"
        >
          <div v-if="title" class="flex items-center justify-between border-b border-morandi-peach/10 px-6 py-4">
            <h3 class="font-bold text-morandi-charcoal dark:text-morandi-dark-text">{{ title }}</h3>
            <button class="text-neutral-400 hover:text-morandi-clay" @click="emit('close')">
              <i class="fa-solid fa-xmark" />
            </button>
          </div>
          <slot />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
