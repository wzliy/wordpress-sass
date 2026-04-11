<script setup>
import { computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import StatePanel from '../../components/common/StatePanel.vue'

const props = defineProps({
  pages: {
    type: Array,
    default: () => [],
  },
  editor: {
    type: Object,
    default: null,
  },
  preview: {
    type: Object,
    default: null,
  },
  versions: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  saving: {
    type: Boolean,
    default: false,
  },
  previewing: {
    type: Boolean,
    default: false,
  },
  publishing: {
    type: Boolean,
    default: false,
  },
  rollingBack: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['save-page-draft', 'preview-page', 'publish-page', 'rollback-page-version'])

const route = useRoute()
const router = useRouter()

const form = reactive({
  title: '',
  subtitle: '',
  themeColor: '#2563EB',
  menuText: '',
  featuredProductIds: '',
  body: '',
  supportTitle: '',
  supportBody: '',
  badgesText: '',
  submitLabel: '',
  policyTitle: '',
  policyBody: '',
  nextStepsText: '',
  ctaLabel: '',
  ctaPath: '',
  versionNote: '',
})

const pageKey = computed(() => props.editor?.pageKey || 'HOME')

watch(
  () => props.editor,
  (editor) => {
    if (!editor?.layout) return
    populateForm(editor)
  },
  { immediate: true },
)

const menuItems = computed(() =>
  form.menuText
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [label, path] = line.split('|').map((value) => value?.trim())
      return {
        label: label || 'Untitled',
        path: path || '/',
      }
    }),
)

const featuredProductIds = computed(() =>
  form.featuredProductIds
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean),
)

const textItems = computed(() =>
  form.badgesText
    .split('\n')
    .map((item) => item.trim())
    .filter(Boolean),
)

const nextSteps = computed(() =>
  form.nextStepsText
    .split('\n')
    .map((item) => item.trim())
    .filter(Boolean),
)

const localPreview = computed(() => {
  switch (pageKey.value) {
    case 'PRODUCT':
      return {
        pageTitle: form.title || 'Product detail promise',
        body: form.body || 'Explain what makes the product worth buying.',
        trustBadges: textItems.value,
        supportTitle: form.supportTitle || 'Shipping & support',
        supportBody: form.supportBody || 'Set delivery and support expectations here.',
        themeColor: '#2563EB',
      }
    case 'CHECKOUT':
      return {
        pageTitle: form.title || 'Secure checkout',
        helperText: form.body || 'Reassure customers before they submit payment.',
        submitLabel: form.submitLabel || 'Place order',
        trustBadges: textItems.value,
        policyTitle: form.policyTitle || 'Payment & shipping policy',
        policyBody: form.policyBody || 'Clarify handling time, taxes and support rules.',
        themeColor: '#2563EB',
      }
    case 'SUCCESS':
      return {
        bannerTitle: form.title || 'Order confirmed',
        bannerSubtitle: form.subtitle || 'Tell customers what happens after they pay.',
        themeColor: form.themeColor || '#2563EB',
        ctaLabel: form.ctaLabel || 'Continue shopping',
        ctaPath: form.ctaPath || '/category/all',
        nextStepsTitle: 'What happens next',
        nextSteps: nextSteps.value,
        supportTitle: form.supportTitle || 'Need help?',
        supportBody: form.supportBody || 'Add support channels and delivery reminders.',
      }
    default:
      return {
        bannerTitle: form.title || 'Untitled home page',
        bannerSubtitle: form.subtitle || 'Add a subtitle to explain the offer.',
        themeColor: form.themeColor || '#2563EB',
        menuItems: menuItems.value,
        featuredProductIds: featuredProductIds.value,
      }
  }
})

const activePreview = computed(() => props.preview?.runtimeConfig || localPreview.value)

const pageDescription = computed(() => {
  switch (pageKey.value) {
    case 'PRODUCT':
      return '维护商品详情页的补充文案、信任标签和发货承诺。'
    case 'CHECKOUT':
      return '维护结账页的确认文案、提交按钮和支付说明。'
    case 'SUCCESS':
      return '维护支付成功页的后续动作、提醒和客服引导。'
    default:
      return '维护首页首屏、导航和精选商品入口。'
  }
})

