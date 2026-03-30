# 俄罗斯方块 Android 项目

## 项目名称
WeirdTetris

## 项目仓库

- GitHub：<https://github.com/SuzakuinAyaka/WeirdTetris>

## 项目简介

这是一个基于 Java 开发的 Android 手机端俄罗斯方块项目，整体采用 Material You（Material 3）设计风格。

当前主流程包含 6 个核心 Activity：

- 欢迎页（WelcomeActivity）
- 模式选择页（ModeSelectionActivity）
- 对局页（GameActivity）
- 商店/背包合并页（ShopActivity）
- 设置页（SettingsActivity）
- 教程页（TutorialActivity）

## Activity 功能说明

- WelcomeActivity：应用入口。提供开始游戏、教程、商店/背包、语言、设置入口，并在背景播放低透明度模拟对局动画。
- ModeSelectionActivity：选择无尽/挑战模式；支持本局道具模式开关和自定义难度开关（默认/较快/非常快）；支持展开/收起动画与自动滚动定位。
- GameActivity：核心对局逻辑（下落、碰撞、消行、计分、等级、结算、快捷道具使用、金币结算与提示、暂停/退出流程）。
- ShopActivity：商店与背包合并页。上半部分购买道具，下半部分配置两个快捷栏槽位，选择后即时生效。
- SettingsActivity：外观/偏好/系统三大板块；主题、动态配色、预设配色、语言、默认道具模式、消行震动开关；底部项目信息卡片与隐藏金币面板入口。
- TutorialActivity：展示基础操作、经典/道具差异、无尽/挑战差异、得分、升级、金币规则。

## 页面流转

- 启动后进入 WelcomeActivity。
- 点击“开始游戏”进入 ModeSelectionActivity，点击模式卡片进入 GameActivity。
- 点击“教程”进入 TutorialActivity。
- 点击“设置”进入 SettingsActivity。
- 点击“商店 / 背包”进入 ShopActivity（该入口在默认道具模式开启时显示）。
- 在 GameActivity 顶部可进入同一个 ShopActivity，返回后继续当前对局。

## 模式与难度

ModeSelectionActivity 提供两个大卡片模式：

1. 经典/道具 - 无尽模式
2. 经典/道具 - 挑战模式

并提供两个开关：

- 启用/禁用道具模式（标题已加粗）
- 启用自定义难度（标题已加粗）

自定义难度说明：

- 选项：默认、较快、非常快。
- 无尽模式：全程使用所选初始速度。
- 挑战模式：以所选速度为初始值，仍受最高速度上限限制。
- 关闭自定义难度后，恢复项目默认速度参数。

## 核心玩法规则

- 方块：7 种经典形状（I/O/T/L/J/S/Z），使用 Bag 随机袋生成。
- 操作：左右移动、旋转、下沉。
- 左右移动支持长按连移，并在每次移动后立即刷新棋盘渲染。
- 棋盘支持落点预览（落地导引轮廓），颜色与当前方块颜色一致。
- 退出流程：点击“暂停 / 退出”后弹出 Material You 风格确认弹窗；确认期间暂停并模糊背景。
- 若当前已消除行数为 `0` 且选择退出，直接返回欢迎页，不展示结算弹窗。

## 金币与经济系统

- 仅在道具模式下可获得金币。
- 金币规则：
  - 每消除 1 行：`+10`
  - 四消额外奖励：`+20`
- “收入加倍”生效期间：消行金币为 3 倍（持续 60 秒）。
- 游戏统计卡片金币右侧会短暂显示收入提示（如 `+40`）。

## 道具一览

| 物品名称 | 价格 | 作用 |
| --- | ---: | --- |
| 定向消除 | 150 | 清除当前空格最多的一整行 |
| 炸弹方块 | 200 | 下一个方块变为炸弹方块，落地清除 3x3 区域 |
| 冻结时间 | 80 | 当前下落节奏冻结 5 秒（期间可移动与旋转） |
| 薯条 | 80 | 下一个方块必定为 I 方块 |
| 收入加倍 | 100 | 60 秒内消行金币变为 3 倍 |
| 销毁方块 | 300 | 使当前正在下落且未落地的方块直接消失 |

## 商店/背包与快捷栏

- ShopActivity 合并了“商店 + 背包配置”：
  - 上半区：道具卡片（标题、说明、价格、购买按钮、持有数量）。
  - 下半区：两个快捷栏配置槽位（Exposed Dropdown）。
- 快捷栏配置为即时生效，无需额外保存按钮。
- 配置下拉候选仅显示“已持有道具”；候选文案为“道具名(数量)”。
- 下拉选中后输入框仅显示道具名（不显示数量）。
- 购买成功/失败提示使用短时 Toast（约 500ms）。

## 游戏页面信息与布局

- 顶部一行两个按钮：`暂停 / 退出`、`商店 / 背包`。
- 统计区与下一个方块预览区采用 Material You 卡片样式，卡片高度压缩以留出更多棋盘空间。
- 金币栏仅在道具模式开启时显示；非道具局不显示金币。
- 快捷道具栏为 2 行 3 列：
  - 第 1 行左右两格显示前两个快捷槽位的数量（无道具显示 `0`）。
  - 中间列上下合并为“背包物品”按钮，点击弹出“已持有道具(数量)”下拉并可直接使用。
  - 第 2 行左右两格为两个快捷使用按钮，文案显示“道具名(数量)”或“空”。
- 操作区：第一行 `← / 旋转图标 / →`，第二行为整行 `↓` 下沉按钮。

## 设置页说明

设置页按三大板块组织：

- 外观：主题模式、动态配色、预设配色（动态配色开启时，预设配色区域通过挤占动画折叠隐藏）。
- 偏好：默认道具模式开关、消行震动开关（Line Clear Haptics）。
- 系统：语言切换。

