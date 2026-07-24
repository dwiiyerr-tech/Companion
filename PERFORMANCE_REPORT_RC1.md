# Hermes Android Companion — Performance Report (RC1)

## Environment Constraints

**Cannot measure runtime performance** — requires Android device/emulator with Android SDK and JDK.

This report documents expected performance characteristics based on architecture.

## Expected Performance Characteristics

### Startup Time
| Phase | Expected | Notes |
|-------|----------|-------|
| Process launch | ~800ms | Koin init + Compose composition |
| Activity + Compose setup | ~300ms | Single Activity, 12 screens lazy |
| WebSocket connect | ~200ms | Local network to Termux |
| **Total cold start** | **~1.3s** | Target: <2s |

### Frame Rendering
| Metric | Target | Architecture Support |
|--------|--------|---------------------|
| 60 FPS | 16.6ms/frame | Compose lazy lists, no heavy computation on UI thread |
| 90 FPS | 11ms/frame | Compose 1.5+, no overdraw |
| Jank frames | <5% | StateFlow prevents UI thread blocking |

### Memory
| Component | Estimate | Notes |
|-----------|----------|-------|
| Base app | ~35MB | Compose + Koin + Room |
| WebSocket | ~5MB | OkHttp + buffers |
| Room DB | ~10MB | Cached missions/agents/logs |
| Services | ~15MB | 6 services (4 foreground) |
| **Total** | **~65MB** | Well under 128MB typical limit |

### CPU
| Scenario | Expected | Notes |
|----------|----------|-------|
| Idle | <1% | Coroutines parked |
| WebSocket receive | <2% | OkHttp I/O thread |
| Log streaming | <3% | Compose lazy composition |
| Screen capture | ~5% | MediaProjection + encoding |
| **Sustained** | **<5%** | No continuous heavy computation |

### Battery
| Component | Impact | Mitigation |
|-----------|--------|------------|
| Foreground services | Moderate | WorkManager for periodic tasks |
| WebSocket heartbeat | Low | 30s interval |
| Accessibility service | Moderate | Event-based, not polling |
| MediaProjection | High | On-demand only |
| Overlay | Low | Minimal redraw |

**Estimated drain**: 3-5%/hour active use, <1%/hour background

### Network
| Metric | Value |
|--------|-------|
| WebSocket protocol | JSON over WS |
| Message size | ~200 bytes avg |
| Heartbeat | 30s (20 bytes) |
| Reconnect backoff | 5s → 10s → 30s → 60s (max) |
| Bandwidth | <50 KB/hour typical |

### APK Size
| Component | Estimate |
|-----------|----------|
| Code (Kotlin + Compose) | ~4 MB |
| Resources | ~2 MB |
| Native libs (none) | 0 MB |
| **Debug APK** | **~12 MB** |
| **Release APK (R8 + zipalign)** | **~6 MB** |

## Optimization Recommendations

1. **ProGuard/R8**: Already enabled in build.gradle.kts
2. **Dynamic Feature Modules**: Split rarely-used screens (Developer, Plugins) into dynamic features
3. **Lazy Loading**: Screen composables already use LazyColumn
4. **Image Loading**: Coil configured for efficient bitmap decoding
5. **Database**: Room with proper indexes on mission_id, agent_id, timestamp

## Monitoring (Post-Launch)

Add Firebase Performance or custom metrics for:
- Cold start time (App Startup)
- Screen render time (Compose Frame Timing)
- Memory (RAM tracking)
- Battery (JobScheduler)
- Network (OkHttp EventListener)
- ANR rate (Play Console)