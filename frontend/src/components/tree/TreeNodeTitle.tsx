import { useState } from 'react';
import type { TreeNodeVO } from '@/types/topology';
import { getStatusColor } from '@/utils/iconUtils';
import NodeContextMenu from '@/components/canvas/NodeContextMenu';

interface Props {
  node: TreeNodeVO;
}

export default function TreeNodeTitle({ node }: Props) {
  const [contextMenu, setContextMenu] = useState<{
    x: number;
    y: number;
  } | null>(null);

  const handleContextMenu = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setContextMenu({ x: e.clientX, y: e.clientY });
  };

  const statusColor = getStatusColor(node.status);
  const alarmCount = node.alarmCount ?? 0;

  return (
    <>
      <span
        onContextMenu={handleContextMenu}
        style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}
      >
        {node.isMergeGroup && (
          <span
            style={{
              fontSize: 10,
              background: '#e6f7ff',
              color: '#1890ff',
              padding: '0 4px',
              borderRadius: 4,
            }}
          >
            合并
          </span>
        )}
        <span>{node.displayName || node.name}</span>
        <span
          style={{
            width: 6,
            height: 6,
            borderRadius: '50%',
            background: statusColor,
            display: 'inline-block',
          }}
        />
        {alarmCount > 0 && (
          <span
            style={{
              minWidth: 16,
              height: 16,
              borderRadius: 8,
              background: '#ff4d4f',
              color: '#fff',
              fontSize: 10,
              fontWeight: 600,
              lineHeight: '16px',
              textAlign: 'center',
              padding: '0 3px',
            }}
          >
            {alarmCount > 99 ? '99+' : alarmCount}
          </span>
        )}
      </span>
      {contextMenu && (
        <NodeContextMenu
          x={contextMenu.x}
          y={contextMenu.y}
          nodeDn={node.dn}
          nodeName={node.displayName || node.name}
          nodeType={node.type}
          isMergeGroup={node.isMergeGroup}
          onClose={() => setContextMenu(null)}
        />
      )}
    </>
  );
}
