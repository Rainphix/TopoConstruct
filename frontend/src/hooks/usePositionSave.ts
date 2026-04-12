import { useCallback, useRef } from 'react';
import { message } from 'antd';
import { useTopologyStore } from '@/stores/useTopologyStore';
import { topologyApi } from '@/api/topologyApi';
import type { ElementType } from '@/types/api';

export function usePositionSave() {
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const savePositions = useCallback(async () => {
    const { nodes, subnetDn } = useTopologyStore.getState();
    if (!subnetDn || nodes.length === 0) return;

    const positions = nodes.map((node) => {
      const data = node.data as {
        type: string;
        width: number;
        height: number;
      };
      return {
        elementDn: node.id,
        elementType: (data.type || 'NE') as ElementType,
        x: node.position.x,
        y: node.position.y,
        width: data.width || 80,
        height: data.height || 60,
      };
    });

    try {
      await topologyApi.savePosition({ subnetDn, positions });
      message.success('位置已保存');
    } catch {
      // error handled by interceptor
    }
  }, []);

  /** 防抖保存 - 3s 后自动保存 */
  const debounceSave = useCallback(() => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }
    timerRef.current = setTimeout(() => {
      savePositions();
    }, 3000);
  }, [savePositions]);

  return { savePositions, debounceSave };
}
