import { Node, Edge, MarkerType } from 'reactflow';
import dagre from 'dagre';

// Color scheme for different node types
export const nodeColors = {
  service: '#4CAF50',
  database: '#2196F3',
  queue: '#FF9800',
  cache: '#F44336',
  frontend: '#9C27B0',
  module: '#00BCD4',
  gateway: '#795548',
  external: '#607D8B',
  client: '#E91E63',
  planned: '#9E9E9E',
};

// Node type icons (emoji)
export const nodeIcons: Record<string, string> = {
  service: '⚙️',
  database: '🗄️',
  queue: '📬',
  cache: '⚡',
  frontend: '🖥️',
  module: '📦',
  gateway: '🚪',
  external: '☁️',
  client: '👤',
};

export interface TableColumn {
  name: string;
  type: string;
  key?: boolean;
  nullable?: boolean;
}

export interface TableInfo {
  name: string;
  columns: TableColumn[];
  relationships?: string[];
}

export interface EndpointInfo {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  path: string;
  description?: string;
}

export interface NodeDataType {
  label: string;
  description?: string;
  icon?: string;
  type: keyof typeof nodeColors;
  port?: number;
  endpoints?: EndpointInfo[];
  tables?: TableInfo[];
  technologies?: string[];
  events?: string[];
}

// Create a styled node
export const createNode = (
  id: string,
  label: string,
  type: keyof typeof nodeColors,
  position: { x: number; y: number },
  data?: Partial<NodeDataType>
): Node<NodeDataType> => ({
  id,
  type: 'default', // Use default ReactFlow node type
  position,
  data: {
    label,
    type,
    icon: nodeIcons[type],
    ...data,
  },
  style: {
    background: nodeColors[type],
    color: '#fff',
    border: '2px solid #333',
    borderRadius: 8,
    padding: 10,
    minWidth: 150,
    textAlign: 'center',
    fontSize: 12,
    fontWeight: 500,
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
  },
});

// Create an edge between nodes
export const createEdge = (
  source: string,
  target: string,
  label?: string,
  animated?: boolean,
  style?: Record<string, unknown>
): Edge => ({
  id: `${source}-${target}`,
  source,
  target,
  label,
  animated: animated ?? false,
  type: 'smoothstep',
  markerEnd: { type: MarkerType.ArrowClosed },
  style: {
    strokeWidth: 2,
    stroke: '#666',
    ...style,
  },
  labelStyle: {
    fontSize: 10,
    fontWeight: 500,
  },
  labelBgStyle: {
    fill: '#fff',
    fillOpacity: 0.8,
  },
});

// Auto-layout using dagre
export const getLayoutedElements = (
  nodes: Node[],
  edges: Edge[],
  direction: 'TB' | 'LR' = 'TB'
) => {
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));

  const nodeWidth = 180;
  const nodeHeight = 80;

  dagreGraph.setGraph({ rankdir: direction, ranksep: 80, nodesep: 50 });

  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  const layoutedNodes = nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    return {
      ...node,
      position: {
        x: nodeWithPosition.x - nodeWidth / 2,
        y: nodeWithPosition.y - nodeHeight / 2,
      },
    };
  });

  return { nodes: layoutedNodes, edges };
};

