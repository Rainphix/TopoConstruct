import type { Node } from '@xyflow/react';
import type { TopoElement } from '@/types/topology';
import type { NeType } from '@/types/api';

/** 自定义节点数据类型 */
export interface TopoNodeData extends Record<string, unknown> {
  dn: string;
  name: string;
  type: string;
  neType?: NeType;
  icon?: string;
  status?: string;
  isMergeGroup: boolean;
  childCount: number;
  criticalCount?: number;
  majorCount?: number;
  minorCount?: number;
  warningCount?: number;
  width: number;
  height: number;
}

/** 将 TopoElement 转换为 ReactFlow Node */
export function topoElementToNode(el: TopoElement): Node {
  return {
    id: el.dn,
    type: el.isMergeGroup
      ? 'mergeGroup'
      : el.type === 'SUBNET'
        ? 'subnet'
        : 'ne',
    position: { x: el.x, y: el.y },
    data: {
      dn: el.dn,
      name: el.name,
      type: el.type,
      neType: el.neType,
      icon: el.icon,
      status: el.status,
      isMergeGroup: el.isMergeGroup,
      childCount: el.childCount,
      criticalCount: el.criticalCount,
      majorCount: el.majorCount,
      minorCount: el.minorCount,
      warningCount: el.warningCount,
      width: el.width,
      height: el.height,
    } satisfies TopoNodeData,
  };
}

/** 构建边：子网内的网元与子网关联 */
export function buildEdges(elements: TopoElement[]) {
  const edges: Array<{ id: string; source: string; target: string }> = [];
  const subnets = elements.filter((e) => e.type === 'SUBNET');

  for (const subnet of subnets) {
    for (const el of elements) {
      if (el.type === 'NE' || el.type === 'MERGE_GROUP') {
        edges.push({
          id: `e-${subnet.dn}-${el.dn}`,
          source: subnet.dn,
          target: el.dn,
        });
      }
    }
  }
  return edges;
}