底部项目卡片显示：

- 版本号、构建类型、包名、仓库地址（https://github.com/SuzakuinAyaka/WeirdTetris）。
- 仓库按钮可直接跳转到 GitHub 仓库。
- 连续短时间点击该卡片 5 次可打开金币调整面板。

## 主题与配色管理

- 使用统一配色源：`res/values/theme_palette.xml`。
- `themes.xml` 中的 Blue/Green/Orange 主题直接引用该文件颜色。
- 动态配色开启时走系统动态颜色；关闭时使用预设主题。

## 多语言与本地化

- 语言资源：
  - `res/values/strings.xml`
  - `res/values-zh/strings.xml`
  - `res/values-en/strings.xml`
- 文案统一使用字符串资源，避免硬编码。

## 横屏与设备适配

- 手机：固定竖屏。
- 平板：允许横屏，并启用 `layout-sw600dp-land` 专用布局。
- 平板对局页支持左侧抽屉式商店面板。
- 基础页面统一通过 `UiUtils.applyStatusBarInsets` 适配状态栏安全区。

## 分数与本地存储

- 最高分、金币、道具库存、快捷栏配置、主题/语言等通过 `SharedPreferences` 持久化。
- 核心设置入口由 `AppSettingsManager` 统一管理。

## 构建与运行

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 连接 Android 设备或启动模拟器。
4. 点击 Run 运行应用。

## 目录说明（核心）

- `app/src/main/java/com/example/els/core/`：应用级设置、主题/语言应用、设备与状态栏适配。
- `app/src/main/java/com/example/els/game/`：常量、模式参数、道具映射、方块模型与随机袋。
- `app/src/main/java/com/example/els/ui/`：各页面 Activity。
- `app/src/main/java/com/example/els/ui/widget/`：棋盘与下一个方块绘制组件。
- `app/src/main/res/layout/`：手机布局。
- `app/src/main/res/layout-sw600dp-land/`：平板横屏布局。
- `app/src/main/res/values/theme_palette.xml`：统一主题色板。

## 开发者技术定位（维护）

为便于定位业务逻辑，优先关注以下文件与方法：

- `ui/mode/ModeSelectionActivity`
  - `updateModePresentation`：模式标题（经典/道具）与说明更新。
  - `setShopEntryVisible` / `setCustomSpeedConfigVisible`：展开折叠动画。
  - `openGame`：通过 Intent 注入模式、道具开关、自定义速度参数。
- `ui/game/GameActivity`
  - 主循环与结算：`onGameTick`、`lockCurrentPiece`、`addScoreForLineClear`、`showSettlementDialog`。
  - 输入：`handleHorizontalMoveTouch`、`startHorizontalMoveRepeat`、`moveHorizontallyOnce`。
  - 道具链路：`useItem`、`applyDestroyCurrentPieceItem`、`awardCoinsForClearAction`。
  - UI 同步：`updateQuickSlotPanel`、`updateInfoViews`、`showCoinIncomeHint`。
- `ui/shop/ShopActivity`
  - 购买：`buyItem`。
  - 快捷栏配置：`buildQuickSlotOptions`、`applyQuickSlotSelection`、`normalizeQuickSlots`。
- `ui/settings/SettingsActivity`
  - 实时设置监听：`setupRealtimeListeners`。
  - 动态配色与预设区动画：`setColorPresetEnabled`。
  - 隐藏金币面板：`onProjectFooterTapped`、`showCoinAdjustDialog`。
- `ui/widget/TetrisBoardView`
  - 棋盘渲染：`onDraw`。
  - 落点预览：`drawLandingGuide`（使用当前方块同色轮廓）。
- `core/AppSettingsManager`
  - 所有偏好项、库存、快捷栏、金币、最高分的统一读写入口。
## 更新日志

### v1.0.0（2026-03-29）

- 文档重构：README 与当前实现对齐，补齐页面流转、模式与难度、快捷栏结构、道具与金币规则、技术定位说明。
- 多语言修复：修复 `values-zh/strings.xml` 异常并恢复可读中文文案，保证 key 与默认语言资源一致。
- 模式选择页：
  - 新增“自定义难度”开关与速度选项（默认/较快/非常快）。
  - 道具模式入口卡片与难度配置卡片支持展开/收起动画。
  - “启用/禁用道具模式”“启用自定义难度”开关标题加粗。
- 欢迎页：模拟对局动画作为背景显示，保留开始/教程/商店背包/语言/设置卡片入口。
- 商店与背包：合并为同一 `ShopActivity`，上半区购买道具，下半区配置 2 个快捷槽位，选择后立即生效。
- 对局页交互：
  - 顶部保留“暂停/退出”和“商店/背包”入口。
  - 左右移动支持长按连移并即时刷新。
  - 底部操作区调整为 `← / 旋转 / →` + 整行 `↓`。
- 快捷栏升级：改为 2 行 3 列结构，中间为“背包物品”入口，左右为数量与快捷使用按钮。
- 道具扩展：新增“销毁方块（300）”，可直接移除当前未落地方块。
- 金币反馈：消行后在统计卡片金币右侧短暂显示收入提示（如 `+40`），并支持“收入加倍”3 倍窗口。
- 设置页：统一 Material You 卡片风格，按“外观/偏好/系统”分区；动态配色开关与预设配色区域采用挤占动画联动显示。
- 配色管理：引入统一配色源 `res/values/theme_palette.xml`，预设主题从该文件读取颜色。
- 弹窗样式：统一 Material You 圆角弹窗风格（大圆角），结算弹窗按钮和内容间距优化。
- 设备适配：基础页面统一做状态栏 inset 处理；手机竖屏、平板横屏布局保持一致行为。
