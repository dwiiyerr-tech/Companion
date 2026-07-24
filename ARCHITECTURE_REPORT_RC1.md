# Hermes Android Companion — Architecture Report (RC1)

## Overview

Hermes Android Companion is a production-grade Android dashboard application designed to be the visual operating system for Hermes AI OS. It communicates with Hermes Brain running in Termux/Ubuntu (proot-distro) via secure WebSocket.

## Architecture Pattern

**MVVM + Repository + Clean Architecture**

```
UI Layer (Compose + Material3)
    ↓
ViewModel Layer (StateFlow + Coroutines)
    ↓
Repository Layer (Data access abstraction)
    ↓
Domain Layer (Models + Events)
    ↓
Data Layer (Room DB + WebSocket + Services)
```

## Module Structure

### Core (Business Logic)
| Module | Purpose | Files |
|--------|---------|-------|
| `core/domain/` | Domain models, events, enums | 6 files |
| `core/di/` | Koin dependency injection | 3 files |
| `core/event/` | EventBus (SharedFlow-based) | 1 file |
| `core/network/` | WebSocket client + protocol | 2 files |
| `core/state/` | Global app state | 1 file |

### Data
| Module | Purpose | Files |
|--------|---------|-------|
| `data/local/` | Room database + DAOs | 6 files |
| `data/repository/` | Repository pattern | 5 files |

### UI
| Module | Purpose | Files |
|--------|---------|-------|
| `ui/screen/` | Screen composables | 13 files |
| `ui/screenmodel/` | ViewModels | 14 files |
| `ui/component/` | Reusable components | 7 files |
| `ui/navigation/` | Nav + BottomBar | 2 files |
| `ui/theme/` | Theme system | 4 files |

### Services
| Module | Purpose | Files |
|--------|---------|-------|
| `service/` | Android foreground/background services | 6 files |

## Data Flow

```
User Action → Composable → ViewModel → Repository → Brain/WebSocket
                                      ↓
                                 Room Database (local cache)
                                      ↓
                                  StateFlow → UI Update
```

## Key Design Decisions

1. **Single Source of Truth**: All domain models live in `core/domain/` and are shared across all layers.
2. **Repository Pattern**: Each domain entity has a corresponding Repository that handles both local DB and remote Brain communication.
3. **EventBus**: Loose coupling between services and UI via SharedFlow-based event bus.
4. **StateFlow**: All reactive state uses Kotlin StateFlow for lifecycle-safe observation.
5. **Koin DI**: Constructor injection for all ViewModels and Repositories.
6. **Room + In-Memory Cache**: Repositories use in-memory StateFlow cache backed by Room for persistence.

## Protocol Layer

The Companion communicates with Hermes Brain via WebSocket using JSON-serialized messages:
- `BrainMessage` / `CompanionMessage` envelope format
- Typed payloads: MissionRequest, AgentStatus, MemoryQuery, etc.
- Authentication via token-based handshake
- 30-second heartbeat with auto-reconnect

## Services Architecture

| Service | Type | Purpose |
|---------|------|---------|
| HermesWebSocketService | Foreground | Maintains Brain connection |
| OverlayService | System alert window | Floating assistant bubble |
| VoiceService | Background | Wake word + STT/TTS |
| HermesAccessibilityService | Accessibility | UI tree reading + gestures |
| HermesNotificationService | NotificationListener | Notification monitoring |
| HermesMediaProjectionService | Foreground | Screen capture |

## Known Limitations

1. No compiled APK (requires Android SDK + JDK for build)
2. Some ViewModels use hardcoded demo data (not yet wired to repositories)
3. Navigation routes exist but some screens bypass ViewModels (AgentScreen, ToolsScreen)
4. Services are defined but not fully wired to EventBus consumers