function resetForm() {
  form.title = ''
  form.subtitle = ''
  form.themeColor = '#2563EB'
  form.menuText = ''
  form.featuredProductIds = ''
  form.body = ''
  form.supportTitle = ''
  form.supportBody = ''
  form.badgesText = ''
  form.submitLabel = ''
  form.policyTitle = ''
  form.policyBody = ''
  form.nextStepsText = ''
  form.ctaLabel = ''
  form.ctaPath = ''
  form.versionNote = ''
}

function populateForm(editor) {
  resetForm()
  const sections = editor.layout?.sections || []

  if (editor.pageKey === 'PRODUCT') {
    const copy = findSection(sections, 'product-copy-1', 'rich-text')
    const trust = findSection(sections, 'trust-1', 'trust-badges')
    const support = findSection(sections, 'shipping-1', 'rich-text')
    form.title = copy?.props?.title || ''
    form.body = copy?.props?.body || ''
    form.badgesText = toMultiline(trust?.props?.items || [])
    form.supportTitle = support?.props?.title || ''
    form.supportBody = support?.props?.body || ''
    return
  }

  if (editor.pageKey === 'CHECKOUT') {
    const hero = findSection(sections, 'checkout-hero-1', 'checkout-notice')
    const trust = findSection(sections, 'security-1', 'trust-badges')
    const policy = findSection(sections, 'policy-1', 'rich-text')
    form.title = hero?.props?.title || ''
    form.body = hero?.props?.body || ''
    form.submitLabel = hero?.props?.submitLabel || ''
    form.badgesText = toMultiline(trust?.props?.items || [])
    form.policyTitle = policy?.props?.title || ''
    form.policyBody = policy?.props?.body || ''
    return
  }

  if (editor.pageKey === 'SUCCESS') {
    const hero = findSection(sections, 'success-hero-1', 'hero-banner')
    const steps = findSection(sections, 'next-steps-1', 'order-next-steps')
    const support = findSection(sections, 'support-1', 'rich-text')
    form.title = hero?.props?.title || ''
    form.subtitle = hero?.props?.subtitle || ''
    form.themeColor = hero?.props?.themeColor || '#2563EB'
    form.ctaLabel = hero?.props?.ctaLabel || ''
    form.ctaPath = hero?.props?.ctaPath || ''
    form.nextStepsText = toMultiline(steps?.props?.items || [])
    form.supportTitle = support?.props?.title || ''
    form.supportBody = support?.props?.body || ''
    return
  }

  const hero = findSection(sections, 'hero-1', 'hero-banner')
  const menu = findSection(sections, 'menu-1', 'top-menu')
  const featured = findSection(sections, 'featured-1', 'featured-products')
  form.title = hero?.props?.title || ''
  form.subtitle = hero?.props?.subtitle || ''
  form.themeColor = hero?.props?.themeColor || '#2563EB'
  form.menuText = (menu?.props?.items || [])
    .map((item) => `${item.label}|${item.path}`)
    .join('\n')
  form.featuredProductIds = (featured?.bindings?.productIds || []).join(', ')
}

function findSection(sections = [], sectionId, fallbackType) {
  return sections.find((item) => item?.id === sectionId)
    || sections.find((item) => item?.type === fallbackType)
    || null
}

function toMultiline(items = []) {
  return items
    .map((item) => String(item || '').trim())
    .filter(Boolean)
    .join('\n')
}

