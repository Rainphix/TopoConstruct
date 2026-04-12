import { Handle, Position } from '@xyflow/react';
import type { NodeProps } from '@xyflow/react';
import { FolderOutlined } from '@ant-design/icons';
import type { TopoNodeData } from '@/utils/topoUtils';

export default function SubnetNode({ data, selected }: NodeProps) {
  const d = data as TopoNodeData;
  const totalAlarm = (d.criticalCount ?? 0) + (d.majorCount ?? 0) + (d.minorCount ?? 0) + (d.warningCount ?? 0);

  return (
    <div
      onDoubleClick={(e) => {
        e.stopPropagation();
        // Drill-down is handled by the parent canvas
        const event = new CustomEvent('subnetDrillDown', { detail: d.dn });
        window.dispatchEvent(event);
      }}
      style={{
        width: d.width || 100,
        height: d.height || 70,
        border: `2px solid ${selected ? '#1890ff' : '#1890ff80'}`,
        borderRadius: 10,
        background: 'linear-gradient(135deg, #e6f7ff, #bae7ff)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: 12,
        fontWeight: 500,
        boxShadow: selected ? '0 0 8px rgba(24,144,255,0.4)' : '0 1px 4px rgba(0,0,0,0.1)',
        cursor: 'pointer',
        position: 'relative',
      }}
    >
      <Handle type="target" position={Position.Top} style={{ visibility: 'hidden' }} />
      <Handle type="source" position={Position.Bottom} style={{ visibility: 'hidden' }} />
      {totalAlarm > 0 && (
        <span
          style={{
            position: 'absolute',
            top: -6,
            right: -6,
            minWidth: 18,
            height: 18,
            borderRadius: 9,
            background: '#ff4d4f',
            color: '#fff',
            fontSize: 10,
            fontWeight: 600,
            lineHeight: '18px',
            textAlign: 'center',
            padding: '0 4px',
            boxShadow: '0 1px 3px rgba(255,77,79,0.4)',
          }}
        >
          {totalAlarm > 99 ? '99+' : totalAlarm}
        </span>
      )}
      <FolderOutlined style={{ fontSize: 20, color: '#1890ff', marginBottom: 2 }} />
      <span
        style={{
          maxWidth: d.width ? d.width - 8 : 92,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          textAlign: 'center',
        }}
      >
        {d.name}
      </span>
      {d.childCount > 0 && (
        <span style={{ fontSize: 10, color: '#8c8c8c' }}>({d.childCount})</span>
      )}
    </div>
  );
}
