# 页面装修器设计基线

## 1. 目标

本设计对应 `docs/plan.md` 中的 `BX-03`，用于把“可视化页面装修器”从模糊 backlog 收口为可执行的模块设计。

本轮目标不是直接实现完整 CMS，而是先明确：
- 页面装修器如何接入当前仓库而不推翻既有 storefront 主链路
- 页面数据如何与现有 `site_homepage_config`、`site_setting`、`theme_config` 协同
- 页面编辑、预览、发布、版本管理的最小闭环应该如何拆分

---

## 2. 当前基线与缺口

当前仓库已经具备以下可复用能力：
- `site_homepage_config`：承载首页 banner、menu、featured products 等 storefront 运行时配置
- `site_setting.page_skeleton_json`：承载站点初始化时的页面骨架信息
- `theme_config`：承载站点级主题 token
- `StorefrontPageApplicationService`：当前首页、分类页、商品详情页的运行时聚合入口
- `SiteWorkspaceApplicationService`：已经为“进入页面装修”预留动作和占位卡片

当前缺口：
- 没有独立的页面领域模型，无法表达页面类型、草稿、发布版本和回滚
- 没有后台编辑器接口，前端只能看到工作台占位
- storefront 仍直接读取 `site_homepage_config`，没有从“页面版本”发布到“运行时配置”的机制
- `site_setting.page_skeleton_json` 仅是初始化结果，没有进入后续编辑生命周期

结论：
- 页面装修器必须建立独立 `page` 领域
- 但首版不能直接改写 storefront 的全部渲染机制，应采用“编辑模型 -> 发布编译 -> 运行时配置”的兼容路线

---

## 3. 设计原则

### 3.1 先做首页，再扩到其他页面

当前已支持：
- `HOME`
- `PRODUCT`
- `CHECKOUT`
- `SUCCESS`

后续扩展：
- `LANDING`
- `CONTENT`

原因：
- 当前 storefront 真正存在且可演示的动态页面核心是首页
- 首页已经有可复用的运行时结构，适合作为装修器第一刀
- `PRODUCT / CHECKOUT / SUCCESS` 先收口为“可编辑 + 可预览 + 可发布 + 可回滚”的系统页面，前台仍暂时使用固定模板

### 3.2 先做 schema-driven editor，不做任意 HTML 编辑器

首版只允许编辑受控区块，不允许直接输入任意脚本或复杂 DOM。

原因：
- 可以复用当前 storefront 模板和字段语义
- 更容易做租户隔离、版本回滚和发布校验
- 避免首版直接演化成难以维护的低代码引擎

### 3.3 发布必须走编译，不让 storefront 直接吃草稿

页面编辑数据和 storefront 运行时数据必须分离：
- 编辑器写入 `page_layout_version.layout_json`
- 发布时编译成 `compiled_runtime_json`
- `HOME` 页面发布后同步写入 `site_homepage_config`

这样可以保证：
- storefront 继续稳定运行
- 编辑器迭代不会直接破坏线上页面
- 后续可以逐步把 storefront 从旧配置表迁移到通用页面运行时

### 3.4 所有页面数据保持租户与站点隔离

新表和新查询必须遵守：
- `tenant_id NOT NULL`
- 详情更新查询使用 `id + tenant_id`
- 站点下页面数据默认增加 `site_id`
- 联表或聚合读取不得跨租户

---

## 4. 模块边界

建议新增 `page` 领域，职责如下：

| 模块 | 职责 |
|---|---|
| `page.interfaces` | 编辑器接口、页面版本列表、预览/发布接口 |
| `page.application` | 编辑器业务编排、草稿保存、发布、版本回滚、运行时编译 |
| `page.domain` | 页面、页面版本、区块 schema、发布状态、仓储接口 |
| `page.infrastructure` | MyBatis 仓储、JSON 序列化、运行时编译器 |

现有模块的职责调整：

| 模块 | 边界 |
|---|---|
| `site` | 继续承接站点上下文、工作台入口和默认页面骨架初始化 |
| `storefront` | 保持公开页面路由和运行时渲染，不直接承接编辑动作 |
| `catalog` | 为页面区块提供商品、分类数据绑定来源 |
| `ops/task` | 后续承接页面发布审计和异步发布扩展；首版可先同步发布 |

---

## 5. 数据模型

## 5.1 新增表

### `page`

页面主档，负责表达“站点下有哪些页面”和当前版本指针。