function buildLayout() {
  if (pageKey.value === 'PRODUCT') {
    return {
      pageKey: 'PRODUCT',
      sections: [
        {
          id: 'product-copy-1',
          type: 'rich-text',
          props: {
            title: form.title,
            body: form.body,
          },
        },
        {
          id: 'trust-1',
          type: 'trust-badges',
          props: {
            items: textItems.value,
          },
        },
        {
          id: 'shipping-1',
          type: 'rich-text',
          props: {
            title: form.supportTitle,
            body: form.supportBody,
          },
        },
      ],
    }
  }

  if (pageKey.value === 'CHECKOUT') {
    return {
      pageKey: 'CHECKOUT',
      sections: [
        {
          id: 'checkout-hero-1',
          type: 'checkout-notice',
          props: {
            title: form.title,
            body: form.body,
            submitLabel: form.submitLabel,
          },
        },
        {
          id: 'security-1',
          type: 'trust-badges',
          props: {
            items: textItems.value,
          },
        },
        {
          id: 'policy-1',
          type: 'rich-text',
          props: {
            title: form.policyTitle,
            body: form.policyBody,
          },
        },
      ],
    }
  }

  if (pageKey.value === 'SUCCESS') {
    return {
      pageKey: 'SUCCESS',
      sections: [
        {
          id: 'success-hero-1',
          type: 'hero-banner',
          props: {
            title: form.title,
            subtitle: form.subtitle,
            themeColor: form.themeColor,
            ctaLabel: form.ctaLabel,
            ctaPath: form.ctaPath,
          },
        },
        {
          id: 'next-steps-1',
          type: 'order-next-steps',
          props: {
            title: 'What happens next',
            items: nextSteps.value,
          },
        },
        {
          id: 'support-1',
          type: 'rich-text',
          props: {
            title: form.supportTitle,
            body: form.supportBody,
          },
        },
      ],
    }
  }

  return {
    pageKey: 'HOME',
    sections: [
      {
        id: 'hero-1',
        type: 'hero-banner',
        props: {
          title: form.title,
          subtitle: form.subtitle,
          themeColor: form.themeColor,
        },
      },
      {
        id: 'menu-1',
        type: 'top-menu',
        props: {
          items: menuItems.value,
        },
      },
      {
        id: 'featured-1',
        type: 'featured-products',
        bindings: {
          productIds: featuredProductIds.value,
        },
      },
    ],
  }
}

function defaultVersionNote(action) {
  const label = props.editor?.pageName || '页面'
  if (action === 'publish') return `${label} 发布准备`
  if (action === 'preview') return `${label} 预览准备`
  return `${label} 草稿保存`
}

function handleSave() {
  emit('save-page-draft', {
    layout: buildLayout(),
    versionNote: form.versionNote || defaultVersionNote('save'),
  })
}

function handlePreview() {
  emit('preview-page', {
    layout: buildLayout(),
    versionNote: form.versionNote || defaultVersionNote('preview'),
  })
}

function handlePublish() {
  emit('publish-page', {
    layout: buildLayout(),
    versionNote: form.versionNote || defaultVersionNote('publish'),
  })
}

function handleRollback(version) {
  emit('rollback-page-version', {
    versionId: version.versionId,
    versionNote: `Rollback to v${version.versionNo}`,
  })
}

function navigateToPage(nextPageKey) {
  if (!route.params.id || !nextPageKey) return
  if (String(nextPageKey).toUpperCase() === pageKey.value) return
  router.push(`/sites/${route.params.id}/pages/${String(nextPageKey).toLowerCase()}/editor`)
}

