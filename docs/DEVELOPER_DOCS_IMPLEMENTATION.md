# Developer Documentation Page - Implementation Plan

## Overview
Create an interactive developer documentation page with visual flowcharts/mind maps (like Google's tools), clickable nodes showing tables and details, smooth graphics. NO authentication required.

## Reference Files
- **Architecture Source**: `docs/ARCHITECTURE/APPLICATION_FLOWCHART.md` (contains all Mermaid diagrams)
- **Routes**: `frontend/src/routes.tsx`
- **Theme**: `frontend/src/theme.ts`

## Dependencies Installed ✅
- `reactflow` - Interactive diagram library
- `dagre` - Graph layout algorithm
- `@types/dagre` - TypeScript definitions

---

## Task Breakdown

### Task 1: Create Main Page Structure ✅ PENDING
**File**: `frontend/src/pages/DeveloperDocsPage.tsx`

```tsx
// Main page with tabs:
// - Architecture Overview
// - Database Schema  
// - API Flow
// - Event Flow
// - Frontend Structure
// - Backend Modules
```

### Task 2: Create Developer Components Folder ✅ PENDING
**Folder**: `frontend/src/components/developer/`

Files to create:
1. `index.ts` - Exports
2. `ArchitectureFlow.tsx` - System architecture diagram
3. `DatabaseSchema.tsx` - ER diagram visualization
4. `APIFlow.tsx` - API request/response flow
5. `EventFlow.tsx` - Event-driven architecture
6. `FrontendFlow.tsx` - Frontend structure
7. `BackendModules.tsx` - Backend modules
8. `NodeDetailsPanel.tsx` - Side panel for clicked node details
9. `flowUtils.ts` - Helper functions for node/edge creation

### Task 3: Add Route (NO AUTH) ✅ PENDING
**File**: `frontend/src/routes.tsx`

Add public route:
```tsx
<Route path="/developer-docs" element={<DeveloperDocsPage />} />
```

---

## Node Data Structure

```typescript
interface NodeData {
  label: string;
  description?: string;
  icon?: string;
  type: 'service' | 'database' | 'queue' | 'cache' | 'frontend' | 'module';
  port?: number;
  endpoints?: string[];
  tables?: TableInfo[];
  technologies?: string[];
}

interface TableInfo {
  name: string;
  columns: { name: string; type: string; key?: boolean }[];
  relationships?: string[];
}
```

---

## Color Scheme

```typescript
const nodeColors = {
  service: '#4CAF50',      // Green - microservices
  database: '#2196F3',     // Blue - PostgreSQL
  cache: '#F44336',        // Red - Redis
  frontend: '#9C27B0',     // Purple - React
  module: '#00BCD4',       // Cyan - modules
  gateway: '#795548',      // Brown - API Gateway
  external: '#607D8B',     // Grey - external services
};
```

---

## Architecture Nodes (from APPLICATION_FLOWCHART.md)

### Main Services
| ID | Label | Port | Type |
|----|-------|------|------|
| react | React Frontend | 3000 | frontend |
| gateway | API Gateway | 8080 | gateway |
| auth | Auth Service | 8081 | service |
| fleet | Fleet Service | 8082 | service |
| charging | Charging Service | 8083 | service |
| maintenance | Maintenance Service | 8084 | service |
| driver | Driver Service | 8085 | service |
| analytics | Analytics Service | 8086 | service |
| notification | Notification Service | 8087 | service |
| billing | Billing Service | 8088 | service |

### Infrastructure
| ID | Label | Port | Type |
|----|-------|------|------|
| postgres | PostgreSQL | 5432 | database |
| redis | Redis | 6379 | cache |
| firebase | Firebase Auth | - | external |

---

## Database Tables (by service)

### Auth Service (evfleet_auth)
- users, roles, permissions, companies, user_roles, role_permissions

### Fleet Service (evfleet_fleet)
- fleets, vehicles, vehicle_events, vehicle_current_state, vehicle_documents

### Charging Service (evfleet_charging)
- charging_stations, connectors, charging_sessions

### Maintenance Service (evfleet_maintenance)
- maintenance_schedules, work_orders, service_records

### Driver Service (evfleet_driver)
- drivers, driver_assignments, driver_performance

### Analytics Service (evfleet_analytics)
- fleet_metrics, vehicle_metrics, utilization_reports

### Notification Service (evfleet_notification)
- notifications, alerts, templates

### Billing Service (evfleet_billing)
- invoices, invoice_lines, payments, subscriptions

---

## Implementation Order

1. **flowUtils.ts** - Node/edge helpers
2. **NodeDetailsPanel.tsx** - Details sidebar
3. **ArchitectureFlow.tsx** - Main architecture view
4. **BackendModules.tsx** - Backend structure
5. **DatabaseSchema.tsx** - Database tables
6. **APIFlow.tsx** - API endpoints
7. **EventFlow.tsx** - Module events
8. **FrontendFlow.tsx** - Frontend pages
9. **DeveloperDocsPage.tsx** - Main page with tabs
10. **routes.tsx** - Add public route
11. **index.ts** - Exports

---

## Quick Start Commands

```bash
# Navigate to frontend
cd frontend

# Start development server
npm start

# Access developer docs
# http://localhost:3000/developer-docs
```

---

## Code Templates

### Basic ReactFlow Component
```tsx
import ReactFlow, { 
  Background, 
  Controls, 
  MiniMap,
  Node,
  Edge,
  useNodesState,
  useEdgesState
} from 'reactflow';
import 'reactflow/dist/style.css';

const MyFlow = () => {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [selectedNode, setSelectedNode] = useState(null);

  return (
    <ReactFlow
      nodes={nodes}
      edges={edges}
      onNodesChange={onNodesChange}
      onEdgesChange={onEdgesChange}
      onNodeClick={(_, node) => setSelectedNode(node)}
      fitView
    >
      <Background />
      <Controls />
      <MiniMap />
    </ReactFlow>
  );
};
```

### Custom Node Component
```tsx
import { Handle, Position } from 'reactflow';

const CustomNode = ({ data }) => (
  <div className="custom-node" style={{ background: data.color }}>
    <Handle type="target" position={Position.Top} />
    <div className="icon">{data.icon}</div>
    <div className="label">{data.label}</div>
    <div className="port">{data.port && `:${data.port}`}</div>
    <Handle type="source" position={Position.Bottom} />
  </div>
);
```

---

*Created: December 3, 2025*
*Status: Ready for Implementation*
