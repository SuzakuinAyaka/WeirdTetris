# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2026-03-30

### Fixed

- 调整 Gradle Wrapper 分发地址为腾讯镜像 `mirrors.cloud.tencent.com/gradle`，并切换为 `all` 分发包，提升国内网络环境下的可用性。
- 修复动态配色开关的即时生效问题：切换后可通过页面重建立即应用。
- 修复全局 WindowInsets 仅处理顶部的问题，补齐底部导航栏 inset，避免底部控件被遮挡。
- 修复平板旋转重开局问题：`GameActivity` 新增对局状态保存与恢复（棋盘、方块、分数、计时、状态）。

### Changed

- `settings.gradle.kts` 改为腾讯镜像优先并保留官方仓库兜底，提升国内网络环境下的依赖下载速度与成功率。
- 版本号升级：`1.0.0` -> `1.1.0`（`versionCode: 10000 -> 10100`）。
- README 重构为规范化结构，并将更新日志独立至本文件。

### Added

- 新增单元测试：
  - `GameConstantsTest`
  - `TetrominoBagGeneratorTest`

## [1.0.0] - 2026-03-29

### Added

- 完成基础玩法（无尽/挑战）与核心对局流程。
- 完成道具系统、金币系统、商店与背包（快捷栏）功能。
- 完成设置页（主题、动态配色、语言、偏好）与教程页。
- 完成手机/平板布局适配与主要页面流转。
- 完成 v1 文档整理。