建议字段：

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `tenant_id` | 租户 ID |
| `site_id` | 站点 ID |
| `page_key` | 页面键，如 `HOME` |
| `page_name` | 页面名称 |
| `page_type` | 页面类型，如 `SYSTEM` / `CUSTOM` |
| `status` | 页面状态，如 `DRAFT_ONLY` / `PUBLISHED` / `DISABLED` |
| `current_version_id` | 当前编辑中的版本 |
| `published_version_id` | 当前线上版本 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

约束：
- `UNIQUE (tenant_id, site_id, page_key)`

### `page_layout_version`

页面版本表，承载草稿、已发布版本和编译结果。

建议字段：

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `tenant_id` | 租户 ID |
| `site_id` | 站点 ID |
| `page_id` | 页面 ID |
| `version_no` | 版本号，自增 |
| `version_status` | `DRAFT` / `PUBLISHED` / `ARCHIVED` |
| `schema_version` | 编辑器 schema 版本 |
| `layout_json` | 编辑器原始布局 JSON |
| `compiled_runtime_json` | 发布编译后的运行时 JSON |
| `version_note` | 版本说明 |
| `created_by` | 创建人 |
| `created_at` | 创建时间 |
| `published_at` | 发布时间 |

约束：
- `INDEX (tenant_id, site_id, page_id)`
- `UNIQUE (tenant_id, page_id, version_no)`

## 5.2 暂不新增 `layout_block` 表

首版区块结构直接存在 `layout_json` 中，不拆成单独明细表。

原因：
- 当前块类型有限，JSON 足够承载
- 单表版本管理更简单
- 后续只有在“区块级检索、复用、统计”成为实际需求时，再拆表

## 5.3 与现有表的协同

| 表 | 首版角色 |
|---|---|
| `site_setting.page_skeleton_json` | 站点初始化时生成默认页面骨架，作为 `page` 初始版本来源 |
| `theme_config` | 提供站点级主题 token，编辑器属性面板可读写对应 token |
| `site_homepage_config` | `HOME` 页面发布后的兼容运行时产物，不再作为人工直接编辑的主模型 |

---

## 6. 页面 schema

首版 `layout_json` 建议结构：

```json
{
  "pageKey": "HOME",
  "sections": [
    {
      "id": "hero-1",
      "type": "hero-banner",
      "props": {
        "title": "Demo Shop",
        "subtitle": "Default hero copy",
        "ctaLabel": "Shop now",
        "ctaPath": "/category/all"
      }
    },
    {
      "id": "menu-1",
      "type": "top-menu",
      "props": {
        "items": [
          { "label": "Home", "path": "/" },
          { "label": "Catalog", "path": "/category/all" }
        ]
      }
    },
    {
      "id": "featured-1",
      "type": "featured-products",
      "bindings": {
        "productIds": []
      }
    }
  ]
}
```

首批允许的区块类型：
- `hero-banner`
- `top-menu`
- `featured-products`
- `logo-strip`
- `rich-text`
- `trust-badges`

字段规则：
- 每个区块必须有稳定 `id`
- `type` 决定可编辑属性 schema
- `props` 保存纯展示配置
- `bindings` 保存数据绑定配置，如商品列表、分类或菜单
- 不允许无 schema 的任意字段自由写入

---

## 7. 发布与预览机制

## 7.1 草稿保存

流程：
1. 编辑器读取当前 `page.current_version_id`
2. 用户修改后写回 `page_layout_version.layout_json`
3. 首版可复用单个草稿版本，不强制每次保存新建版本

## 7.2 预览

流程：
1. 把草稿 `layout_json` 送入 `PageRuntimeCompiler`
2. 合并 `theme_config`、站点基础信息、商品发布数据
3. 返回预览态 runtime view model

首版预览不强制落独立预览域名，可先通过后台内嵌 iframe 或独立预览面板实现。

## 7.3 发布

流程：
1. 对草稿执行 schema 校验
2. 生成 `compiled_runtime_json`
3. 把当前版本标记为 `PUBLISHED`
4. 更新 `page.published_version_id`
5. 如果 `page_key = HOME`，把 `compiled_runtime_json` 同步写入 `site_homepage_config`
6. 写审计记录，后续可扩展为异步发布任务

这一步是首版底层机制的关键：
- 编辑器不直接操作 storefront 模板
- storefront 仍读稳定 runtime 配置
- 页面引擎通过编译器向下兼容现有首页渲染

---

## 8. storefront 兼容策略

首版只改“数据来源”，不改“前台渲染骨架”：

