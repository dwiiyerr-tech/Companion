# Hermes Android Companion — Workflow Builder Reference

## Drag-and-Drop Workflow Engine for Android

The Workflow Builder is a visual drag-and-drop editor for creating automation
workflows that execute on the Hermes AI OS. Users compose blocks into pipelines.

### Workflow Block Types

| Block | Description | Inputs | Outputs |
|-------|-------------|--------|---------|
| **Goal** | Define a mission objective | Text prompt | Mission spec |
| **AI** | Run AI reasoning/analysis | Prompt, model | Response |
| **Browser** | Browser automation | URL, actions | Result |
| **Android** | Android device control | Action type | Status |
| **Vision** | Screen analysis | Image | Detection result |
| **Voice** | Speech I/O | Audio/text | Text/audio |
| **Memory** | Read/write memories | Query | Memory data |
| **Condition** | Branching logic | Expression | True/False path |
| **Loop** | Repeat actions | Count, block | Iteration output |
| **Delay** | Pause execution | Duration | - |
| **Approval** | Human-in-the-loop | Prompt | Approved/Denied |
| **Notification** | Send notification | Message | Sent status |
| **Connector** | External API call | HTTP config | API response |
| **Plugin** | Run plugin action | Plugin, params | Plugin output |
| **Export** | Output/save result | Data format | Saved file |
| **Import** | Load external data | Source | Parsed data |

### Workflow Definition Format

```kotlin
@Serializable
data class WorkflowDefinition(
    val id: String,
    val name: String,
    val description: String = "",
    val version: String = "1.0",
    val blocks: List<WorkflowBlock>,
    val connections: List<BlockConnection>,
    val metadata: WorkflowMetadata = WorkflowMetadata()
)

@Serializable
data class WorkflowBlock(
    val id: String,
    val type: BlockType,
    val position: BlockPosition,
    val label: String = "",
    val config: Map<String, Any> = emptyMap(),
    val inputs: List<BlockPort> = emptyList(),
    val outputs: List<BlockPort> = emptyList()
)

@Serializable
enum class BlockType {
    GOAL, AI, BROWSER, ANDROID, VISION, VOICE,
    MEMORY, CONDITION, LOOP, DELAY, APPROVAL,
    NOTIFICATION, CONNECTOR, PLUGIN, EXPORT, IMPORT
}

@Serializable
data class BlockConnection(
    val id: String,
    val sourceBlockId: String,
    val sourcePortId: String,
    val targetBlockId: String,
    val targetPortId: String
)
```

### Key Composable (WorkflowCanvas)

```kotlin
@Composable
fun WorkflowCanvas(
    workflow: WorkflowDefinition,
    onBlockMove: (String, BlockPosition) -> Unit,
    onBlockSelect: (String) -> Unit,
    onConnectionCreate: (BlockConnection) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Render connections as lines between blocks
        workflow.connections.forEach { connection ->
            ConnectionLine(
                source = findBlock(connection.sourceBlockId, workflow.blocks),
                target = findBlock(connection.targetBlockId, workflow.blocks)
            )
        }
        
        // Render workflow blocks
        workflow.blocks.forEach { block ->
            WorkflowBlockCard(
                block = block,
                onDrag = { position -> onBlockMove(block.id, position) },
                onSelect = { onBlockSelect(block.id) }
            )
        }
    }
}

@Composable
fun WorkflowBlockCard(
    block: WorkflowBlock,
    onDrag: (BlockPosition) -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .offset { IntOffset(block.position.x, block.position.y) }
            .width(200.dp)
            .clickable { onSelect() }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(BlockPosition(
                        block.position.x + dragAmount.x,
                        block.position.y + dragAmount.y
                    ))
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = blockColor(block.type)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BlockIcon(type = block.type)
                Spacer(Modifier.width(8.dp))
                Text(block.label, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(4.dp))
            Text(block.type.name, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```
