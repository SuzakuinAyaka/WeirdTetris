# WeirdTetris (Android)

一个基于 Java 开发的 Android 俄罗斯方块项目，采用 Material You（Material 3）视觉风格，支持经典玩法与道具玩法。

## 仓库地址

- GitHub: <https://github.com/SuzakuinAyaka/WeirdTetris>

## 功能概览

- 核心对局：方块生成、移动、旋转、消行、计分、升级、结算
- 双模式：无尽模式 / 挑战模式
- 道具体系：商店购买、背包库存、快捷栏配置、局内使用
- 经济系统：金币结算、收入加倍、局内收入提示
- 设置能力：主题模式、动态配色、预设配色、语言切换、偏好开关
- 多语言：`zh` / `en` / 跟随系统
- 设备适配：手机竖屏、平板横屏（含抽屉商店面板）

## 页面结构

- `WelcomeActivity`：应用入口（开始、教程、商店/背包、语言、设置）
- `ModeSelectionActivity`：模式与难度配置
- `GameActivity`：主对局与结算
- `ShopActivity`：商店 + 快捷栏配置
- `SettingsActivity`：外观/偏好/系统设置
- `TutorialActivity`：规则说明

## 技术栈

- Android SDK / Java 11
- AndroidX AppCompat / Activity / ConstraintLayout / DrawerLayout / GridLayout
- Material Components (Material 3)
- SharedPreferences（本地持久化）
- Gradle (Wrapper)

## 运行环境

- Android Studio（建议最新稳定版）
- JDK 11+
- Android minSdk 30

## 快速开始

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle Sync 完成。
3. 连接设备或启动模拟器。
4. 运行 `app` 模块。

## 构建

- Debug 构建：`./gradlew :app:assembleDebug`
- 单元测试：`./gradlew testDebugUnitTest`

## 目录结构

- `app/src/main/java/com/example/els/core/`：全局设置、主题/语言、UI 通用工具
- `app/src/main/java/com/example/els/game/`：常量、方块模型、随机袋
- `app/src/main/java/com/example/els/ui/`：各页面 Activity
- `app/src/main/java/com/example/els/ui/widget/`：棋盘/预览自定义 View
- `app/src/main/res/layout/`：手机布局
- `app/src/main/res/layout-sw600dp-land/`：平板横屏布局

## 数据持久化

通过 `AppSettingsManager` 统一管理以下数据：

- 最高分、金币
- 道具库存
- 快捷栏配置
- 主题、语言与偏好设置

## 版本与更新

- 当前版本：`1.2.0`
- 历史更新见 [CHANGELOG.md](./CHANGELOG.md)
