# Hermes Android Companion — Architecture Document

## Architecture Comparison: Browser Extension vs Android Companion

### Browser Extension (Reference)
- **Context**: Chrome Extension (Manifest V3)
- **Surface**: Browser popup + content script injection
- **Communication**: chrome.runtime messaging + HTTP to Hermes backend
- **State**: chrome.storage.local (KV)
- **UI**: HTML/CSS popup
- **Agents**: Hermes Agent drives via tool calls
- **Scope**: Browser tab control + page DOM interaction

### Android Companion (New)
- **Context**: Native Android App (Kotlin + Jetpack Compose)
- **Surface**: Launcher Activity + Floating Overlay + System Tiles
- **Communication**: WebSocket + HTTP + Android IPC + Binder
- **State**: StateFlow + ViewModel + Room DB
- **UI**: Jetpack Compose with Material 3
- **Agents**: Visual dashboard + direct agent spawning
- **Scope**: Full device runtime control

### Adapted Concepts
| Browser Feature | Android Equivalent |
|---|---|
| Popup UI | Launcher Activity |
| Content Script | Accessibility Service |
| Background Service Worker | Android Foreground Service + WebSocket |
| chrome.storage | Room + DataStore |
| chrome.runtime messaging | JSON-RPC over WebSocket |
| Tab control | Activity Manager + Accessibility |
| Cookie management | Android Account Manager |
| Host permissions | Android Permissions |
| Extension panel | Floating Overlay (Bubble/Expanded/Fullscreen) |

### Browser-Specific (Not Adapted)
- Manifest V3 restrictions
- chrome.* API surface
- Content script sandbox
- Extension store distribution

---

## System Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                    HERMES ANDROID COMPANION                    │
├──────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐│
│  │   UI Layer       │  │  ViewModel      │  │  Repository      ││
│  │  (Compose)       │──│  (StateFlow)    │──│  (Data Source)   ││
│  │                  │  │                 │  │                  ││
│  │  • Home          │  │  • MissionVM    │  │  • WebSocket     ││
│  │  • Mission       │  │  • AgentVM      │  │  • Room DB       ││
│  │  • Agents        │  │  • MemoryVM     │  │  • DataStore     ││
│  │  • Android       │  │  • SettingsVM   │  │  • FileStore     ││
│  │  • Browser       │  │  • DashboardVM  │  │  • EventBus      ││
│  │  • Memory        │  │  • WorkflowVM   │  │                  ││
│  │  • Tools         │  │  • VoiceVM      │  │                  ││
│  │  • Plugins       │  │  • PerformanceVM│  │                  ││
│  │  • Logs          │  │  • SecurityVM   │  │                  ││
│  │  • Performance   │  │  • DeveloperVM  │  │                  ││
│  │  • Settings      │  │  • PluginVM     │  │                  ││
│  │  • Developer     │  │  • LogVM        │  │                  ││
│  └─────────────────┘  └─────────────────┘  └──────────────────┘│
├──────────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                  Service Layer                            │  │
│  │  ┌────────────┐ ┌───────────┐ ┌──────────┐ ┌───────────┐ │  │
│  │  │ Hermes     │ │Accessibility│ │Media     │ │Notif     │ │  │
│  │  │WebSocket   │ │Service     │ │Projection│ │Listener  │ │  │
│  │  │Service     │ │            │ │Service   │ │Service   │ │  │
│  │  └────────────┘ └───────────┘ └──────────┘ └───────────┘ │  │
│  │  ┌────────────┐ ┌───────────┐ ┌──────────┐ ┌───────────┐ │  │
│  │  │ Overlay    │ │Clipboard  │ │Voice     │ │Device     │ │  │
│  │  │Service     │ │Module     │ │Service   │ │Admin      │ │  │
│  │  └────────────┘ └───────────┘ └──────────┘ └───────────┘ │  │
│  └────────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                 Connection Layer                           │  │
│  │  ┌──────────────┐ ┌─────────────┐ ┌───────────────────┐   │  │
│  │  │ Secure WebSocket│ │ Heartbeat   │ │ Auth + Session    │   │  │
│  │  │ ws://127.0.0.1 │ │ Protocol    │ │ Recovery          │   │  │
│  │  │ :9876          │ │ 30s interval│ │ Token Mgmt       │   │  │
│  │  └──────────────┘ └─────────────┘ └───────────────────┘   │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘

              │ WebSocket + JSON-RPC
              ▼
┌──────────────────────────────────────────────────────────────────┐
│              HERMES BRAIN (Ubuntu in Termux PRoot)              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────────────┐ │
│  │ Task     │ │ Planner  │ │ Executor │ │ Knowledge +        │ │
│  │ Engine   │ │ Subsystem│ │          │ │ Memory Systems     │ │
│  └──────────┘ └──────────┘ └──────────┘ └────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

## Communication Protocol

### Connection Flow
1. Companion launches → reads saved Hermes Brain address
2. Opens Secure WebSocket to `ws://127.0.0.1:9876`
3. Sends auth token (from saved config or QR pair)
4. Receives session ID
5. Heartbeat every 30s
6. Streaming events flow in both directions

### Event Types
| Type | Direction | Description |
|---|---|---|
| `brain.status` | Brain→Companion | Brain heartbeat + load |
| `brain.mission.update` | Brain→Companion | Mission progress |
| `brain.mission.complete` | Brain→Companion | Mission finished |
| `brain.memory.query` | Both | Memory read/write |
| `brain.log` | Brain→Companion | Runtime logs |
| `brain.event` | Brain→Companion | Arbitrary events |
| `companion.command` | Companion→Brain | Execute command |
| `companion.mission.create` | Companion→Brain | New mission |
| `companion.query.status` | Companion→Brain | Status request |
| `companion.query.agents` | Companion→Brain | Agent list |
| `companion.query.memory` | Companion→Brain | Memory query |

---

## Module Map

### UI Pages (12)
1. **Home** — Dashboard overview with key stats
2. **Mission** — Mission Center (running, queued, history)
3. **Agents** — Agent Center with health/status
4. **Android** — Runtime Control (accessibility, sensors, etc.)
5. **Browser** — Browser CDP status and control
6. **Memory** — Memory Viewer (short/long/semantic/KG)
7. **Tools** — Tool registry and status
8. **Plugins** — Plugin Marketplace
9. **Logs** — Live log viewer
10. **Performance** — System metrics dashboard
11. **Settings** — App configuration
12. **Developer** — Developer Mode tools

### Services (6)
1. **HermesWebSocketService** — Brain connection
2. **Accessibility Service** — UI tree + gestures
3. **MediaProjection Service** — Screen capture
4. **Notification Listener** — Android notifications
5. **Overlay Service** — Floating assistant
6. **Voice Service** — Wake word + STT/TTS

### Floating Overlay Modes (8)
1. **Bubble** — Minimal circular icon
2. **Compact** — Small bar with quick actions
3. **Expanded** — Full widget view
4. **Fullscreen** — Dialog takeover
5. **Voice** — Listening/responding indicator
6. **Mission Running** — Progress bar
7. **Thinking** — AI thinking animation
8. **Error** — Error state display
9. **Sleep** — Idle/dormant state

### Data Stores
- **Room** — Missions, agents, plugins, logs, memory
- **DataStore** — Settings, preferences, auth
- **StateFlow** — Real-time UI state
- **EventBus** — Cross-component events
