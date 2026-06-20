<template>
  <div v-if="visible" class="km-overlay" @click.self="$emit('close')">
    <div class="km-dialog">
      <!-- 标题栏 -->
      <div class="km-header">
        <div class="km-header-left">
          <h2 class="km-title">知识库文档管理</h2>
          <span class="km-count">已启用 {{ enabledCount }} / {{ totalCount }}</span>
        </div>
        <div class="km-header-actions">
          <button class="km-btn km-btn-secondary" :disabled="reindexing" @click="handleReindex">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" class="km-btn-icon"><path d="M1 8a7 7 0 0 1 13.5-3M15 8a7 7 0 0 1-13.5 3"/><path d="M14.5 1v4h-4M1.5 15v-4h4"/></svg>
            {{ reindexing ? '索引中...' : '重新索引' }}
          </button>
          <button class="km-btn km-btn-close" @click="$emit('close')" aria-label="关闭">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 4l8 8M12 4l-8 8"/></svg>
          </button>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="!showForm" class="km-body">
        <div class="km-toolbar">
          <div class="km-filter-row">
            <input v-model="searchQuery" class="km-search" placeholder="搜索文档标题..." />
            <select v-model="filterCategory" class="km-select">
              <option value="">全部分类</option>
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
            <button class="km-btn km-btn-primary" @click="startCreate">
              <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" class="km-btn-icon"><path d="M8 3v10M3 8h10"/></svg>
              添加文档
            </button>
          </div>
        </div>

        <div v-if="loading" class="km-state">加载中...</div>
        <div v-else-if="filteredDocuments.length === 0" class="km-state">暂无文档</div>
        <div v-else class="km-table-wrap">
          <table class="km-table">
            <thead>
              <tr>
                <th class="km-col-title">标题</th>
                <th class="km-col-cat">分类</th>
                <th class="km-col-source">来源</th>
                <th class="km-col-status">状态</th>
                <th class="km-col-actions">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="doc in filteredDocuments" :key="doc.id">
                <td class="km-cell-title">{{ doc.title }}</td>
                <td><span class="km-cat-tag">{{ doc.category }}</span></td>
                <td><span class="km-source-tag">{{ doc.source === 'custom' ? '自定义' : '系统' }}</span></td>
                <td>
                  <button
                    class="km-toggle"
                    :class="{ active: doc.enabled }"
                    :disabled="doc.source === 'classpath'"
                    @click="toggleEnabled(doc)"
                  >
                    <span class="toggle-track"></span>
                  </button>
                </td>
                <td class="km-cell-actions">
                  <button class="km-action-btn" title="编辑" @click="startEdit(doc)">
                    <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M11 2l3 3-9 9H2v-3z"/></svg>
                  </button>
                  <button
                    class="km-action-btn km-action-del"
                    title="删除"
                    :disabled="doc.source === 'classpath'"
                    @click="confirmDelete(doc)"
                  >
                    <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M2 4h12M5 4V2.5a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 .5.5V4M4 4v9a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4"/></svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="km-footer">
          <span class="km-footer-text">系统文档不可编辑，自定义文档可自由管理</span>
          <span class="km-footer-count">共 {{ filteredDocuments.length }} 条</span>
        </div>
      </div>

      <!-- 表单视图 -->
      <div v-else class="km-body">
        <div class="km-form-header">
          <button class="km-btn km-btn-text" @click="cancelForm">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" class="km-btn-icon"><path d="M10 4L6 8l4 4"/></svg>
            返回列表
          </button>
          <span class="km-form-title">{{ editingId ? '编辑文档' : '添加文档' }}</span>
        </div>

        <div class="km-form">
          <div class="km-field">
            <label class="km-field-label">标题 <span class="km-req">*</span></label>
            <input v-model="form.title" class="km-input" placeholder="输入文档标题" maxlength="200" />
          </div>

          <div class="km-field">
            <label class="km-field-label">分类 <span class="km-req">*</span></label>
            <select v-model="form.category" class="km-select km-select-full">
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>

          <div class="km-field">
            <label class="km-field-label">文档内容 <span class="km-req">*</span></label>
            <textarea
              v-model="form.content"
              class="km-textarea"
              placeholder="输入文档内容（支持 Markdown 格式）"
              maxlength="50000"
              rows="12"
            ></textarea>
            <span class="km-char-count">{{ form.content.length }} / 50000</span>
          </div>

          <div class="km-field-row">
            <label class="km-field-label">启用</label>
            <button
              class="km-toggle km-toggle-lg"
              :class="{ active: form.enabled }"
              @click="form.enabled = !form.enabled"
            >
              <span class="toggle-track"></span>
            </button>
          </div>

          <p v-if="formError" class="km-error">{{ formError }}</p>

          <div class="km-form-actions">
            <button class="km-btn km-btn-primary km-btn-lg" :disabled="saving" @click="saveDocument">
              {{ saving ? '保存中...' : (editingId ? '保存修改' : '创建文档') }}
            </button>
            <button class="km-btn km-btn-secondary km-btn-lg" @click="cancelForm">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  listKnowledgeDocuments,
  getKnowledgeDocumentCount,
  getKnowledgeCategories,
  createKnowledgeDocument,
  updateKnowledgeDocument,
  deleteKnowledgeDocument,
  reindexKnowledgeDocuments
} from '../api'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'documents-changed'])

