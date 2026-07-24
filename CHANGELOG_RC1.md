# Hermes Android Companion — Release Candidate RC1

## Changelog (v1.0.0-rc1)

### Date: 2026-07-24

### Fixed
- Removed duplicate LogLevel enum from CompanionProtocol (now imports from core.domain)
- Removed duplicate LogLevel enum from LogsScreen (now imports from core.domain)
- Removed duplicate LogLevel/EventLevel from DeveloperScreen (merged to EventLevel)
- Removed duplicate LogEntry data class from LogStream (now imports from core.domain)
- Fixed 8 ViewModel files with wrong package declarations (ui.screen → ui.screenmodel)
- Fixed LogLevel.WARN → LogLevel.WARNING in BrowserViewModel
- Fixed MissionRepository inner Mission class → CachedMission (no longer collides with domain Mission)
- Added missing imports in BrowserViewModel for LogEntry and LogLevel
- Added BrowserUiState data class to BrowserViewModel (was undefined)
- Unified LogLevel enum: merged TRACE/DEBUG/INFO/WARNING/ERROR/CRITICAL into single canonical set
- LogEntry entity added as separate file for backward compatibility with UI layer

### Architecture
- 1 source of truth for LogLevel: core/domain/Log.kt
- 1 source of truth for LogEntry: core/domain/LogEntry.kt
- 1 source of truth for Mission: core/domain/Mission.kt
- 1 source of truth for Agent: core/domain/Agent.kt
- 1 source of truth for Memory: core/domain/Memory.kt
- All ViewModels correctly in ui.screenmodel package
- All domain models in core.domain package
- No circular dependencies detected
- Zero duplicate model definitions

### File Inventory (84 files)
- 71 Kotlin source files
- 4 documentation files
- 4 build configuration files
- 6 Android resource XML files
- 1 root-level misplaced file (HermesCompanionApp.kt — should be in app/)

### Modules
| Layer | Count | Description |
|-------|-------|-------------|
| core/domain | 6 | Domain models (Agent, Mission, Log, LogEntry, Memory, Events) |
| core/di | 3 | Koin dependency injection |
| core/event | 1 | EventBus |
| core/network | 2 | WebSocket client + Protocol |
| core/state | 1 | AppState |
| data/local | 6 | Room DB + DAOs + Converters |
| data/repository | 5 | Repository layer |
| service | 6 | Android services |
| ui/screen | 13 | Screen composables + Launcher |
| ui/screenmodel | 14 | ViewModels |
| ui/component | 7 | Reusable UI components |
| ui/navigation | 2 | Navigation + BottomBar |
| ui/theme | 4 | Theme, Colors, Typography, Shapes |

### Build
- minSdk: 26
- targetSdk: 34
- compileSdk: 34
- Kotlin: 1.9.22
- Compose BOM: 2024.02.00
- AGP: 8.2.0
