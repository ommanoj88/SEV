import React, { useCallback, useState, useEffect } from 'react';
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  Node,
  useNodesState,
  useEdgesState,
  BackgroundVariant,
  useReactFlow,
  ReactFlowProvider,
} from 'reactflow';
import 'reactflow/dist/style.css';
import {
  eventNodes,
  eventEdges,
  getLayoutedElements,
  NodeDataType,
  nodeColors,
} from './flowUtils';
import NodeDetailsPanel from './NodeDetailsPanel';

const EventFlowInner: React.FC = () => {
  const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
    eventNodes,
    eventEdges,
    'TB'
  );

  // Transform nodes to include label in the correct format for ReactFlow
  const transformedNodes = layoutedNodes.map(node => ({
    ...node,
    data: {
      ...node.data,
      label: `${node.data.icon || ''} ${node.data.label}`,
    },
  }));

  const [nodes, , onNodesChange] = useNodesState(transformedNodes);
  const [edges, , onEdgesChange] = useEdgesState(layoutedEdges);
  const [selectedNode, setSelectedNode] = useState<Node<NodeDataType> | null>(null);
  const { fitView } = useReactFlow();

  // Fit view after nodes are initialized
  useEffect(() => {
    setTimeout(() => {
      fitView({ padding: 0.2, duration: 200 });
    }, 100);
  }, [fitView]);

  const onNodeClick = useCallback((_: React.MouseEvent, node: Node<NodeDataType>) => {
    setSelectedNode(node);
  }, []);

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative', backgroundColor: '#f5f5f5' }}>
      <div style={{ padding: '8px', backgroundColor: '#c8e6c9', color: '#2e7d32', fontSize: 12 }}>
        Debug: {nodes.length} nodes, {edges.length} edges loaded
      </div>
      <div style={{ width: '100%', height: 'calc(100% - 30px)' }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onNodeClick={onNodeClick}
          fitView
          fitViewOptions={{ padding: 0.2 }}
          attributionPosition="bottom-left"
          minZoom={0.1}
          maxZoom={4}
          proOptions={{ hideAttribution: true }}
        >
          <Background variant={BackgroundVariant.Lines} gap={24} size={1} />
          <Controls />
          <MiniMap
            nodeColor={(node) => {
              const data = node.data as NodeDataType;
              return nodeColors[data?.type] || '#666';
            }}
          />
        </ReactFlow>
      </div>
      <NodeDetailsPanel node={selectedNode} onClose={() => setSelectedNode(null)} />
    </div>
  );
};

const EventFlow: React.FC = () => (
  <ReactFlowProvider>
    <EventFlowInner />
  </ReactFlowProvider>
);

export default EventFlow;