function formatDateTime(value) {
  if (!value) return '暂无时间'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <section class="page-stack">
    <StatePanel
      v-if="loading"
      title="编辑器加载中"
      description="正在同步页面布局、版本和可编辑模块。"
      tone="loading"
    />
    <StatePanel
      v-else-if="errorMessage"
      title="编辑器加载失败"
      :description="errorMessage"
      tone="error"
    />
    <StatePanel
      v-else-if="!editor"
      title="没有可编辑的页面"
      description="当前站点还没有初始化系统页面。"
      tone="empty"
    />
    <template v-else>
      <article class="panel editor-hero">
        <div>
          <span class="workspace-eyebrow">Page Builder</span>
          <h3>{{ editor.pageName }}</h3>
          <p>{{ pageDescription }}</p>
          <p>当前编辑版本 V{{ editor.currentVersionNo }} · {{ editor.currentVersionStatus }}，线上版本 {{ editor.publishedVersionId || '暂无' }}。</p>
        </div>
        <div class="editor-actions">
          <button class="button button-secondary" type="button" :disabled="saving" @click="handleSave">
            {{ saving ? '保存中' : '保存草稿' }}
          </button>
          <button class="button button-secondary" type="button" :disabled="previewing" @click="handlePreview">
            {{ previewing ? '预览中' : '更新预览' }}
          </button>
          <button class="button button-primary" type="button" :disabled="publishing" @click="handlePublish">
            {{ publishing ? '发布中' : `发布${editor.pageName}` }}
          </button>
        </div>
      </article>

      <section class="editor-shell">
        <aside class="panel editor-panel editor-sidebar">
          <div class="panel-header">
            <h3>页面列表</h3>
            <span class="panel-stats">{{ pages.length }} 页</span>
          </div>
          <div class="editor-page-list">
            <button
              v-for="page in pages"
              :key="page.pageKey"
              class="editor-page-card"
              :class="{ 'editor-page-card-active': page.pageKey === editor.pageKey }"
              type="button"
              @click="navigateToPage(page.pageKey)"
            >
              <strong>{{ page.pageName }}</strong>
              <span>{{ page.pageKey }}</span>
              <small>{{ page.status }}</small>
            </button>
          </div>

          <div class="panel-header">
            <h3>区块库</h3>
            <span class="panel-stats">{{ editor.blockLibrary.length }} 个区块</span>
          </div>
          <div class="editor-library">
            <article v-for="block in editor.blockLibrary" :key="block.type" class="editor-library-card">
              <span>{{ block.category }}</span>
              <strong>{{ block.label }}</strong>
              <p>{{ block.fields.map((field) => field.label).join(' / ') }}</p>
            </article>
          </div>
        </aside>

        <article class="panel editor-panel">
          <div class="panel-header">
            <h3>结构配置</h3>
            <span class="panel-stats">{{ editor.pageKey }}</span>
          </div>
          <div class="editor-form-grid">
            <template v-if="editor.pageKey === 'HOME'">
              <label class="editor-field">
                <span>Banner 标题</span>
                <input v-model="form.title" class="input" type="text" placeholder="输入首页标题">
              </label>
              <label class="editor-field">
                <span>主题色</span>
                <input v-model="form.themeColor" class="input" type="text" placeholder="#2563EB">
              </label>
              <label class="editor-field editor-field-wide">
                <span>Banner 副标题</span>
                <textarea v-model="form.subtitle" class="input textarea" rows="4" placeholder="输入副标题"></textarea>
              </label>
              <label class="editor-field editor-field-wide">
                <span>菜单项</span>
                <textarea
                  v-model="form.menuText"
                  class="input textarea"
                  rows="6"
                  placeholder="每行一项，格式：Label|/path"
                ></textarea>
              </label>
              <label class="editor-field editor-field-wide">
                <span>精选商品 ID</span>
                <input
                  v-model="form.featuredProductIds"
                  class="input"
                  type="text"
                  placeholder="例如：101, 102, 103"
                >
              </label>
            </template>

            <template v-else-if="editor.pageKey === 'PRODUCT'">
              <label class="editor-field">
                <span>主标题</span>
                <input v-model="form.title" class="input" type="text" placeholder="例如：Why this product wins">
              </label>
              <label class="editor-field editor-field-wide">
                <span>核心文案</span>
                <textarea v-model="form.body" class="input textarea" rows="6" placeholder="输入商品详情页补充说明"></textarea>
              </label>
              <label class="editor-field editor-field-wide">
                <span>信任标签</span>
                <textarea v-model="form.badgesText" class="input textarea" rows="5" placeholder="每行一个标签"></textarea>
              </label>
              <label class="editor-field">
                <span>发货与支持标题</span>
                <input v-model="form.supportTitle" class="input" type="text" placeholder="例如：Shipping & support">
              </label>
              <label class="editor-field editor-field-wide">
                <span>发货与支持说明</span>
                <textarea v-model="form.supportBody" class="input textarea" rows="5" placeholder="输入履约和售后承诺"></textarea>
              </label>
            </template>

            <template v-else-if="editor.pageKey === 'CHECKOUT'">
              <label class="editor-field">
                <span>结账标题</span>
                <input v-model="form.title" class="input" type="text" placeholder="例如：Secure checkout">
              </label>
              <label class="editor-field">
                <span>提交按钮</span>
                <input v-model="form.submitLabel" class="input" type="text" placeholder="例如：Place order">
              </label>
              <label class="editor-field editor-field-wide">
                <span>结账说明</span>
                <textarea v-model="form.body" class="input textarea" rows="5" placeholder="输入支付前提示"></textarea>
              </label>
              <label class="editor-field editor-field-wide">
                <span>信任标签</span>
                <textarea v-model="form.badgesText" class="input textarea" rows="5" placeholder="每行一个标签"></textarea>
              </label>
              <label class="editor-field">
                <span>政策标题</span>
                <input v-model="form.policyTitle" class="input" type="text" placeholder="例如：Payment & shipping policy">
              </label>
              <label class="editor-field editor-field-wide">
                <span>政策说明</span>
                <textarea v-model="form.policyBody" class="input textarea" rows="5" placeholder="输入支付、税费、物流说明"></textarea>
              </label>
            </template>

            <template v-else>
              <label class="editor-field">
                <span>成功页标题</span>
                <input v-model="form.title" class="input" type="text" placeholder="例如：Order confirmed">
              </label>
              <label class="editor-field">
                <span>主题色</span>
                <input v-model="form.themeColor" class="input" type="text" placeholder="#2563EB">
              </label>
              <label class="editor-field editor-field-wide">
                <span>成功页副标题</span>
                <textarea v-model="form.subtitle" class="input textarea" rows="4" placeholder="输入下单成功说明"></textarea>
              </label>
              <label class="editor-field">
                <span>CTA 文案</span>
                <input v-model="form.ctaLabel" class="input" type="text" placeholder="例如：Continue shopping">
              </label>
              <label class="editor-field">
                <span>CTA 路径</span>
                <input v-model="form.ctaPath" class="input" type="text" placeholder="/category/all">
              </label>
              <label class="editor-field editor-field-wide">
                <span>后续步骤</span>
                <textarea v-model="form.nextStepsText" class="input textarea" rows="5" placeholder="每行一个步骤"></textarea>
              </label>
              <label class="editor-field">
                <span>客服标题</span>
                <input v-model="form.supportTitle" class="input" type="text" placeholder="例如：Need help?">
              </label>
              <label class="editor-field editor-field-wide">
                <span>客服说明</span>
                <textarea v-model="form.supportBody" class="input textarea" rows="5" placeholder="输入售后和客服说明"></textarea>
              </label>
            </template>

            <label class="editor-field editor-field-wide">
              <span>版本说明</span>
              <input v-model="form.versionNote" class="input" type="text" placeholder="例如：首页首屏改版">
            </label>
          </div>
        </article>

        <section class="editor-side-column">
          <article class="panel editor-preview-panel">
            <div class="panel-header">
              <h3>页面预览</h3>
              <span class="panel-stats">{{ preview ? 'Server Preview' : 'Local Preview' }}</span>
            </div>
            <div class="editor-preview" :style="{ '--editor-theme': activePreview.themeColor || '#2563EB' }">
              <template v-if="editor.pageKey === 'PRODUCT'">
                <div class="editor-preview-hero">
                  <span class="editor-preview-kicker">Product Preview</span>
                  <h4>{{ activePreview.pageTitle }}</h4>
                  <p>{{ activePreview.body }}</p>
                </div>
                <div class="editor-preview-featured">
                  <strong>信任标签</strong>
                  <div class="editor-chip-list">
                    <span v-for="item in activePreview.trustBadges || []" :key="item">{{ item }}</span>
                    <span v-if="(activePreview.trustBadges || []).length === 0">暂无标签</span>
                  </div>
                </div>
                <div class="editor-preview-section">
                  <strong>{{ activePreview.supportTitle }}</strong>
                  <p>{{ activePreview.supportBody }}</p>
                </div>
              </template>

              <template v-else-if="editor.pageKey === 'CHECKOUT'">
                <div class="editor-preview-hero">
                  <span class="editor-preview-kicker">Checkout Preview</span>
                  <h4>{{ activePreview.pageTitle }}</h4>
                  <p>{{ activePreview.helperText }}</p>
                </div>
                <div class="editor-preview-featured">
                  <strong>信任标签</strong>
                  <div class="editor-chip-list">
                    <span v-for="item in activePreview.trustBadges || []" :key="item">{{ item }}</span>
                    <span v-if="(activePreview.trustBadges || []).length === 0">暂无标签</span>
                  </div>
                </div>
                <div class="editor-preview-section">
                  <strong>{{ activePreview.policyTitle }}</strong>
                  <p>{{ activePreview.policyBody }}</p>
                  <button class="button button-primary" type="button" disabled>{{ activePreview.submitLabel }}</button>
                </div>
              </template>

              <template v-else-if="editor.pageKey === 'SUCCESS'">
                <div class="editor-preview-hero">
                  <span class="editor-preview-kicker">Success Preview</span>
                  <h4>{{ activePreview.bannerTitle }}</h4>
                  <p>{{ activePreview.bannerSubtitle }}</p>
                  <span class="editor-preview-cta">{{ activePreview.ctaLabel }} · {{ activePreview.ctaPath }}</span>
                </div>
                <div class="editor-preview-featured">
                  <strong>{{ activePreview.nextStepsTitle || 'What happens next' }}</strong>
                  <ol class="editor-preview-list">
                    <li v-for="item in activePreview.nextSteps || []" :key="item">{{ item }}</li>
                    <li v-if="(activePreview.nextSteps || []).length === 0">暂无步骤</li>
                  </ol>
                </div>
                <div class="editor-preview-section">
                  <strong>{{ activePreview.supportTitle }}</strong>
                  <p>{{ activePreview.supportBody }}</p>
                </div>
              </template>

              <template v-else>
                <div class="editor-preview-hero">
                  <span class="editor-preview-kicker">Home Preview</span>
                  <h4>{{ activePreview.bannerTitle }}</h4>
                  <p>{{ activePreview.bannerSubtitle }}</p>
                </div>
                <div class="editor-preview-menu">
                  <span v-for="item in activePreview.menuItems || []" :key="`${item.label}-${item.path}`">
                    {{ item.label }}
                  </span>
                </div>
                <div class="editor-preview-featured">
                  <strong>精选商品</strong>
                  <div class="editor-chip-list">
                    <span v-for="item in activePreview.featuredProductIds || []" :key="item">{{ item }}</span>
                    <span v-if="(activePreview.featuredProductIds || []).length === 0">暂无商品</span>
                  </div>
                </div>
              </template>
            </div>
          </article>

          <article class="panel editor-version-panel">
            <div class="panel-header">
              <h3>版本历史</h3>
              <span class="panel-stats">{{ versions.length }} 条</span>
            </div>
            <div class="editor-version-list">
              <article v-for="version in versions" :key="version.versionId" class="editor-version-card">
                <div class="editor-version-head">
                  <strong>V{{ version.versionNo }}</strong>
                  <div class="editor-version-flags">
                    <span>{{ version.versionStatus }}</span>
                    <span v-if="version.currentVersion">当前</span>
                    <span v-if="version.publishedVersion">线上</span>
                  </div>
                </div>
                <p class="editor-version-note">{{ version.versionNote || '无版本说明' }}</p>
                <p class="editor-version-time">
                  创建 {{ formatDateTime(version.createdAt) }}
                  <span v-if="version.publishedAt"> · 发布 {{ formatDateTime(version.publishedAt) }}</span>
                </p>
                <button
                  class="button button-secondary editor-version-action"
                  type="button"
                  :disabled="rollingBack || version.currentVersion"
                  @click="handleRollback(version)"
                >
                  {{ rollingBack ? '回滚中' : version.currentVersion ? '当前版本' : '回滚为新草稿' }}
                </button>
              </article>
            </div>
          </article>
        </section>
      </section>
    </template>
  </section>
</template>
