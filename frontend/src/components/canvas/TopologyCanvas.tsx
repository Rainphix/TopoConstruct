import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  ReactFlowProvider,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { Descriptions, Empty, Spin, Tag } from 'antd';
import { FolderOutlined, DesktopOutlined, AlertOutlined, AppstoreOutlined } from '@ant-design/icons';
import { getNeIcon, getStatusColor } from '@/utils/iconUtils';
import NeNode from './NeNode';
import SubnetNode from './SubnetNode';
import MergeGroupNode from './MergeGroupNode';
import NodeDetailPanel from './NodeDetailPanel';
import NodeContextMenu from './NodeContextMenu';
import CanvasToolbar from './CanvasToolbar';
import { useTopologyStore } from '@/stores/useTopologyStore';
import { useTreeStore } from '@/stores/useTreeStore';
import type { TopoNodeData } from '@/utils/topoUtils';
import type { ElementType } from '@/types/api';

const nodeTypes = {
  ne: NeNode,
  subnet: SubnetNode,
  mergeGroup: MergeGroupNode,
};

const NE_TYPE_LABELS: Record<string, string> = {
  FIREWALL: '防火墙',
  SWITCH: '交换机',
  SERVER: '服务器',
  STORAGE: '存储设备',
  GATEWAY: '网关',
  CHASSIS: '机框',
  RACK: '机架',
  DEFAULT: '通用设备',
};