// Architecture nodes
export const architectureNodes: Node<NodeDataType>[] = [
  createNode('client', 'Web Browser', 'client', { x: 400, y: 0 }, {
    description: 'End users accessing the platform',
    technologies: ['Chrome', 'Firefox', 'Safari', 'Edge'],
  }),
  createNode('react', 'React Frontend', 'frontend', { x: 400, y: 100 }, {
    port: 3000,
    description: 'React 18 with Material-UI and Redux',
    technologies: ['React 18', 'TypeScript', 'Material-UI', 'Redux Toolkit'],
  }),
  createNode('gateway', 'API Gateway', 'gateway', { x: 400, y: 200 }, {
    port: 8080,
    description: 'Single entry point for all API requests',
    technologies: ['Spring Cloud Gateway', 'Rate Limiting', 'Load Balancing'],
  }),
  createNode('auth', 'Auth Service', 'service', { x: 100, y: 320 }, {
    port: 8081,
    description: 'Authentication & Authorization',
    technologies: ['Spring Security', 'Firebase', 'JWT'],
    endpoints: [
      { method: 'POST', path: '/api/auth/login' },
      { method: 'POST', path: '/api/auth/register' },
      { method: 'GET', path: '/api/auth/me' },
    ],
    tables: [
      { name: 'users', columns: [{ name: 'id', type: 'UUID', key: true }], relationships: ['roles', 'companies'] },
      { name: 'roles', columns: [{ name: 'id', type: 'UUID', key: true }] },
      { name: 'companies', columns: [{ name: 'id', type: 'UUID', key: true }] },
    ],
  }),
  createNode('fleet', 'Fleet Service', 'service', { x: 250, y: 320 }, {
    port: 8082,
    description: 'Vehicle & Fleet Management',
    technologies: ['Spring Boot', 'JPA'],
    endpoints: [
      { method: 'GET', path: '/api/fleet/vehicles' },
      { method: 'POST', path: '/api/fleet/vehicles' },
      { method: 'GET', path: '/api/fleet/trips' },
    ],
    tables: [
      { name: 'fleets', columns: [{ name: 'id', type: 'UUID', key: true }] },
      { name: 'vehicles', columns: [{ name: 'id', type: 'UUID', key: true }] },
      { name: 'vehicle_events', columns: [{ name: 'id', type: 'UUID', key: true }] },
    ],
  }),
  createNode('charging', 'Charging Service', 'service', { x: 400, y: 320 }, {
    port: 8083,
    description: 'Charging Infrastructure Management',
    technologies: ['Spring Boot', 'WebSocket', 'OCPP'],
    endpoints: [
      { method: 'GET', path: '/api/charging/stations' },
      { method: 'POST', path: '/api/charging/sessions' },
    ],
    tables: [
      { name: 'charging_stations', columns: [{ name: 'id', type: 'UUID', key: true }] },
      { name: 'charging_sessions', columns: [{ name: 'id', type: 'UUID', key: true }] },
      { name: 'connectors', columns: [{ name: 'id', type: 'UUID', key: true }] },
    ],
  }),
  createNode('maintenance', 'Maintenance Service', 'service', { x: 550, y: 320 }, {
    port: 8084,
    description: 'Predictive Maintenance & Work Orders',
    technologies: ['Spring Boot', 'ML Models'],
    endpoints: [
      { method: 'GET', path: '/api/maintenance/schedules' },
      { method: 'POST', path: '/api/maintenance/work-orders' },
    ],
    tables: [
      { name: 'maintenance_schedules', columns: [{ name: 'id', type: 'UUID', key: true }] },
      { name: 'work_orders', columns: [{ name: 'id', type: 'UUID', key: true }] },
    ],
  }),
  createNode('driver', 'Driver Service', 'service', { x: 700, y: 320 }, {
    port: 8085,
    description: 'Driver Management & Performance',
    technologies: ['Spring Boot', 'Analytics'],
    endpoints: [
      { method: 'GET', path: '/api/drivers' },
      { method: 'GET', path: '/api/drivers/performance' },
    ],
    tables: [
      { name: 'drivers', columns: [{ name: 'id', type: 'UUID', key: true }] },
      { name: 'driver_assignments', columns: [{ name: 'id', type: 'UUID', key: true }] },
    ],
  }),
  createNode('analytics', 'Analytics Service', 'service', { x: 200, y: 420 }, {
    port: 8086,
    description: 'Business Intelligence & Reporting',
    technologies: ['Spring Boot', 'Charts', 'Export'],
    endpoints: [
      { method: 'GET', path: '/api/analytics/fleet' },
      { method: 'GET', path: '/api/analytics/costs' },
    ],
  }),
  createNode('notification', 'Notification Service', 'service', { x: 400, y: 420 }, {
    port: 8087,
    description: 'Alerts, Email, SMS, Push',
    technologies: ['Spring Boot', 'Firebase FCM', 'SendGrid'],
    endpoints: [
      { method: 'POST', path: '/api/notifications' },
      { method: 'GET', path: '/api/alerts' },
    ],
  }),
  createNode('billing', 'Billing Service', 'service', { x: 600, y: 420 }, {
    port: 8088,
    description: 'Invoicing & Cost Tracking',
    technologies: ['Spring Boot', 'Stripe'],
    endpoints: [
      { method: 'GET', path: '/api/billing/invoices' },
      { method: 'POST', path: '/api/billing/payments' },
    ],
  }),
  createNode('postgres', 'PostgreSQL', 'database', { x: 200, y: 550 }, {
    port: 5432,
    description: 'Primary Database',
    technologies: ['PostgreSQL 15', 'Multi-DB per service'],
  }),
  createNode('redis', 'Redis', 'cache', { x: 400, y: 550 }, {
    port: 6379,
    description: 'Caching & Session Store',
    technologies: ['Redis 7', 'Pub/Sub'],
  }),
  createNode('firebase', 'Firebase Auth', 'external', { x: 100, y: 100 }, {
    description: 'External Authentication Provider',
    technologies: ['Firebase Auth', 'OAuth 2.0'],
  }),
];