| 页面 | 首版策略 |
|---|---|
| `HOME` | 发布时编译到 `site_homepage_config`，继续由 `StorefrontPageApplicationService` 读取 |
| `CATEGORY` | 继续使用固定 Thymeleaf 模板 |
| `PRODUCT` | 继续使用固定 Thymeleaf 模板 |
| `CHECKOUT` | 继续使用固定 Thymeleaf 模板 |
| `SUCCESS` | 继续使用固定 Thymeleaf 模板 |

这样做的好处：
- 不影响已完成的订单与支付主链路
- 装修器第一刀只需要拿下首页编辑闭环
- 后续扩展到其他页面时，可以逐步把 `compiled_runtime_json` 引入对应页面渲染器

---

## 9. 后端接口建议

首版接口建议统一到：

- `GET /api/admin/sites/{siteId}/pages`
- `GET /api/admin/sites/{siteId}/pages/{pageKey}`
- `GET /api/admin/sites/{siteId}/pages/{pageKey}/editor`
- `PUT /api/admin/sites/{siteId}/pages/{pageKey}/draft`
- `POST /api/admin/sites/{siteId}/pages/{pageKey}/preview`
- `POST /api/admin/sites/{siteId}/pages/{pageKey}/publish`
- `GET /api/admin/sites/{siteId}/pages/{pageKey}/versions`
- `POST /api/admin/sites/{siteId}/pages/{pageKey}/versions/{versionId}/rollback`

当前开放：
- `HOME`
- `PRODUCT`
- `CHECKOUT`
- `SUCCESS`

接口职责：
- `pages`：页面列表和当前状态
- `editor`：返回区块树、schema、主题 token、可绑定商品
- `draft`：保存草稿
- `preview`：返回预览编译结果
- `publish`：发布并写入兼容运行时配置
- `versions`：版本记录
- `rollback`：从历史版本复制出新草稿，不直接篡改历史版本

---

## 10. 前端信息架构

编辑器建议路由：
- `/sites/:id/pages/home/editor`

进入路径：
- 站点工作台 `GO_LAYOUT_EDITOR`
- 站点列表页的快捷动作

页面结构遵循 `docs/ui-design-proposal.md`：

1. 顶部编辑栏
- 页面名称
- PC / Mobile 切换
- 预览
- 保存草稿
- 发布
- 版本记录

2. 左侧面板
- 页面树
- 区块库
- 模板区块

3. 中间画布
- 首页实时预览
- 区块选中高亮
- 首版允许“上移 / 下移 / 插入”，拖拽可后置

4. 右侧属性面板
- 文案
- 图片
- 按钮
- 颜色
- 数据绑定

当前范围控制：
- 已落 `HOME / PRODUCT / CHECKOUT / SUCCESS` 的编辑器壳
- 已落 schema 驱动的表单编辑、预览、发布和版本回滚
- 仍未做区块拖拽、模板市场复用和 `undo/redo`

---

## 11. 与工作台的集成

工作台中的页面卡片不再长期停留在“未落地”占位。

下一步调整原则：
- `GO_LAYOUT_EDITOR` 必须跳转到真实内部路由
- 页面模块摘要要读取 `page` 聚合，而不是写死 “0 页面”
- 页面准备度检查项要从“页面骨架是否初始化”升级为“是否存在已发布首页版本”

---

## 12. 分阶段实施建议

### BX-03-01 设计基线
- 完成本设计文档
- 明确 `page` 模块边界、数据模型和发布编译机制

### BX-03-02 schema 与仓储
- 新增 `page`、`page_layout_version` 表
- 新增 domain / repository / mapper
- 站点初始化时为 `HOME` 页面创建默认版本

### BX-03-03 编辑器查询与草稿保存
- 新增页面列表、编辑器查询、草稿保存接口
- 后台新增首页编辑器壳页面

### BX-03-04 预览与发布
- 新增 `PageRuntimeCompiler`
- 发布时把 `HOME` 编译到 `site_homepage_config`
- 工作台接入真实“进入页面装修”动作

### BX-03-05 版本历史与更多页面类型
- 增加版本列表、回滚
- 扩展到 `PRODUCT / CHECKOUT / SUCCESS`
- 视情况引入拖拽和模板复用

---

## 13. 非目标

本阶段明确不做：
- 通用任意 HTML/CSS/JS 编辑器
- 独立 CMS 前台运行时
- 完整低代码模板市场
- 审核流量斗篷规则与页面版本联动
- 与 WordPress 页面编辑双向同步

首版只要求：
- 后台可编辑首页结构
- 可保存草稿
- 可预览
- 可发布
- 发布后 storefront 首页实际生效