const documents = ref([])
const categories = ref(['恋爱', '婚姻', '单身', '自定义'])
const loading = ref(false)
const saving = ref(false)
const reindexing = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const searchQuery = ref('')
const filterCategory = ref('')
const formError = ref('')
const enabledCount = ref(0)
const totalCount = ref(0)

const form = reactive({
  title: '',
  content: '',
  category: '恋爱',
  enabled: true
})

const filteredDocuments = computed(() => {
  let items = documents.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    items = items.filter(d => d.title.toLowerCase().includes(q))
  }
  if (filterCategory.value) {
    items = items.filter(d => d.category === filterCategory.value)
  }
  return items
})

const fetchData = async () => {
  loading.value = true
  try {
    const [docs, countResult, cats] = await Promise.all([
      listKnowledgeDocuments(),
      getKnowledgeDocumentCount(),
      getKnowledgeCategories().catch(() => ['恋爱', '婚姻', '单身', '自定义'])
    ])
    documents.value = docs
    categories.value = cats
    enabledCount.value = countResult.enabledCount
    totalCount.value = countResult.totalCount
  } catch (e) {
    console.error('Failed to load knowledge documents:', e)
  } finally {
    loading.value = false
  }
}

const startCreate = () => {
  editingId.value = null
  form.title = ''
  form.content = ''
  form.category = '恋爱'
  form.enabled = true
  formError.value = ''
  showForm.value = true
}

const startEdit = (doc) => {
  editingId.value = doc.id
  form.title = doc.title
  form.content = doc.content
  form.category = doc.category
  form.enabled = doc.enabled
  formError.value = ''
  showForm.value = true
}

const cancelForm = () => {
  showForm.value = false
  editingId.value = null
  formError.value = ''
}

const saveDocument = async () => {
  if (!form.title.trim()) {
    formError.value = '请输入文档标题'
    return
  }
  if (!form.content.trim()) {
    formError.value = '请输入文档内容'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    if (editingId.value) {
      await updateKnowledgeDocument(editingId.value, {
        title: form.title.trim(),
        content: form.content.trim(),
        category: form.category,
        enabled: form.enabled
      })
    } else {
      await createKnowledgeDocument({
        title: form.title.trim(),
        content: form.content.trim(),
        category: form.category
      })
    }
    showForm.value = false
    editingId.value = null
    emit('documents-changed')
    await fetchData()
  } catch (e) {
    formError.value = e?.response?.data?.message || e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

const toggleEnabled = async (doc) => {
  try {
    await updateKnowledgeDocument(doc.id, {
      title: doc.title,
      content: doc.content,
      category: doc.category,
      enabled: !doc.enabled
    })
    emit('documents-changed')
    await fetchData()
  } catch (e) {
    console.error('Toggle failed:', e)
  }
}

const confirmDelete = async (doc) => {
  if (!confirm(`确定删除「${doc.title}」吗？此操作不可撤销。`)) return
  try {
    await deleteKnowledgeDocument(doc.id)
    emit('documents-changed')
    await fetchData()
  } catch (e) {
    console.error('Delete failed:', e)
  }
}

const handleReindex = async () => {
  reindexing.value = true
  try {
    const result = await reindexKnowledgeDocuments()
    alert(`重新索引完成，已索引 ${result.indexedCount} 篇文档。`)
  } catch (e) {
    alert('重新索引失败：' + (e?.response?.data?.message || e.message))
  } finally {
    reindexing.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    fetchData()
    showForm.value = false
  }
})
</script>

<style scoped>
.km-overlay {
  position: fixed; inset: 0;
  z-index: 999;
  display: flex;
  align-items: center; justify-content: center;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  padding: 20px;
}

.km-dialog {
  width: min(720px, 100%);
  max-height: 80vh;
  display: flex; flex-direction: column;
  background: var(--color-base-1);
  border: var(--border-subtle);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-elevated);
  overflow: hidden;
}