// Architecture edges
export const architectureEdges: Edge[] = [
  createEdge('client', 'react'),
  createEdge('react', 'gateway', 'HTTP/REST'),
  createEdge('react', 'firebase', 'Auth'),
  createEdge('gateway', 'auth'),
  createEdge('gateway', 'fleet'),
  createEdge('gateway', 'charging'),
  createEdge('gateway', 'maintenance'),
  createEdge('gateway', 'driver'),
  createEdge('gateway', 'analytics'),
  createEdge('gateway', 'notification'),
  createEdge('gateway', 'billing'),
  createEdge('auth', 'postgres'),
  createEdge('fleet', 'postgres'),
  createEdge('charging', 'postgres'),
  createEdge('maintenance', 'postgres'),
  createEdge('driver', 'postgres'),
  createEdge('analytics', 'postgres'),
  createEdge('notification', 'postgres'),
  createEdge('billing', 'postgres'),
  createEdge('auth', 'redis'),
  createEdge('gateway', 'redis'),
  createEdge('auth', 'firebase'),
];

// Frontend pages for frontend flow
export const frontendNodes: Node<NodeDataType>[] = [
  createNode('app', 'App.tsx', 'frontend', { x: 400, y: 0 }, {
    description: 'Main application entry',
  }),
  createNode('routes', 'AppRoutes', 'module', { x: 400, y: 80 }),
  createNode('protected', 'ProtectedRoute', 'module', { x: 250, y: 160 }),
  createNode('public', 'PublicRoute', 'module', { x: 550, y: 160 }),
  createNode('login', 'LoginPage', 'frontend', { x: 500, y: 240 }),
  createNode('register', 'RegisterPage', 'frontend', { x: 600, y: 240 }),
  createNode('dashboard', 'DashboardPage', 'frontend', { x: 100, y: 240 }),
  createNode('fleet-page', 'FleetManagement', 'frontend', { x: 200, y: 320 }),
  createNode('charging-page', 'ChargingPage', 'frontend', { x: 300, y: 320 }),
  createNode('drivers-page', 'DriversPage', 'frontend', { x: 100, y: 320 }),
  createNode('maintenance-page', 'MaintenancePage', 'frontend', { x: 400, y: 320 }),
  createNode('analytics-page', 'AnalyticsPage', 'frontend', { x: 200, y: 400 }),
  createNode('billing-page', 'BillingPage', 'frontend', { x: 300, y: 400 }),
  createNode('redux', 'Redux Store', 'cache', { x: 100, y: 500 }, {
    description: 'State Management',
    technologies: ['Redux Toolkit', 'RTK Query'],
  }),
  createNode('api', 'API Services', 'service', { x: 300, y: 500 }, {
    description: 'Axios HTTP Client',
  }),
];

