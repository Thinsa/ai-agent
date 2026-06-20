<template>
  <div class="cosmic-bg" aria-hidden="true">
    <!-- 星云层 -->
    <template v-if="showNebula">
      <div class="nebula n1"></div>
      <div class="nebula n2"></div>
      <div class="nebula n3"></div>
      <div class="nebula n4"></div>
    </template>

    <!-- 星座光点与连线 -->
    <template v-if="showConstellations">
      <div class="constellation">
        <i v-for="n in starCount" :key="n" class="star-dot" :style="starStyle(n)"></i>
        <svg class="constellation-lines" viewBox="0 0 1440 900" preserveAspectRatio="none">
          <line x1="12%" y1="18%" x2="28%" y2="35%" />
          <line x1="28%" y1="35%" x2="45%" y2="22%" />
          <line x1="68%" y1="15%" x2="82%" y2="40%" />
          <line x1="82%" y1="40%" x2="75%" y2="60%" />
          <line x1="20%" y1="65%" x2="35%" y2="78%" />
          <line x1="55%" y1="80%" x2="70%" y2="68%" />
        </svg>
      </div>
    </template>

    <!-- 覆盖层 -->
    <div v-if="overlay" class="cosmic-overlay"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  density: {
    type: [Number, String],
    default: 35
  },
  showNebula: {
    type: Boolean,
    default: true
  },
  showConstellations: {
    type: Boolean,
    default: true
  },
  overlay: {
    type: Boolean,
    default: false
  }
})

const starCount = computed(() => {
  const d = Number(props.density)
  return Number.isFinite(d) ? Math.max(1, Math.round(d)) : 35
})

const starStyle = (n) => {
  const angle = (n / starCount.value) * 360
  const radius = 15 + Math.random() * 50
  const x = 50 + Math.cos(angle * Math.PI / 180) * radius
  const y = 50 + Math.sin(angle * Math.PI / 180) * radius
  const size = 1 + Math.random() * 2.5
  const delay = Math.random() * 12
  const duration = 3 + Math.random() * 7
  return {
    left: `${x}%`,
    top: `${y}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    opacity: 0.15 + Math.random() * 0.6
  }
}
</script>

<style scoped>
.cosmic-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

/* ═══════ 星云 ═══════ */
.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(150px);
  animation: nebula-drift 30s var(--ease-in-out) infinite;
}
.nebula.n1 {
  width: 700px; height: 700px;
  background: radial-gradient(circle, rgba(180,160,232,0.22), transparent 70%);
  top: -10%; left: -8%;
  animation-delay: 0s;
}
.nebula.n2 {
  width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(126,200,224,0.15), transparent 70%);
  bottom: -15%; left: 30%;
  animation-delay: -10s;
}
.nebula.n3 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(240,192,96,0.10), transparent 70%);
  top: 40%; right: -5%;
  animation-delay: -20s;
}
.nebula.n4 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(240,144,160,0.08), transparent 70%);
  top: 60%; left: 10%;
  animation-delay: -5s;
}

@keyframes nebula-drift {
  0%, 100% { transform: translate(0,0) scale(1); }
  33% { transform: translate(20px,-20px) scale(1.05); }
  66% { transform: translate(-15px,15px) scale(0.95); }
}

/* ═══════ 星座光点 ═══════ */
.constellation {
  position: fixed;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}
.star-dot {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  animation: star-twinkle 5s var(--ease-in-out) infinite;
}
@keyframes star-twinkle {
  0%, 100% { opacity: 0.1; transform: scale(0.5); }
  50% { opacity: 0.8; transform: scale(1.3); }
}
.constellation-lines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.constellation-lines line {
  stroke: rgba(255,255,255,0.04);
  stroke-width: 0.5;
}

/* ═══════ 覆盖层 ═══════ */
.cosmic-overlay {
  position: fixed;
  inset: 0;
  z-index: 2;
  background: radial-gradient(ellipse at 50% 40%, rgba(13,17,23,0.3), rgba(8,12,22,0.6));
  pointer-events: none;
}
</style>