.km-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 22px;
  border-bottom: var(--border-subtle);
  flex-shrink: 0;
}
.km-header-left { display: flex; align-items: center; gap: 12px; }
.km-title { margin: 0; font-size: 1rem; font-weight: 700; color: var(--color-text-1); }
.km-count { font-size: 0.78rem; color: var(--color-text-2); padding: 3px 10px; background: rgba(255,255,255,0.03); border-radius: var(--radius-full); }
.km-header-actions { display: flex; align-items: center; gap: 8px; }

/* buttons */
.km-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 14px; border: var(--border-subtle);
  border-radius: var(--radius-sm);
  background: transparent; color: var(--color-text-2);
  cursor: pointer; font-size: 0.82rem; font-weight: 600;
  transition: all var(--duration-fast) var(--ease-out);
}
.km-btn:hover:not(:disabled) { background: rgba(255,255,255,0.04); color: var(--color-text-1); border-color: rgba(255,255,255,0.10); }
.km-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.km-btn-icon { width: 14px; height: 14px; flex-shrink: 0; }
.km-btn-primary { background: rgba(240,192,96,0.08); color: var(--color-glow); border-color: rgba(240,192,96,0.15); }
.km-btn-primary:hover:not(:disabled) { background: rgba(240,192,96,0.14); border-color: rgba(240,192,96,0.25); }
.km-btn-secondary { background: rgba(126,200,224,0.06); color: var(--color-aurora-1); border-color: rgba(126,200,224,0.12); }
.km-btn-secondary:hover:not(:disabled) { background: rgba(126,200,224,0.12); }
.km-btn-close { padding: 6px; border: none; background: none; color: var(--color-text-2); }
.km-btn-close:hover { color: var(--color-text-1); background: rgba(255,255,255,0.04); border-radius: 50%; }
.km-btn-text { border: none; background: none; color: var(--color-text-2); padding: 4px; }
.km-btn-text:hover { color: var(--color-glow); }
.km-btn-lg { padding: 10px 22px; font-size: 0.88rem; }

.km-body { flex: 1; overflow-y: auto; padding: 16px 22px; min-height: 200px; }

