# Changelog

All notable changes to this project will be documented in this file.

## [1.2.0] - 2026-03-31

### Fixed

- Fixed status bar overlap in tablet landscape by applying system bar insets correctly in DrawerLayout.
- Fixed gameplay continuity after screen rotation: the game now pauses and prompts before resuming.

### Changed

- In tablet landscape mode, all operation buttons are now placed in a bottom action bar.
- Added a blurred background behind the rotation pause dialog on Android 12+.
- Version bumped from 1.1.0 to 1.2.0 (versionCode: 10100 -> 10200).

## [1.1.0] - 2026-03-30

### Fixed

- Switched Gradle Wrapper distribution to Tencent mirror mirrors.cloud.tencent.com/gradle and all package.
- Fixed dynamic color toggle to take effect immediately after switching.
- Fixed global WindowInsets handling to include bottom inset, avoiding covered bottom controls.
- Fixed tablet rotation state loss by restoring game state in GameActivity.

### Changed

- Updated settings.gradle.kts to prioritize Tencent mirror and keep official repositories as fallback.
- Version bumped from 1.0.0 to 1.1.0 (versionCode: 10000 -> 10100).
- Restructured README and moved updates into this standalone changelog file.

### Added

- Added unit tests: GameConstantsTest and TetrominoBagGeneratorTest.

## [1.0.0] - 2026-03-29

### Added

- Initial release including core gameplay (endless/challenge), item and coin systems, shop/inventory, settings/tutorial, and phone/tablet adaptation.
