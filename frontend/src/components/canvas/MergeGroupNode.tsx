import { Handle, Position } from '@xyflow/react';
import type { NodeProps } from '@xyflow/react';
import { AppstoreOutlined } from '@ant-design/icons';
import type { TopoNodeData } from '@/utils/topoUtils';

export default function MergeGroupNode({ data, selected }: NodeProps) {
  const d = data as TopoNodeData;

  return (
    <div
      style={{
        width: d.width || 90,
        height: d.height || 65,
        border: `2px dashed ${selected ? '#722ed1' : '#b37feb'}`,
        borderRadius: 10,
        background: 'linear-gradient(135deg, #f9f0ff, #efdbff)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: 12,
        boxShadow: selected ? '0 0 8px rgba(114,46,209,0.3)' : '0 1px 4px rgba(0,0,0,0.1)',
        cursor: 'grab',
        position: 'relative',
      }}
    >
      <Handle type="target" position={Position.Top} style={{ visibility: 'hidden' }} />
      <Handle type="source" position={Position.Bottom} style={{ visibility: 'hidden' }} />
      <AppstoreOutlined style={{ fontSize: 20, color: '#722ed1', marginBottom: 2 }} />
      <span
        style={{
          maxWidth: d.width ? d.width - 8 : 82,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          textAlign: 'center',
        }}
      >
        {d.name}
      </span>
      <span style={{ fontSize: 10, color: '#722ed1' }}>x{d.childCount}</span>
    </div>
  );
}