.km-toolbar { margin-bottom: 14px; }
.km-filter-row { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.km-search {
  flex: 1; min-width: 150px; min-height: 36px; padding: 0 12px;
  background: var(--glass-input); border: var(--border-input);
  border-radius: var(--radius-sm); color: var(--color-text-1);
  font-size: 0.85rem; font-family: var(--font-body); outline: none;
}
.km-search::placeholder { color: var(--color-text-3); }
.km-search:focus { border-color: rgba(240,192,96,0.25); }

.km-select {
  min-height: 36px; padding: 0 12px;
  background: var(--color-base-2); border: var(--border-input);
  border-radius: var(--radius-sm); color: var(--color-text-1);
  font-size: 0.82rem; cursor: pointer; outline: none; min-width: 100px;
}
.km-select-full { width: 100%; }

.km-state { display: flex; align-items: center; justify-content: center; min-height: 120px; color: var(--color-text-2); font-size: 0.85rem; }

/* table */
.km-table-wrap { overflow-x: auto; }
.km-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
.km-table th {
  text-align: left; padding: 10px 10px 8px;
  color: var(--color-text-2); font-weight: 600; font-size: 0.75rem;
  border-bottom: var(--border-subtle); letter-spacing: 0.04em;
}
.km-table td { padding: 10px; border-bottom: 1px solid rgba(255,255,255,0.02); color: var(--color-text-1); }
.km-table tbody tr:hover { background: rgba(255,255,255,0.02); }
.km-col-title { min-width: 140px; }
.km-col-cat { width: 70px; }
.km-col-source { width: 60px; }
.km-col-status { width: 60px; }
.km-col-actions { width: 80px; text-align: right; }
.km-cell-title { font-weight: 600; }
.km-cat-tag, .km-source-tag {
  display: inline-block; padding: 2px 8px; border-radius: var(--radius-full);
  font-size: 0.72rem; font-weight: 600;
}
.km-cat-tag { background: rgba(180,160,232,0.08); color: var(--color-aurora-2); }
.km-source-tag { background: rgba(126,200,224,0.08); color: var(--color-aurora-1); }

/* toggle switch */
.km-toggle {
  position: relative; width: 32px; height: 18px;
  border: 0; border-radius: 999px;
  background: var(--color-base-4); cursor: pointer;
  transition: background var(--duration-fast) var(--ease-out);
  padding: 0;
}
.km-toggle::after {
  content: ''; position: absolute; top: 2px; left: 2px;
  width: 14px; height: 14px; border-radius: 50%;
  background: var(--color-base-1);
  box-shadow: 0 1px 4px rgba(0,0,0,0.3);
  transition: transform var(--duration-fast) var(--ease-out);
}
.km-toggle.active { background: var(--color-glow); }
.km-toggle.active::after { transform: translateX(14px); background: var(--color-base-0); }
.km-toggle:disabled { opacity: 0.35; cursor: not-allowed; }

.km-action-btn {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border: none; border-radius: var(--radius-sm);
  background: transparent; color: var(--color-text-2); cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.km-action-btn svg { width: 14px; height: 14px; }
.km-action-btn:hover { background: rgba(255,255,255,0.04); color: var(--color-text-1); }
.km-action-del:hover { color: #f0a0a0; background: rgba(240,120,120,0.08); }
.km-action-btn:disabled { opacity: 0.2; cursor: not-allowed; }

.km-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 0 4px; border-top: var(--border-subtle); margin-top: 8px;
}
.km-footer-text { font-size: 0.72rem; color: var(--color-text-3); }
.km-footer-count { font-size: 0.75rem; color: var(--color-text-2); }

/* form */
.km-form-header {
  display: flex; align-items: center; gap: 12px; margin-bottom: 20px;
}
.km-form-title { font-size: 0.95rem; font-weight: 700; color: var(--color-text-1); }
.km-form { display: flex; flex-direction: column; gap: 16px; }
.km-field { display: grid; gap: 5px; }
.km-field-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.km-field-label { font-size: 0.82rem; color: var(--color-text-2); font-weight: 500; }
.km-req { color: var(--color-aurora-3); }
.km-input, .km-textarea {
  width: 100%; padding: 10px 12px;
  background: var(--glass-input); border: var(--border-input);
  border-radius: var(--radius-sm); color: var(--color-text-1);
  font-size: 0.88rem; font-family: var(--font-body); outline: none;
  transition: border-color var(--duration-fast) var(--ease-out);
}
.km-input:focus, .km-textarea:focus { border-color: rgba(240,192,96,0.25); }
.km-textarea { resize: vertical; min-height: 160px; line-height: 1.6; }
.km-char-count { font-size: 0.72rem; color: var(--color-text-3); text-align: right; margin-top: 2px; }
.km-input::placeholder, .km-textarea::placeholder { color: var(--color-text-3); }
.km-toggle-lg { width: 40px; height: 22px; }
.km-toggle-lg::after { width: 16px; height: 16px; }
.km-toggle-lg.active::after { transform: translateX(18px); }

.km-error {
  padding: 8px 12px; font-size: 0.82rem;
  color: #f0a0a0; background: rgba(240,120,120,0.06);
  border: 1px solid rgba(240,120,120,0.10); border-radius: var(--radius-sm);
}
.km-form-actions { display: flex; gap: 10px; margin-top: 4px; }

@media (max-width: 600px) {
  .km-dialog { max-height: 90vh; }
  .km-body { padding: 12px 14px; }
  .km-header { padding: 14px; flex-wrap: wrap; gap: 8px; }
  .km-filter-row { flex-direction: column; }
  .km-search { width: 100%; }
  .km-select { width: 100%; }
}
</style>