function TopologyCanvasInner() {
  const nodes = useTopologyStore((s) => s.nodes);
  const edges = useTopologyStore((s) => s.edges);
  const loading = useTopologyStore((s) => s.loading);
  const subnetDn = useTopologyStore((s) => s.subnetDn);
  const subnetName = useTopologyStore((s) => s.subnetName);
  const selectedNeDetail = useTopologyStore((s) => s.selectedNeDetail);
  const setNodes = useTopologyStore((s) => s.setNodes);
  const setSelectedNodeId = useTopologyStore((s) => s.setSelectedNodeId);
  const fetchView = useTopologyStore((s) => s.fetchView);
  const setSelectedKey = useTreeStore((s) => s.setSelectedKey);
  const isDemo = useTopologyStore((s) => s.isDemo);
  const viewCriticalCount = useTopologyStore((s) => s.viewCriticalCount);
  const viewMajorCount = useTopologyStore((s) => s.viewMajorCount);
  const viewMinorCount = useTopologyStore((s) => s.viewMinorCount);
  const viewWarningCount = useTopologyStore((s) => s.viewWarningCount);
  const viewNeCount = useTopologyStore((s) => s.viewNeCount);
  const viewOnlineCount = useTopologyStore((s) => s.viewOnlineCount);
  const viewOfflineCount = useTopologyStore((s) => s.viewOfflineCount);
  const viewSubnetCount = useTopologyStore((s) => s.viewSubnetCount);
  const totalAlarm = viewCriticalCount + viewMajorCount + viewMinorCount + viewWarningCount;

  const [contextMenu, setContextMenu] = useState<{
    x: number;
    y: number;
    dn: string;
    name: string;
    type: ElementType;
    isMergeGroup: boolean;
  } | null>(null);

  const onNodeClick = useCallback(
    (_: React.MouseEvent, node: { id: string }) => {
      setSelectedNodeId(node.id);
    },
    [setSelectedNodeId],
  );

  const onNodeDoubleClick = useCallback(
    (_: React.MouseEvent, node: { id: string; data: Record<string, unknown> }) => {
      const data = node.data as TopoNodeData;
      if (data.type === 'SUBNET') {
        setSelectedKey(node.id);
        fetchView(node.id);
      }
    },
    [setSelectedKey, fetchView],
  );

  const onNodeContextMenu = useCallback(
    (event: React.MouseEvent, node: { id: string; data: Record<string, unknown> }) => {
      event.preventDefault();
      const data = node.data as TopoNodeData;
      setContextMenu({
        x: event.clientX,
        y: event.clientY,
        dn: node.id,
        name: data.name,
        type: data.type as ElementType,
        isMergeGroup: data.isMergeGroup,
      });
    },
    [],
  );

  const onPaneClick = useCallback(() => {
    setSelectedNodeId(null);
    setContextMenu(null);
  }, [setSelectedNodeId]);

  const onNodesChange = useCallback(
    (changes: Array<{ type: string; id: string; position?: { x: number; y: number } }>) => {
      const updatedNodes = nodes.map((node) => {
        const change = changes.find((c) => c.id === node.id && c.type === 'position');
        if (change?.position) {
          return { ...node, position: change.position };
        }
        return node;
      });
      setNodes(updatedNodes);
    },
    [nodes, setNodes],
  );

  useEffect(() => {
    const handler = (e: Event) => {
      const dn = (e as CustomEvent).detail as string;
      setSelectedKey(dn);
      fetchView(dn);
    };
    window.addEventListener('subnetDrillDown', handler);
    return () => window.removeEventListener('subnetDrillDown', handler);
  }, [setSelectedKey, fetchView]);

  const defaultEdgeOptions = useMemo(
    () => ({
      type: 'smoothstep' as const,
      style: { stroke: '#bfbfbf', strokeWidth: 1.5 },
      animated: false,
    }),
    [],
  );

  const showNeDetail = !!selectedNeDetail;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <CanvasToolbar />
      {/* 子网标题横幅 + 告警统计 */}
      {subnetDn && !loading && !showNeDetail && (
        <div
          style={{
            padding: '8px 16px',
            background: 'linear-gradient(90deg, #1890ff, #36cfc9)',
            color: '#fff',
            fontWeight: 600,
            fontSize: 14,
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            letterSpacing: 1,
            boxShadow: '0 2px 8px rgba(24,144,255,0.25)',
          }}
        >
          <FolderOutlined style={{ fontSize: 16 }} />
          <span>{subnetName || subnetDn}</span>
          {/* 设备统计 */}
          <span style={{ marginLeft: 8, display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 12, fontWeight: 400 }}>
            <AppstoreOutlined />
            {viewNeCount > 0 && <span>NE {viewNeCount}</span>}
            {viewSubnetCount > 0 && <span>子网 {viewSubnetCount}</span>}
            {viewNeCount > 0 && (
              <span style={{ opacity: 0.85 }}>
                ({viewOnlineCount} 在线 / {viewOfflineCount} 离线)
              </span>
            )}
          </span>
          {/* 告警统计 */}
          {totalAlarm > 0 && (
            <span style={{ marginLeft: 'auto', display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 12 }}>
              <AlertOutlined style={{ color: '#ffccc7' }} />
              {viewCriticalCount > 0 && <span style={{
                background: '#ff4d4f', borderRadius: 4, padding: '0 6px', fontWeight: 600,
              }}>严重 {viewCriticalCount}</span>}
              {viewMajorCount > 0 && <span style={{
                background: '#fa8c16', borderRadius: 4, padding: '0 6px', fontWeight: 600,
              }}>主要 {viewMajorCount}</span>}
              {viewMinorCount > 0 && <span style={{
                background: '#1890ff', borderRadius: 4, padding: '0 6px', fontWeight: 600,
              }}>一般 {viewMinorCount}</span>}
              {viewWarningCount > 0 && <span style={{
                background: '#faad14', color: '#595959', borderRadius: 4, padding: '0 6px', fontWeight: 600,
              }}>警告 {viewWarningCount}</span>}
              <span style={{ opacity: 0.85 }}>共 {totalAlarm} 条</span>
            </span>
          )}
        </div>
      )}
      {/* 网元标题横幅 */}
      {showNeDetail && (
        <div
          style={{
            padding: '8px 16px',
            background: 'linear-gradient(90deg, #722ed1, #b37feb)',
            color: '#fff',
            fontWeight: 600,
            fontSize: 14,
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            letterSpacing: 1,
            boxShadow: '0 2px 8px rgba(114,46,209,0.25)',
          }}
        >
          <DesktopOutlined style={{ fontSize: 16 }} />
          <span>{selectedNeDetail.displayName || selectedNeDetail.name}</span>
        </div>
      )}
      <div style={{ flex: 1, position: 'relative' }}>
        {isDemo && !showNeDetail && (
          <Tag
            color="blue"
            style={{
              position: 'absolute',
              top: 8,
              right: 8,
              zIndex: 10,
              fontSize: 12,
            }}
          >
            演示模式
          </Tag>
        )}
        {/* 网元详情卡片 */}
        {showNeDetail ? (
          <div
            style={{
              height: '100%',
              display: 'flex',
              alignItems: 'flex-start',
              justifyContent: 'center',
              padding: 32,
              background: '#f5f5f5',
              overflow: 'auto',
            }}
          >
            <div
              style={{
                background: '#fff',
                borderRadius: 12,
                boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
                padding: 0,
                width: '100%',
                maxWidth: 600,
                overflow: 'hidden',
              }}
            >
              {/* 卡片头部 */}
              <div
                style={{
                  background: 'linear-gradient(135deg, #722ed1, #b37feb)',
                  padding: '20px 24px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 16,
                }}
              >
                <div
                  style={{
                    width: 56,
                    height: 56,
                    borderRadius: 12,
                    background: 'rgba(255,255,255,0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  {(() => {
                    const Icon = getNeIcon(selectedNeDetail.neType);
                    return <Icon style={{ fontSize: 28, color: '#fff' }} />;
                  })()}
                </div>
                <div>
                  <div style={{ color: '#fff', fontSize: 18, fontWeight: 700 }}>
                    {selectedNeDetail.displayName || selectedNeDetail.name}
                  </div>
                  <div style={{ color: 'rgba(255,255,255,0.85)', fontSize: 13, marginTop: 2 }}>
                    {NE_TYPE_LABELS[selectedNeDetail.neType] || selectedNeDetail.neType}
                  </div>
                </div>
                <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 6 }}>
                  <span
                    style={{
                      width: 10,
                      height: 10,
                      borderRadius: '50%',
                      background: getStatusColor(selectedNeDetail.status),
                      display: 'inline-block',
                    }}
                  />
                  <span style={{ color: '#fff', fontSize: 13 }}>
                    {selectedNeDetail.statusDesc || (selectedNeDetail.status === 1 ? '在线' : '离线')}
                  </span>
                </div>
              </div>
              {/* 卡片内容 */}
              <Descriptions
                column={1}
                bordered
                size="small"
                style={{ margin: 0 }}
                labelStyle={{ width: 120, fontWeight: 500, background: '#fafafa' }}
                contentStyle={{ background: '#fff' }}
              >
                <Descriptions.Item label="DN">{selectedNeDetail.dn}</Descriptions.Item>
                <Descriptions.Item label="名称">{selectedNeDetail.name}</Descriptions.Item>
                {selectedNeDetail.address && (
                  <Descriptions.Item label="IP 地址">{selectedNeDetail.address}</Descriptions.Item>
                )}
                {selectedNeDetail.location && (
                  <Descriptions.Item label="位置">{selectedNeDetail.location}</Descriptions.Item>
                )}
                {selectedNeDetail.maintainer && (
                  <Descriptions.Item label="维护人">{selectedNeDetail.maintainer}</Descriptions.Item>
                )}
                {selectedNeDetail.contact && (
                  <Descriptions.Item label="联系方式">{selectedNeDetail.contact}</Descriptions.Item>
                )}
                {(() => {
                  const c = selectedNeDetail.criticalCount ?? 0;
                  const m = selectedNeDetail.majorCount ?? 0;
                  const n = selectedNeDetail.minorCount ?? 0;
                  const w = selectedNeDetail.warningCount ?? 0;
                  const total = c + m + n + w;
                  if (total === 0) return null;
                  return (
                    <Descriptions.Item label="告警状态">
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                        {c > 0 && <Tag color="red">严重 {c}</Tag>}
                        {m > 0 && <Tag color="orange">主要 {m}</Tag>}
                        {n > 0 && <Tag color="blue">一般 {n}</Tag>}
                        {w > 0 && <Tag color="gold">警告 {w}</Tag>}
                        <span style={{ color: '#8c8c8c', fontSize: 12 }}>共 {total} 条</span>
                      </div>
                    </Descriptions.Item>
                  );
                })()}
              </Descriptions>
            </div>
          </div>
        ) : !subnetDn ? (
          <div
            style={{
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Empty description="请在左侧树中选择子网或网元" />
          </div>
        ) : loading && nodes.length === 0 ? (
          <div
            style={{
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Spin size="large" />
          </div>
        ) : (
          <>
            <ReactFlow
              nodes={nodes}
              edges={edges}
              nodeTypes={nodeTypes}
              defaultEdgeOptions={defaultEdgeOptions}
              onNodeClick={onNodeClick}
              onNodeDoubleClick={onNodeDoubleClick}
              onNodeContextMenu={onNodeContextMenu}
              onPaneClick={onPaneClick}
              onNodesChange={onNodesChange}
              fitView
              style={{ background: '#f5f5f5' }}
            >
              <Background gap={20} size={1} />
              <Controls showInteractive={false} />
              <MiniMap
                nodeStrokeWidth={3}
                style={{ border: '1px solid #d9d9d9' }}
              />
            </ReactFlow>
            <NodeDetailPanel />
            {contextMenu && (
              <NodeContextMenu
                x={contextMenu.x}
                y={contextMenu.y}
                nodeDn={contextMenu.dn}
                nodeName={contextMenu.name}
                nodeType={contextMenu.type}
                isMergeGroup={contextMenu.isMergeGroup}
                onClose={() => setContextMenu(null)}
              />
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default function TopologyCanvas() {
  return (
    <ReactFlowProvider>
      <TopologyCanvasInner />
    </ReactFlowProvider>
  );
}
