<script setup>
</script>

<template>
  <router-view />
</template>

<style>
@import './styles/tokens.css';

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html, body {
  font-family: var(--font-body);
  font-size: 16px;
  color: var(--color-text-1);
  background: var(--color-base-0);
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  height: 100%;
}

a {
  text-decoration: none;
  color: inherit;
}

button {
  cursor: pointer;
}

/* 响应式字体大小 */
@media (max-width: 768px) {
  html, body {
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  html, body {
    font-size: 14px;
  }
}

/* 滚动条 - dark theme */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--color-base-1);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--color-base-4);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-3);
}
</style>
