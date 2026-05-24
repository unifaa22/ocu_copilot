<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: String, required: true },
  open: { type: Boolean, default: false },
  options: { type: Array, required: true },
  ariaLabel: { type: String, default: '菜单' },
  leadingIcon: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'update:open'])

const current = computed(() => props.options.find((o) => o.value === props.modelValue))

function toggle() {
  emit('update:open', !props.open)
}

function select(value) {
  emit('update:modelValue', value)
  emit('update:open', false)
}
</script>

<template>
  <div class="relative">
    <button
      type="button"
      class="flex h-8 cursor-pointer items-center gap-1.5 rounded-full border border-neutral-200/90 bg-neutral-50/90 px-3 text-xs font-medium text-neutral-700 transition-colors duration-200 hover:border-morandi-clay/35 hover:bg-white dark:border-morandi-peach/15 dark:bg-morandi-dark-bg dark:text-neutral-200 dark:hover:border-morandi-clay/40"
      :aria-expanded="props.open"
      :aria-haspopup="true"
      :aria-label="ariaLabel"
      @click.stop="toggle"
    >
      <i
        v-if="leadingIcon || current?.icon"
        class="fa-solid text-[11px] text-morandi-clay dark:text-morandi-peach"
        :class="leadingIcon || current?.icon"
        aria-hidden="true"
      />
      <span>{{ current?.label }}</span>
      <i
        class="fa-solid fa-chevron-down text-[9px] text-neutral-400 transition-transform duration-200"
        :class="props.open ? 'rotate-180' : ''"
        aria-hidden="true"
      />
    </button>

    <Transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="scale-95 opacity-0"
      enter-to-class="scale-100 opacity-100"
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="scale-100 opacity-100"
      leave-to-class="scale-95 opacity-0"
    >
      <ul
        v-if="props.open"
        class="absolute top-[calc(100%+6px)] left-0 z-30 min-w-[9.5rem] overflow-hidden rounded-xl border border-neutral-200/90 bg-white py-1 shadow-lg dark:border-morandi-peach/15 dark:bg-morandi-dark-card"
        role="listbox"
        :aria-label="ariaLabel"
      >
        <li v-for="opt in options" :key="opt.value" role="option" :aria-selected="opt.value === modelValue">
          <button
            type="button"
            class="flex w-full cursor-pointer items-center gap-2 px-3 py-2 text-left text-xs transition-colors duration-150"
            :class="
              opt.value === modelValue
                ? 'bg-morandi-clay/10 font-medium text-morandi-clay dark:bg-morandi-clay/20 dark:text-morandi-peach'
                : 'text-neutral-700 hover:bg-neutral-50 dark:text-neutral-200 dark:hover:bg-morandi-dark-bg'
            "
            @click.stop="select(opt.value)"
          >
            <i v-if="opt.icon" class="fa-solid w-3.5 text-center text-[10px]" :class="opt.icon" aria-hidden="true" />
            <span>{{ opt.label }}</span>
          </button>
        </li>
      </ul>
    </Transition>
  </div>
</template>