export const frontendEdges: Edge[] = [
  createEdge('app', 'routes'),
  createEdge('routes', 'protected'),
  createEdge('routes', 'public'),
  createEdge('public', 'login'),
  createEdge('public', 'register'),
  createEdge('protected', 'dashboard'),
  createEdge('protected', 'fleet-page'),
  createEdge('protected', 'charging-page'),
  createEdge('protected', 'drivers-page'),
  createEdge('protected', 'maintenance-page'),
  createEdge('protected', 'analytics-page'),
  createEdge('protected', 'billing-page'),
  createEdge('dashboard', 'redux'),
  createEdge('fleet-page', 'redux'),
  createEdge('redux', 'api'),
];

// Database schema nodes
export const databaseNodes: Node<NodeDataType>[] = [
  createNode('auth-db', 'evfleet_auth', 'database', { x: 100, y: 100 }, {
    tables: [
      { name: 'users', columns: [
        { name: 'id', type: 'UUID', key: true },
        { name: 'email', type: 'VARCHAR(255)' },
        { name: 'firebase_uid', type: 'VARCHAR(255)' },
        { name: 'company_id', type: 'UUID' },
      ]},
      { name: 'roles', columns: [
        { name: 'id', type: 'UUID', key: true },
        { name: 'name', type: 'VARCHAR(50)' },
      ]},
      { name: 'companies', columns: [
        { name: 'id', type: 'UUID', key: true },
        { name: 'name', type: 'VARCHAR(255)' },
      ]},
    ],
  }),
  createNode('fleet-db', 'evfleet_fleet', 'database', { x: 300, y: 100 }, {
    tables: [
      { name: 'vehicles', columns: [
        { name: 'id', type: 'UUID', key: true },
        { name: 'license_plate', type: 'VARCHAR(20)' },
        { name: 'fleet_id', type: 'UUID' },
        { name: 'status', type: 'VARCHAR(20)' },
      ]},
      { name: 'fleets', columns: [
        { name: 'id', type: 'UUID', key: true },
        { name: 'name', type: 'VARCHAR(255)' },
        { name: 'company_id', type: 'UUID' },
      ]},
    ],
  }),
  createNode('charging-db', 'evfleet_charging', 'database', { x: 500, y: 100 }, {
    tables: [
      { name: 'charging_stations', columns: [
        { name: 'id', type: 'UUID', key: true },
        { name: 'name', type: 'VARCHAR(255)' },
        { name: 'location', type: 'POINT' },
      ]},
      { name: 'charging_sessions', columns: [
        { name: 'id', type: 'UUID', key: true },
        { name: 'vehicle_id', type: 'UUID' },
        { name: 'station_id', type: 'UUID' },
        { name: 'energy_kwh', type: 'DECIMAL' },
      ]},
    ],
  }),
  createNode('maintenance-db', 'evfleet_maintenance', 'database', { x: 100, y: 300 }),
  createNode('driver-db', 'evfleet_driver', 'database', { x: 300, y: 300 }),
  createNode('analytics-db', 'evfleet_analytics', 'database', { x: 500, y: 300 }),
  createNode('notification-db', 'evfleet_notification', 'database', { x: 200, y: 450 }),
  createNode('billing-db', 'evfleet_billing', 'database', { x: 400, y: 450 }),
];

// Event flow nodes
export const eventNodes: Node<NodeDataType>[] = [
  createNode('modulith-events', 'Spring Modulith Events', 'service', { x: 200, y: 120 }, {
    description: 'In-process domain events between modules (no external broker wiring yet).',
    technologies: ['Spring Modulith 1.1.0'],
    events: ['Module event publish/listen inside monolith'],
  }),
];

export const eventEdges: Edge[] = [
];
