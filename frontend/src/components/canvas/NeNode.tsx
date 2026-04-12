import { Handle, Position } from '@xyflow/react';
import type { NodeProps } from '@xyflow/react';
import { getNeIcon, getStatusColor } from '@/utils/iconUtils';
import type { TopoNodeData } from '@/utils/topoUtils';

export default function NeNode({ data, selected }: NodeProps) {
  const d = data as TopoNodeData;
  const Icon = getNeIcon(d.neType);
  const statusColor = getStatusColor(d.status);
  const totalAlarm = (d.criticalCount ?? 0) + (d.majorCount ?? 0) + (d.minorCount ?? 0) + (d.warningCount ?? 0);

  return (
    <div
      style={{
        width: d.width || 80,
        height: d.height || 60,
        border: `2px solid ${selected ? '#1890ff' : '#d9d9d9'}`,
        borderRadius: 8,
        background: '#fff',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: 12,
        boxShadow: selected ? '0 0 8px rgba(24,144,255,0.4)' : '0 1px 4px rgba(0,0,0,0.1)',
        cursor: 'grab',
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
      <Icon style={{ fontSize: 20, color: '#595959', marginBottom: 2 }} />
      <span
        style={{
          maxWidth: d.width ? d.width - 8 : 72,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          textAlign: 'center',
        }}
      >
        {d.name}
      </span>
      <span
        style={{
          position: 'absolute',
          top: 4,
          right: 4,
          width: 8,
          height: 8,
          borderRadius: '50%',
          background: statusColor,
        }}
      />
    </div>
  );
}
